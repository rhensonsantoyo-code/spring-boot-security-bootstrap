package habsida.spring.boot_security.demo.service;

import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.repository.RoleRepository;
import habsida.spring.boot_security.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public UserServiceImpl(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    @Override public List<User> findAll() { return userRepo.findAll(); }

    @Override public Optional<User> findById(Long id) { return userRepo.findById(id); }

    @Override public Optional<User> findByEmail(String email) { return userRepo.findByEmailIgnoreCase(email); }

    @Override
    public User save(User user) {
        // Validate bean (javax.validation)
        var violations = validator.validate(user);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);

        // Keep old password if blank on update; else encode
        if (user.getId() != null) {
            userRepo.findById(user.getId()).ifPresent(existing -> {
                if (user.getPassword() == null || user.getPassword().isBlank()) {
                    user.setPassword(existing.getPassword());
                } else {
                    user.setPassword(encoder.encode(user.getPassword()));
                }
            });
        } else {
            // new user must set password
            if (user.getPassword() == null || user.getPassword().isBlank())
                throw new IllegalArgumentException("Password is required");
            user.setPassword(encoder.encode(user.getPassword()));
        }

        // Map role names passed from form (may come as Role(name) only)
        // Ensure roles exist in DB
        if (user.getRoles() != null) {
            Set<Role> fixed = user.getRoles().stream()
                    .map(r -> ensureRole(r.getName()))
                    .collect(java.util.stream.Collectors.toSet());
            user.setRoles(fixed);
        }

        return userRepo.save(user);
    }

    @Override public void deleteById(Long id) { userRepo.deleteById(id); }

    @Override public boolean existsByEmailIgnoreCase(String email) {
        return userRepo.existsByEmailIgnoreCase(email);
    }

    @Override public List<Role> allRoles() { return roleRepo.findAll(); }

    @Override
    public Role ensureRole(String roleName) {
        return roleRepo.findByNameIgnoreCase(roleName)
                .orElseGet(() -> roleRepo.save(new Role(roleName)));
    }
}