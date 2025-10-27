package habsida.spring.boot_security.demo.controller;

import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final UserService users;

    public ProfileController(UserService users) { this.users = users; }

    @GetMapping({"/", "/user"})
    public String me(@AuthenticationPrincipal UserDetails principal, Model model) {
        if (principal != null) {
            User u = users.findByEmail(principal.getUsername()).orElse(null);
            model.addAttribute("user", u);
        }
        return "user/info";
    }
}