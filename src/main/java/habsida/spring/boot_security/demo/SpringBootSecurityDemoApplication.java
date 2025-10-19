package habsida.spring.boot_security.demo;

import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.repository.RoleRepository;
import habsida.spring.boot_security.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;

@SpringBootApplication
public class SpringBootSecurityDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootSecurityDemoApplication.class, args);
    }

    @Bean
    CommandLineRunner seed(RoleRepository roles, UserRepository users, PasswordEncoder encoder) {
        return args -> {
            Role admin = roles.findByName("ADMIN").orElseGet(() -> roles.save(new Role(null, "ADMIN")));
            Role userR = roles.findByName("USER").orElseGet(() -> roles.save(new Role(null, "USER")));

            if (users.findByUsername("admin").isEmpty()) {
                User a = new User();
                a.setUsername("admin");
                a.setPassword(encoder.encode("admin123"));
                a.setName("Admin");
                a.setEmail("admin@example.com");
                a.setRoles(new HashSet<>(Arrays.asList(admin, userR)));
                users.save(a);
            }
            if (users.findByUsername("john").isEmpty()) {
                User u = new User();
                u.setUsername("john");
                u.setPassword(encoder.encode("user123"));
                u.setName("John");
                u.setEmail("john@example.com");
                u.setRoles(new HashSet<>(Arrays.asList(userR)));
                users.save(u);
            }
        };
    }
}