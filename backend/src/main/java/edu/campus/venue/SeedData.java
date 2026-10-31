package edu.campus.venue;

import java.time.*;
import java.util.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.seed-enabled", havingValue = "true")
public class SeedData implements CommandLineRunner {
    private final UserRepository users; private final VenueRepository venues; private final BookingRepository bookings;
    private final PasswordEncoder encoder; private final Clock clock; private final Environment env;
    public SeedData(UserRepository users, VenueRepository venues, BookingRepository bookings, PasswordEncoder encoder, Clock clock, Environment env) {
        this.users = users; this.venues = venues; this.bookings = bookings; this.encoder = encoder; this.clock = clock; this.env = env;
    }
    @Override @Transactional public void run(String... args) {
        if (users.count() == 0) {
            users.save(new AppUser("admin", encoder.encode(env.getProperty("ADMIN_PASSWORD", "Admin123!")), "场馆管理员", "ADMIN"));
            users.save(new AppUser("student", encoder.encode(env.getProperty("STUDENT_PASSWORD", "Student123!")), "林同学", "STUDENT"));
            users.save(new AppUser("student2", encoder.encode(env.getProperty("STUDENT_PASSWORD", "Student123!")), "陈同学", "STUDENT"));
        }
        if (venues.count() > 0) return;
        List<Venue> items = venues.saveAll(List.of(
            new Venue("综合体育馆 · 羽毛球馆", "羽毛球", "中心校区 · 体育馆 1 层", "标准室内羽毛球场，运动木地板与专业照明。请穿着室内运动鞋入场，自备球拍。", 4, 8, 22, "court"),
            new Venue("东区篮球馆", "篮球", "中心校区 · 东区运动中心", "全场木地板篮球场，提供计分牌与更衣空间。适合班级友谊赛和日常训练。", 20, 8, 22, "court"),
            new Venue("恒温游泳馆", "游泳", "中心校区 · 游泳中心", "室内恒温泳池，配备救生人员与独立更衣室。请携带泳帽、泳镜与有效校园证件。", 30, 9, 21, "pool"),
            new Venue("活力健身房", "健身", "中心校区 · 体育馆 2 层", "有氧与力量训练区域，适合个人训练和小组健身。器械使用后请及时归位。", 12, 8, 22, "studio"),
            new Venue("南区足球场", "足球", "中心校区 · 南区田径场", "室外标准足球场，夜间照明开放。遇恶劣天气可能临时关闭，请留意场馆状态。", 22, 8, 21, "field"),
            new Venue("西区网球场", "网球", "中心校区 · 西区运动场", "室外硬地网球场，适合单打与双打。请自备球具，按照预约时段使用。", 4, 8, 21, "field")
        ));
        // 历史演示记录只在本地 demo profile 下生成，不写入课程数据库。
        boolean demo = env.getActiveProfiles().length == 0 || Arrays.asList(env.getActiveProfiles()).contains("demo");
        if (!demo) return;
        AppUser student = users.findByUsername("student").orElseThrow();
        AppUser other = users.findByUsername("student2").orElseThrow();
        LocalDate today = LocalDate.now(clock);
        for (int day = 6; day >= 1; day--) {
            for (int n = 0; n < (day % 3) + 1; n++) {
                seed(items.get(n), n % 2 == 0 ? student : other, today.minusDays(day), 16, Booking.Status.CHECKED_IN, "日常运动训练");
            }
        }
        seed(items.get(0), student, today.plusDays(1), 18, Booking.Status.APPROVED, "羽毛球双打练习");
        seed(items.get(1), other, today.plusDays(1), 19, Booking.Status.PENDING, "班级篮球友谊赛");
        seed(items.get(3), student, today.plusDays(2), 16, Booking.Status.PENDING, "小组力量训练");
    }
    private void seed(Venue v, AppUser u, LocalDate day, int hour, Booking.Status status, String purpose) {
        Booking b = new Booking(); b.venue = v; b.user = u; b.date = day; b.startHour = hour; b.participants = Math.min(4, v.capacity);
        b.status = status; b.purpose = purpose;
        LocalDateTime planned = day.minusDays(1).atTime(12, 0), now = LocalDateTime.now(clock);
        b.createdAt = planned.isBefore(now) ? planned : now.minusHours(1);
        if (status == Booking.Status.CHECKED_IN) b.checkedInAt = day.atTime(hour, 0);
        bookings.save(b);
    }
}
