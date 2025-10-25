package habsida.spring.boot_security.demo.controller;

import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.repository.RoleRepository;
import habsida.spring.boot_security.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    // simple view model for the table
    public static class Row {
        public Long id;
        public String username;
        public String name;
        public String email;
        public Set<Role> roles;
        public String rolesCsv; // e.g. "1,2"

        public Row(User u) {
            this.id = u.getId();
            this.username = u.getUsername();
            this.name = u.getName();
            this.email = u.getEmail();
            this.roles = u.getRoles();
            this.rolesCsv = u.getRoles() == null ? "" :
                    u.getRoles().stream()
                            .map(r -> String.valueOf(r.getId()))
                            .collect(Collectors.joining(","));
        }
    }

    public UserController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    // ---------- helpers ----------
    private void buildListPageModel(Model model) {
        List<User> users = userService.findAll();
        List<Row> rows = users.stream().map(Row::new).collect(Collectors.toList());
        model.addAttribute("rows", rows);                 // used by the table
        model.addAttribute("users", users);               // optional if you still reference it
        model.addAttribute("newUser", new User());        // for the "New User" tab
        model.addAttribute("allRoles", roleRepository.findAll());
    }

    // ---------- pages ----------
    @GetMapping
    public String adminPanel(Model model, @ModelAttribute("success") String success) {
        buildListPageModel(model);
        return "admin/users/list";
    }

    // ---------- actions ----------
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("newUser") User newUser,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            buildListPageModel(model);
            return "admin/users/list";
        }
        try {
            userService.save(newUser);
        } catch (IllegalArgumentException ex) {
            result.addError(new FieldError("newUser", "username", ex.getMessage()));
            buildListPageModel(model);
            return "admin/users/list";
        }
        redirect.addFlashAttribute("success", "User created");
        return "redirect:/admin/users";
    }

    @PostMapping("/save")
    public String saveFromModal(@Valid @ModelAttribute("user") User user,
                                BindingResult result,
                                RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("success", "Validation error");
            return "redirect:/admin/users";
        }
        try {
            userService.save(user);
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("success", ex.getMessage());
            return "redirect:/admin/users";
        }
        redirect.addFlashAttribute("success", "User saved");
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        userService.deleteById(id);
        redirect.addFlashAttribute("success", "User deleted");
        return "redirect:/admin/users";
    }
}