package edu.campus.venue;

import java.security.Principal;
import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository users;
    public AuthController(UserRepository users) { this.users = users; }
    @GetMapping("/csrf") Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken(), "headerName", token.getHeaderName()); }
    @GetMapping("/me") ApiModels.UserView me(Principal p) { return ApiModels.UserView.from(users.findByUsername(p.getName()).orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "请重新登录"))); }
}
