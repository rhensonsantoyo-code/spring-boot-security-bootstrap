package habsida.spring.boot_security.demo.controller;

import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public String profile(@AuthenticationPrincipal User loggedInUser, Model model) {
        User user = userService.findByEmail(loggedInUser.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        model.addAttribute("user", user);
        return "user";
    }
}