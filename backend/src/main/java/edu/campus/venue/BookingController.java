package edu.campus.venue;

import java.security.Principal;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookings;
    public BookingController(BookingService bookings) { this.bookings = bookings; }
    @GetMapping List<ApiModels.BookingView> list(Principal p) { return bookings.list(p.getName()); }
    @PostMapping @ResponseStatus(CREATED)
    ApiModels.BookingView create(Principal p, @Valid @RequestBody ApiModels.BookingRequest r) { return bookings.create(p.getName(), r); }
    @PostMapping("/{id}/{action}")
    ApiModels.BookingView action(Principal p, @PathVariable Long id, @PathVariable String action) { return bookings.changeStatus(p.getName(), id, action); }
}
