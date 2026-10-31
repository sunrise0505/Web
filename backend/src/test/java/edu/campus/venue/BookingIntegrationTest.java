package edu.campus.venue;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:venue-test;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "app.seed-enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingIntegrationTest {
    @TestConfiguration static class TimeConfig {
        @Bean @Primary Clock testClock() { return Clock.fixed(Instant.parse("2026-10-26T00:00:00Z"), ZoneId.of("Asia/Shanghai")); }
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired BookingService service;
    @Autowired VenueRepository venues;
    @Autowired BookingRepository bookings;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    Venue venue;
    LocalDate tomorrow = LocalDate.of(2026, 10, 27);
    @BeforeEach void setup() {
        bookings.deleteAll(); venues.deleteAll(); users.deleteAll();
        users.save(new AppUser("admin", encoder.encode("Admin123!"), "管理员", "ADMIN"));
        users.save(new AppUser("student", encoder.encode("Student123!"), "学生甲", "STUDENT"));
        users.save(new AppUser("student2", encoder.encode("Student123!"), "学生乙", "STUDENT"));
        venue = venues.save(new Venue("测试羽毛球场", "羽毛球", "测试体育馆", "测试场馆", 4, 8, 22, "court"));
    }
    ApiModels.BookingRequest requestAt(int hour) { return new ApiModels.BookingRequest(venue.id, tomorrow, hour, 1, 2, "双打练习"); }

    @Test void unauthenticatedCannotReadAndCsrfRequired() throws Exception {
        mvc.perform(get("/api/venues")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/bookings").with(user("student").roles("STUDENT"))
            .contentType("application/json").content(json.writeValueAsString(requestAt(18)))).andExpect(status().isForbidden());
    }
    @Test void loginPersistsSessionAndHidesPasswords() throws Exception {
        var result = mvc.perform(post("/auth/login").with(csrf()).param("username", "admin").param("password", "Admin123!"))
            .andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        mvc.perform(get("/auth/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
        mvc.perform(post("/auth/logout").session(session).with(csrf())).andExpect(status().isNoContent());
        assertTrue(session.isInvalid());
    }
    @Test void adminCannotCreatePersonalBooking() throws Exception {
        mvc.perform(post("/api/bookings").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content(json.writeValueAsString(requestAt(18))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.detail").value("管理员账号仅用于管理，请使用学生账号预约"));
        assertEquals(0, bookings.count());
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> service.create("admin", requestAt(18))).getStatusCode().value());
    }
    @Test void dashboardOffersRoleSpecificActionsAndRefreshesCountsAfterReview() throws Exception {
        var created = service.create("student", requestAt(18));
        assertEquals(List.of("cancel"), created.availableActions());
        assertEquals(List.of("approve", "reject", "cancel"), service.list("admin").getFirst().availableActions());
        mvc.perform(get("/api/dashboard").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pendingBookings").value(1))
                .andExpect(jsonPath("$.recentBookings[0].availableActions[0]").value("approve"));
        mvc.perform(post("/api/bookings/" + created.id() + "/approve").with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(get("/api/dashboard").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pendingBookings").value(0))
                .andExpect(jsonPath("$.recentBookings[0].availableActions.length()").value(1))
                .andExpect(jsonPath("$.recentBookings[0].availableActions[0]").value("cancel"));
    }
    @Test void dashboardAllowsCheckInOnlyInsideWindow() throws Exception {
        Booking b = new Booking(); b.user = users.findByUsername("student").orElseThrow(); b.venue = venue;
        b.date = tomorrow.minusDays(1); b.startHour = 8; b.participants = 2; b.purpose = "晨练";
        b.status = Booking.Status.APPROVED; b.createdAt = tomorrow.minusDays(2).atTime(12, 0);
        b = bookings.save(b);
        mvc.perform(get("/api/dashboard").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.recentBookings[0].availableActions[0]").value("check-in"));
        mvc.perform(post("/api/bookings/" + b.id + "/check-in").with(user("student").roles("STUDENT")).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CHECKED_IN"))
                .andExpect(jsonPath("$.availableActions").isEmpty());
        mvc.perform(get("/api/dashboard").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.recentBookings[0].status").value("CHECKED_IN"));
    }
    @Test void studentCannotChangeVenuesOrReviewOthers() throws Exception {
        mvc.perform(post("/api/admin/venues").with(user("student").roles("STUDENT")).with(csrf()).contentType("application/json").content("{}"))
            .andExpect(status().isForbidden());
        var booking = service.create("student", requestAt(18));
        var forbidden = assertThrows(ResponseStatusException.class, () -> service.changeStatus("student2", booking.id(), "cancel"));
        assertEquals(403, forbidden.getStatusCode().value());
        var review = assertThrows(ResponseStatusException.class, () -> service.changeStatus("student", booking.id(), "approve"));
        assertEquals(403, review.getStatusCode().value());
        assertTrue(service.list("student2").isEmpty());
        assertEquals(1, service.list("admin").size());
    }
    @Test void validatesDateHoursAndCapacity() {
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> service.create("student", requestAt(23))).getStatusCode().value());
        assertThrows(ResponseStatusException.class, () -> service.create("student", new ApiModels.BookingRequest(venue.id, tomorrow.plusDays(15), 18, 1, 2, "训练")));
        assertThrows(ResponseStatusException.class, () -> service.create("student", new ApiModels.BookingRequest(venue.id, tomorrow, 18, 1, 5, "训练")));
        assertThrows(ResponseStatusException.class, () -> service.create("student", new ApiModels.BookingRequest(venue.id, tomorrow.minusDays(2), 18, 1, 2, "训练")));
        assertThrows(ResponseStatusException.class, () -> service.create("student", new ApiModels.BookingRequest(venue.id, tomorrow.minusDays(1), 8, 1, 2, "训练")));
    }
    @Test void cancellationReleasesSlotAndApprovalAllowsOnlyValidTransitions() {
        var first = service.create("student", requestAt(18));
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.create("student2", requestAt(18))).getStatusCode().value());
        assertEquals(Booking.Status.CANCELLED, service.changeStatus("student", first.id(), "cancel").status());
        var second = service.create("student2", requestAt(18));
        assertEquals(Booking.Status.APPROVED, service.changeStatus("admin", second.id(), "approve").status());
        assertThrows(ResponseStatusException.class, () -> service.changeStatus("admin", second.id(), "reject"));
        assertThrows(ResponseStatusException.class, () -> service.changeStatus("student2", second.id(), "check-in"));
    }
    @Test void rejectionReleasesSlotAndClosedVenueRejectsBookings() {
        var first = service.create("student", requestAt(18));
        assertEquals(Booking.Status.REJECTED, service.changeStatus("admin", first.id(), "reject").status());
        assertEquals(Booking.Status.PENDING, service.create("student2", requestAt(18)).status());
        venue.enabled = false; venues.save(venue);
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.create("student", requestAt(19))).getStatusCode().value());
        assertTrue(service.slots(venue.id, tomorrow).stream().allMatch(s -> s.status().equals("CLOSED")));
    }
    @Test void checkInWorksDuringWindow() {
        Booking booking = new Booking(); booking.user = users.findByUsername("student").orElseThrow(); booking.venue = venue;
        booking.date = tomorrow.minusDays(1); booking.startHour = 8; booking.participants = 2; booking.purpose = "晨练";
        booking.status = Booking.Status.APPROVED; booking.createdAt = tomorrow.minusDays(2).atTime(12, 0);
        booking = bookings.save(booking);
        assertEquals(Booking.Status.CHECKED_IN, service.changeStatus("student", booking.id, "check-in").status());
        Long id = booking.id;
        assertThrows(ResponseStatusException.class, () -> service.changeStatus("student", id, "check-in"));
    }
    @Test void onlyBookingStudentCanCheckInEvenWhenAdminKnowsId() throws Exception {
        Booking b = new Booking(); b.user = users.findByUsername("student").orElseThrow(); b.venue = venue;
        b.date = tomorrow.minusDays(1); b.startHour = 8; b.status = Booking.Status.APPROVED;
        b.participants = 2; b.purpose = "日常锻炼"; b.createdAt = b.date.atStartOfDay();
        b = bookings.save(b);
        assertFalse(service.list("admin").getFirst().availableActions().contains("check-in"));
        for (String actor : List.of("admin", "student2")) {
            mvc.perform(post("/api/bookings/" + b.id + "/check-in")
                    .with(user(actor).roles(actor.equals("admin") ? "ADMIN" : "STUDENT")).with(csrf()))
                    .andExpect(status().isForbidden());
        }
        assertEquals(Booking.Status.APPROVED, bookings.findById(b.id).orElseThrow().status);
        assertEquals(Booking.Status.CHECKED_IN, service.changeStatus("student", b.id, "check-in").status());
    }
    @Test void twoHourBookingOccupiesBothHoursAndCancellationReleasesBoth() {
        var two = service.create("student", new ApiModels.BookingRequest(venue.id, tomorrow, 18, 2, 2, "日常锻炼"));
        assertEquals(2, two.durationHours());
        assertEquals(2, service.slots(venue.id, tomorrow).stream().filter(s -> s.status().equals("BUSY")).count());
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.create("student2", requestAt(19))).getStatusCode().value());
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.create("student2",
                new ApiModels.BookingRequest(venue.id, tomorrow, 17, 2, 2, "专项训练"))).getStatusCode().value());
        service.create("student2", requestAt(20)); // 相邻但不重叠。
        service.changeStatus("student", two.id(), "cancel");
        assertTrue(service.slots(venue.id, tomorrow).stream().filter(s -> s.hour() == 18 || s.hour() == 19)
                .allMatch(s -> s.status().equals("AVAILABLE")));
    }
    @Test void durationMustBeOneOrTwoAndCannotExtendPastClosing() throws Exception {
        for (int duration : List.of(0, 3)) {
            var r = new ApiModels.BookingRequest(venue.id, tomorrow, 18, duration, 2, "日常锻炼");
            assertEquals(400, assertThrows(ResponseStatusException.class, () -> service.create("student", r)).getStatusCode().value());
            mvc.perform(post("/api/bookings").with(user("student").roles("STUDENT")).with(csrf())
                    .contentType("application/json").content(json.writeValueAsString(r))).andExpect(status().isBadRequest());
        }
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> service.create("student",
                new ApiModels.BookingRequest(venue.id, tomorrow, 21, 2, 2, "日常锻炼"))).getStatusCode().value());
        assertEquals(2, service.create("student", new ApiModels.BookingRequest(venue.id, tomorrow, 20, 2, 2, "日常锻炼")).durationHours());
    }
    @Test void twoHourCheckInWindowIncludesSecondHourButExcludesEnd() {
        Booking b = new Booking(); b.user = users.findByUsername("student").orElseThrow(); b.venue = venue;
        b.date = tomorrow.minusDays(1); b.startHour = 7; b.durationHours = 2; b.status = Booking.Status.APPROVED;
        b.participants = 2; b.purpose = "日常锻炼"; b.createdAt = b.date.atStartOfDay();
        b = bookings.save(b);
        assertTrue(service.list("student").getFirst().availableActions().contains("check-in"));
        assertEquals(Booking.Status.CHECKED_IN, service.changeStatus("student", b.id, "check-in").status());
        b.startHour = 6; b.status = Booking.Status.APPROVED; b = bookings.save(b);
        Long id = b.id;
        assertFalse(service.list("student").getFirst().availableActions().contains("check-in"));
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.changeStatus("student", id, "check-in")).getStatusCode().value());
    }
    @Test void concurrentBookingHasExactlyOneWinner() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            List<Future<Integer>> results = new ArrayList<>();
            for (String username : List.of("student", "student2")) results.add(pool.submit(() -> {
                start.await();
                try { service.create(username, new ApiModels.BookingRequest(venue.id, tomorrow, username.equals("student") ? 18 : 19, 2, 2, "专项训练")); return 201; }
                catch (ResponseStatusException e) { return e.getStatusCode().value(); }
            }));
            start.countDown();
            List<Integer> codes = List.of(results.get(0).get(10, TimeUnit.SECONDS), results.get(1).get(10, TimeUnit.SECONDS));
            assertEquals(1, codes.stream().filter(c -> c == 201).count());
            assertEquals(1, codes.stream().filter(c -> c == 409).count());
            assertEquals(1, bookings.count());
        }
    }
}
