package habsida.spring.boot_security.demo.configs;

import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.repository.RoleRepository;
import habsida.spring.boot_security.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Seeds required roles and a couple of users on startup (idempotent).
 * - Roles: ROLE_ADMIN, ROLE_USER
 * - Users:
 * admin@admin / admin -> ROLE_ADMIN
 * user@user / user -> ROLE_USER
 *
 * Requires:
 * - Role has a constructor Role(String name)
 * - RoleRepository: Optional<Role> findByNameIgnoreCase(String name)
 * - UserRepository: Optional<User> findByEmailIgnoreCase(String email)
 */
@Component
@Transactional
public class DataInitializer {

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(RoleRepository roleRepo,
                           UserRepository userRepo,
                           PasswordEncoder passwordEncoder) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        // ---- Ensure roles exist ----
        Role adminRole = roleRepo.findByNameIgnoreCase("ROLE_ADMIN")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_ADMIN")));

        Role userRole = roleRepo.findByNameIgnoreCase("ROLE_USER")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_USER")));

        // ---- Seed admin user if missing ----
        userRepo.findByEmailIgnoreCase("admin@admin").orElseGet(() -> {
            User u = new User();
            u.setFirstName("Admin");
            u.setLastName("Admin");
            u.setAge(28);
            u.setEmail("admin@admin");
            u.setPassword(passwordEncoder.encode("admin"));
            u.setRoles(new HashSet<>(Arrays.asList(adminRole))); // admin only
            return userRepo.save(u);
        });

        // ---- Seed regular user if missing ----
        userRepo.findByEmailIgnoreCase("user@user").orElseGet(() -> {
            User u = new User();
            u.setFirstName("User");
            u.setLastName("User");
            u.setAge(38);
            u.setEmail("user@user");
            u.setPassword(passwordEncoder.encode("user"));
            u.setRoles(new HashSet<>(Arrays.asList(userRole))); // user only
            return userRepo.save(u);
        });
    }
}