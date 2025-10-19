package habsida.spring.boot_security.demo.service;

import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.repository.RoleRepository;
import habsida.spring.boot_security.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public UserServiceImpl(UserRepository repo, RoleRepository roleRepo, PasswordEncoder encoder) {
        this.repo = repo; this.roleRepo = roleRepo; this.encoder = encoder;
    }

    @Override @Transactional(readOnly = true)
    public List<User> findAll() { return repo.findAll(); }

    @Override @Transactional(readOnly = true)
    public Optional<User> findById(Long id) { return repo.findById(id); }

    @Override @Transactional
    public User save(User user) {
        // Duplicate username protection (allow same record on update)
        repo.findByUsername(user.getUsername()).ifPresent(existing -> {
            if (user.getId() == null || !existing.getId().equals(user.getId())) {
                throw new IllegalArgumentException("Username already exists");
            }
        });

        // Map roleIds -> Role entities
        if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
            Set<Role> selected = new HashSet<>(roleRepo.findAllById(user.getRoleIds()));
            user.setRoles(selected);
        }

        // Password rules: must not be empty on create; keep old on edit if blank
        if (user.getId() == null) {
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                throw new IllegalArgumentException("Password must not be empty");
            }
            user.setPassword(encoder.encode(user.getPassword()));
        } else {
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                user.setPassword(encoder.encode(user.getPassword()));
            } else {
                repo.findById(user.getId()).ifPresent(existing -> user.setPassword(existing.getPassword()));
            }
        }

        return repo.save(user);
    }

    @Override @Transactional
    public void deleteById(Long id) { repo.deleteById(id); }
}