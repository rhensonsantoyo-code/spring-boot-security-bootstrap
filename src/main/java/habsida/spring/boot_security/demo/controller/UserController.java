package habsida.spring.boot_security.demo.controller;

import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public String list(Model model,
                       @RequestParam(value = "success", required = false) String success,
                       @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("users", users.findAll());
        model.addAttribute("success", success);
        model.addAttribute("error", error);
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", users.allRoles());
        return "admin/users/add";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("user") User user,
                         BindingResult br,
                         @RequestParam(value = "roleNames", required = false) List<String> roleNames,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("roles", users.allRoles());
            return "admin/users/add";
        }
        try {
            if (roleNames != null) {
                user.setRoles(roleNames.stream()
                        .map(users::ensureRole)
                        .collect(Collectors.toSet()));
            }
            users.save(user);
            return "redirect:/admin/users?success=User%20created";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("roles", users.allRoles());
            return "admin/users/add";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User u = users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("user", u);
        model.addAttribute("roles", users.allRoles());
        return "admin/users/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("user") User user,
                         BindingResult br,
                         @RequestParam(value = "roleNames", required = false) List<String> roleNames,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("roles", users.allRoles());
            return "admin/users/edit";
        }
        try {
            user.setId(id);
            if (roleNames != null) {
                user.setRoles(roleNames.stream()
                        .map(users::ensureRole)
                        .collect(Collectors.toSet()));
            }
            users.save(user);
            return "redirect:/admin/users?success=User%20updated";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("roles", users.allRoles());
            return "admin/users/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        users.deleteById(id);
        return "redirect:/admin/users?success=User%20deleted";
    }
}