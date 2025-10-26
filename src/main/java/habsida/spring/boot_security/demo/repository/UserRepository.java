package habsida.spring.boot_security.demo.repository;

import habsida.spring.boot_security.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (case-sensitive)
    Optional<User> findByEmail(String email);

    // Find user by email (case-insensitive)
    Optional<User> findByEmailIgnoreCase(String email);

    // Check existence (case-insensitive)
    boolean existsByEmailIgnoreCase(String email);
}