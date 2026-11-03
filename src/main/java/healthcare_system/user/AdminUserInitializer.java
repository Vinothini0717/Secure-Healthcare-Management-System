
package healthcare_system.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminUserInitializer {

    @Bean
    public CommandLineRunner createAdminUser(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            String username = System.getenv("HEALTHCARE_ADMIN_USERNAME");
            String password = System.getenv("HEALTHCARE_ADMIN_PASSWORD");

            if (username == null || username.isBlank()
                    || password == null || password.isBlank()) {
                System.out.println(
                    "Admin account not created: configure admin environment variables.");
                return;
            }

            if (appUserRepository.findByUsername(username).isEmpty()) {
                AppUser admin = new AppUser(
                    username,
                    passwordEncoder.encode(password),
                    "ADMIN"
                );

                appUserRepository.save(admin);
                System.out.println("Configured admin account created.");
            } else {
                System.out.println("Configured admin account already exists.");
            }
        };
    }
}
