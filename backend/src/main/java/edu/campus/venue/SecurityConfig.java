package edu.campus.venue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean UserDetailsService userDetailsService(UserRepository users) {
        return username -> users.findByUsername(username).map(u -> User.withUsername(u.username)
                .password(u.passwordHash).roles(u.role).build()).orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    }
    @Bean SecurityFilterChain security(HttpSecurity http, ObjectMapper mapper) throws Exception {
        http.csrf(c -> c.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));
        http.authorizeHttpRequests(a -> a.requestMatchers("/auth/login", "/auth/csrf").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**", "/auth/me").authenticated().anyRequest().permitAll());
        http.formLogin(f -> f.loginProcessingUrl("/auth/login")
                .successHandler((req, res, auth) -> { res.setContentType("application/json;charset=UTF-8"); mapper.writeValue(res.getWriter(), Map.of("message", "登录成功")); })
                .failureHandler((req, res, e) -> { res.setStatus(401); res.setContentType("application/json;charset=UTF-8"); mapper.writeValue(res.getWriter(), Map.of("message", "账号或密码不正确")); }));
        http.logout(l -> l.logoutUrl("/auth/logout").invalidateHttpSession(true).deleteCookies("JSESSIONID")
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(204)));
        http.exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
            res.setStatus(HttpStatus.UNAUTHORIZED.value()); res.setContentType("application/json;charset=UTF-8"); mapper.writeValue(res.getWriter(), Map.of("message", "请先登录"));
        }).accessDeniedHandler((req, res, ex) -> {
            res.setStatus(403); res.setContentType("application/json;charset=UTF-8"); mapper.writeValue(res.getWriter(), Map.of("message", "无操作权限或会话已过期，请刷新后重试"));
        }));
        return http.build();
    }
}
