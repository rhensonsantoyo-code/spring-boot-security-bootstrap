package habsida.spring.boot_security.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

// Avoid name clash by fully-qualifying the annotation
@org.springframework.web.bind.annotation.RestController
public class RestController {

    @GetMapping("/rest/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/rest/me")
    public Map<String, Object> me(Authentication auth) {
        return Map.of("username", auth.getName(), "authorities", auth.getAuthorities());
    }
}