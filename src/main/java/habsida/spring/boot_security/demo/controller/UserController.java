package habsida.spring.boot_security.demo.controller;

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

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String list(Model model, @ModelAttribute("success") String successMsg) {
        model.addAttribute("users", userService.findAll());
        return "admin/users/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "admin/users/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        return userService.findById(id).map(u -> {
            u.syncRoleIdsFromRoles();
            model.addAttribute("user", u);
            model.addAttribute("allRoles", roleRepository.findAll());
            return "admin/users/form";
        }).orElseGet(() -> {
            redirect.addFlashAttribute("success", "User not found.");
            return "redirect:/admin/users";
        });
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("user") User user,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            return "admin/users/form";
        }

        try {
            userService.save(user);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() == null ? "Validation error" : ex.getMessage();
            String lower = msg.toLowerCase();
            if (lower.contains("username")) {
                result.addError(new FieldError("user", "username", msg));
            } else if (lower.contains("password")) {
                result.addError(new FieldError("user", "password", msg));
            } else {
                result.reject("globalError", msg);
            }
            model.addAttribute("allRoles", roleRepository.findAll());
            return "admin/users/form";
        }

        redirect.addFlashAttribute("success", "User saved successfully!");
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        userService.deleteById(id);
        redirect.addFlashAttribute("success", "User deleted.");
        return "redirect:/admin/users";
    }
}