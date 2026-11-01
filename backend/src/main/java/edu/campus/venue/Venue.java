package edu.campus.venue;

import jakarta.persistence.*;

@Entity
@Table(name = "venue")
public class Venue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, length = 60) public String name;
    @Column(nullable = false, length = 20) public String category;
    @org.hibernate.annotations.ColumnDefault("'中心校区'")
    @Column(nullable = false, length = 20) public String campus = "中心校区";
    @Column(nullable = false, length = 100) public String location;
    @Column(nullable = false, length = 500) public String description;
    @Column(nullable = false) public int capacity;
    @Column(nullable = false) public int openHour;
    @Column(nullable = false) public int closeHour;
    @Column(nullable = false) public boolean enabled;
    @Column(nullable = false, length = 20) public String theme;
    protected Venue() {}
    Venue(String name, String category, String location, String description, int capacity, int openHour, int closeHour, String theme) {
        this.campus = location.contains("校区") ? location.substring(0, location.indexOf("校区") + 2) : "中心校区";
        this.name = name; this.category = category; this.location = location; this.description = description;
        this.capacity = capacity; this.openHour = openHour; this.closeHour = closeHour; this.enabled = true; this.theme = theme;
    }
}
