package edu.campus.venue;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Override @EntityGraph(attributePaths = {"user", "venue"})
    Optional<Booking> findById(Long id);
    @Query("select b.venue.id from Booking b where b.id = :id")
    Optional<Long> findVenueId(@Param("id") Long id);
    @EntityGraph(attributePaths = {"user", "venue"})
    List<Booking> findAllByOrderByCreatedAtDesc();
    @EntityGraph(attributePaths = {"user", "venue"})
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findByVenueIdAndDate(Long venueId, LocalDate date);
}
