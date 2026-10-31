package edu.campus.venue;

import java.time.*;
import java.util.List;
import jakarta.validation.constraints.*;

public final class ApiModels {
    private ApiModels() {}
    public record UserView(Long id, String username, String displayName, String role) {
        static UserView from(AppUser u) { return new UserView(u.id, u.username, u.displayName, u.role); }
    }
    public record BookingRequest(@NotNull Long venueId, @NotNull LocalDate date,
            @Min(0) @Max(23) int startHour, @Min(1) @Max(2) int durationHours, @Min(1) int participants,
            @NotBlank @Size(max = 200) String purpose) {}
    public record BookingView(Long id, Long venueId, String venueName, String location, String userName,
            LocalDate date, int startHour, int durationHours, int participants, String purpose, Booking.Status status,
            LocalDateTime createdAt, List<String> availableActions) {
        static BookingView from(Booking b, List<String> availableActions) {
            return new BookingView(b.id, b.venue.id, b.venue.name, b.venue.location, b.user.displayName,
                    b.date, b.startHour, b.durationHours, b.participants, b.purpose, b.status, b.createdAt, availableActions);
        }
    }
    public record Slot(int hour, String status) {}
    public record VenueRequest(@NotBlank @Size(max = 60) String name,
            @NotBlank @Pattern(regexp = "篮球|羽毛球|游泳|健身|足球|网球") String category,
            @NotBlank @Size(max = 100) String location, @NotBlank @Size(max = 500) String description,
            @Min(1) @Max(1000) int capacity, @Min(0) @Max(23) int openHour, @Min(1) @Max(24) int closeHour,
            boolean enabled, @NotBlank @Pattern(regexp = "court|pool|studio|field") String theme) {}
}
