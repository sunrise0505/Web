package edu.campus.venue;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VenueApplication {
    public static void main(String[] args) { SpringApplication.run(VenueApplication.class, args); }
    @Bean Clock clock() { return Clock.system(ZoneId.of("Asia/Shanghai")); }
}
