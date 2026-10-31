package edu.campus.venue;

import java.time.LocalDate;
import java.util.*;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@RestController
public class VenueController {
    private final VenueRepository venues;
    private final BookingService bookings;
    public VenueController(VenueRepository venues, BookingService bookings) { this.venues = venues; this.bookings = bookings; }
    @GetMapping("/api/venues") List<Venue> list() { return venues.findAll().stream().sorted(Comparator.comparing(v -> v.id)).toList(); }
    @GetMapping("/api/venues/{id}/slots") List<ApiModels.Slot> slots(@PathVariable Long id, @RequestParam LocalDate date) { return bookings.slots(id, date); }
    @PostMapping("/api/admin/venues") @ResponseStatus(CREATED) @Transactional
    Venue create(@Valid @RequestBody ApiModels.VenueRequest r) { return save(new Venue(), r); }
    @PutMapping("/api/admin/venues/{id}") @Transactional
    Venue update(@PathVariable Long id, @Valid @RequestBody ApiModels.VenueRequest r) {
        return save(venues.lockById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "场馆不存在")), r);
    }
    private Venue save(Venue v, ApiModels.VenueRequest r) {
        if (r.closeHour() <= r.openHour()) throw new ResponseStatusException(BAD_REQUEST, "关闭时间必须晚于开放时间");
        v.name = r.name().trim(); v.category = r.category(); v.location = r.location().trim(); v.description = r.description().trim();
        v.capacity = r.capacity(); v.openHour = r.openHour(); v.closeHour = r.closeHour(); v.enabled = r.enabled(); v.theme = r.theme();
        return venues.save(v);
    }
}
