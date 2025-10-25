package habsida.spring.boot_security.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import habsida.spring.boot_security.demo.model.User;

@Controller
public class ProfileController {

    @GetMapping("/user")
    public String userHome(@AuthenticationPrincipal User current, Model model) {
        model.addAttribute("user", current);
        return "user"; // templates/user.html
    }
}