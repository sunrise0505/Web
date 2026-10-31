package edu.campus.venue;

import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import static edu.campus.venue.Booking.Status.*;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final BookingService bookings;
    private final VenueRepository venues;
    private final Clock clock;
    private final Environment env;
    public DashboardController(BookingService bookings, VenueRepository venues, Clock clock, Environment env) { this.bookings = bookings; this.venues = venues; this.clock = clock; this.env = env; }
    @GetMapping("/dashboard") Map<String, Object> dashboard(Principal p) {
        var list = bookings.list(p.getName());
        LocalDate today = LocalDate.now(clock);
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            trend.add(Map.of("date", day, "count", list.stream().filter(b -> b.date().equals(day) && b.status() != CANCELLED && b.status() != REJECTED).count()));
        }
        return Map.of("venueCount", venues.count(), "openVenues", venues.findAll().stream().filter(v -> v.enabled).count(),
                "todayBookings", list.stream().filter(b -> b.date().equals(today) && BookingService.ACTIVE.contains(b.status())).count(),
                "pendingBookings", list.stream().filter(b -> b.status() == PENDING).count(),
                "totalBookings", list.size(), "trend", trend,
                "recentBookings", list.stream().limit(5).toList());
    }
    @GetMapping("/system") Map<String, Object> system() { return Map.of("demo", Arrays.asList(env.getActiveProfiles()).contains("demo") || (env.getActiveProfiles().length == 0 && Arrays.asList(env.getDefaultProfiles()).contains("demo")), "date", LocalDate.now(clock)); }
}
