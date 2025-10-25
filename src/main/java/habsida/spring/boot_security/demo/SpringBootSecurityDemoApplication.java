package habsida.spring.boot_security.demo;

import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.repository.RoleRepository;
import habsida.spring.boot_security.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;

import java.util.Set;

@SpringBootApplication
public class SpringBootSecurityDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootSecurityDemoApplication.class, args);
    }

    @Bean
    CommandLineRunner seed(RoleRepository roleRepo,
                           UserRepository userRepo,
                           PasswordEncoder encoder) {
        return args -> {
            Role admin = roleRepo.findByName("ADMIN").orElseGet(() -> roleRepo.save(new Role("ADMIN")));
            Role userR = roleRepo.findByName("USER").orElseGet(() -> roleRepo.save(new Role("USER")));

            if (userRepo.findByUsername("admin").isEmpty()) {
                User adminU = new User();
                adminU.setUsername("admin");
                adminU.setPassword(encoder.encode("admin123"));
                adminU.setName("Admin");
                adminU.setEmail("admin@example.com");
                adminU.setRoles(Set.of(admin, userR));
                userRepo.save(adminU);
            }

            if (userRepo.findByUsername("john").isEmpty()) {
                User john = new User();
                john.setUsername("john");
                john.setPassword(encoder.encode("user123"));
                john.setName("John");
                john.setEmail("john@example.com");
                john.setRoles(Set.of(userR));
                userRepo.save(john);
            }
        };
    }
}