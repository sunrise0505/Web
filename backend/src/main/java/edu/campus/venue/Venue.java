package edu.campus.venue;

import jakarta.persistence.*;

@Entity
@Table(name = "venue")
public class Venue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, length = 60) public String name;
    @Column(nullable = false, length = 20) public String category;
    @Column(nullable = false, length = 100) public String location;
    @Column(nullable = false, length = 500) public String description;
    @Column(nullable = false) public int capacity;
    @Column(nullable = false) public int openHour;
    @Column(nullable = false) public int closeHour;
    @Column(nullable = false) public boolean enabled;
    @Column(nullable = false, length = 20) public String theme;
    protected Venue() {}
    Venue(String name, String category, String location, String description, int capacity, int openHour, int closeHour, String theme) {
        this.name = name; this.category = category; this.location = location; this.description = description;
        this.capacity = capacity; this.openHour = openHour; this.closeHour = closeHour; this.enabled = true; this.theme = theme;
    }
}
