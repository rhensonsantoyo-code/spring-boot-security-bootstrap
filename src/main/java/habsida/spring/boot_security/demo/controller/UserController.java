package habsida.spring.boot_security.demo.controller;

import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.repository.RoleRepository;
import habsida.spring.boot_security.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    public UserController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    /* ------------------ helpers for all views ------------------ */

    /** Available roles for the form (ADMIN/USER). */
    @ModelAttribute("allRoles")
    public List<String> allRoles() {
        return roleRepository.findAll()
                .stream()
                .map(Role::getName) // e.g. ROLE_ADMIN, ROLE_USER
                .collect(Collectors.toList());
    }

    /* ------------------ list page ------------------ */

    @GetMapping
    public String list(Model model,
                       @ModelAttribute("success") String success,
                       @ModelAttribute("error") String error) {
        model.addAttribute("users", userService.findAll());
        return "admin/users/list";
    }

    /* ------------------ create (GET/POST) ------------------ */

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User()); // empty bean for the form
        return "admin/users/user"; // render create/edit form
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("user") User user,
                         BindingResult binding,
                         RedirectAttributes ra,
                         Model model) {
        // unique email validation example (optional)
        if (user.getId() == null && userService.existsByEmailIgnoreCase(user.getEmail())) {
            binding.rejectValue("email", "email.exists", "Email already exists");
        }

        if (binding.hasErrors()) {
            // on error we must keep allRoles + user in model, view back to form
            return "admin/users/user";
        }

        try {
            // Service will encode password & keep old if blank (for updates)
            userService.save(user);
            ra.addFlashAttribute("success", "User created successfully.");
            return "redirect:/admin/users";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "admin/users/user";
        }
    }

    /* ------------------ edit (GET/POST) ------------------ */

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return userService.findById(id)
                .map(u -> {
                    model.addAttribute("user", u);
                    return "admin/users/user";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "User not found");
                    return "redirect:/admin/users";
                });
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("user") User form,
                         BindingResult binding,
                         RedirectAttributes ra,
                         Model model) {

        // protect ID
        form.setId(id);

        // prevent email duplication (optional)
        userService.findByEmail(form.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                binding.rejectValue("email", "email.exists", "Email already exists");
            }
        });

        if (binding.hasErrors()) {
            return "admin/users/user";
        }

        try {
            userService.save(form); // service handles blank password (= keep old)
            ra.addFlashAttribute("success", "User updated successfully.");
            return "redirect:/admin/users";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "admin/users/user";
        }
    }

    /* ------------------ delete ------------------ */

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.deleteById(id);
            ra.addFlashAttribute("success", "User deleted.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Delete failed: " + ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    /* ------------------ roles binding from form ------------------ */

    /**
     * If your form posts role names (e.g. ROLE_ADMIN) into user.roles,
     * make sure your User entity has a setter that accepts Set<Role>.
     * If you post plain strings, you can map them here, e.g.:
     *
     * @InitBinder
     * protected void initBinder(WebDataBinder binder) {
     * binder.registerCustomEditor(Set.class, "roles", new CustomCollectionEditor(Set.class) {
     * protected Object convertElement(Object element) {
     * if (element == null) return null;
     * String name = element.toString();
     * return roleRepository.findByNameIgnoreCase(name).orElse(new Role(name));
     * }
     * });
     * }
     *
     * In most cases, if your form uses <option th:value="${role}">ROLE_USER</option>,
     * Spring will bind to Set<String>. Then convert inside service before saving.
     */
}