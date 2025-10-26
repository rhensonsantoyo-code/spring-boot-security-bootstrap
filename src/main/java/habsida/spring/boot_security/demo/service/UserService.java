package habsida.spring.boot_security.demo.service;

import habsida.spring.boot_security.demo.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    User save(User user); // <-- this fixes the missing method

    User update(User user);

    void deleteById(Long id);

    boolean existsByEmailIgnoreCase(String email);
}