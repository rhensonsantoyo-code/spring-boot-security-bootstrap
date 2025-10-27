package habsida.spring.boot_security.demo.service;

import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    User save(User user); // create or update; keep password if blank
    void deleteById(Long id);
    boolean existsByEmailIgnoreCase(String email);

    List<Role> allRoles(); // [ROLE_ADMIN, ROLE_USER]
    Role ensureRole(String roleName);
}