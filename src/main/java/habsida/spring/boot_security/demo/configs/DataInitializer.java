package habsida.spring.boot_security.demo.configs;

import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.repository.UserRepository;
import habsida.spring.boot_security.demo.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

@Component
public class DataInitializer {

    private final UserService userService;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(UserService userService, UserRepository userRepo, PasswordEncoder encoder) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @PostConstruct
    public void init() {
        // Ensure roles
        Role adminRole = userService.ensureRole("ROLE_ADMIN");
        Role userRole = userService.ensureRole("ROLE_USER");

        // Seed admin if missing
        userRepo.findByEmailIgnoreCase("admin@admin").orElseGet(() -> {
            User u = new User();
            u.setEmail("admin@admin");
            u.setFirstName("Admin");
            u.setLastName("Admin");
            u.setAge(28);
            u.setPassword(encoder.encode("admin"));
            u.setRoles(Set.of(adminRole));
            return userRepo.save(u);
        });

        // Seed normal user
        userRepo.findByEmailIgnoreCase("user@user").orElseGet(() -> {
            User u = new User();
            u.setEmail("user@user");
            u.setFirstName("User");
            u.setLastName("User");
            u.setAge(38);
            u.setPassword(encoder.encode("user"));
            u.setRoles(Set.of(userRole));
            return userRepo.save(u);
        });
    }
}