package edu.campus.venue;

import java.time.*;
import jakarta.persistence.*;

@Entity
@Table(name = "booking", indexes = @Index(name = "idx_booking_slot", columnList = "venue_id,booking_date,start_hour"))
public class Booking {
    public enum Status { PENDING, APPROVED, REJECTED, CANCELLED, CHECKED_IN }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") public AppUser user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "venue_id") public Venue venue;
    @Column(name = "booking_date", nullable = false) public LocalDate date;
    @Column(nullable = false) public int startHour;
    @org.hibernate.annotations.ColumnDefault("1")
    @Column(nullable = false) public int durationHours = 1;
    @Column(nullable = false) public int participants;
    @Column(nullable = false, length = 200) public String purpose;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) public Status status;
    @Column(nullable = false) public LocalDateTime createdAt;
    public LocalDateTime checkedInAt;
    protected Booking() {}
}
