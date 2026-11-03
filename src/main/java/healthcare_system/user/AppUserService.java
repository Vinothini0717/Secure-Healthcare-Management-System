
package healthcare_system.user;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import healthcare_system.security.EncryptionService;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;

    public AppUserService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            EncryptionService encryptionService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.encryptionService = encryptionService;
    }

    public AppUser registerUser(
            String username, String rawPassword, String role) {

        if (appUserRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException(
                    "Username already exists");
        }

        AppUser user = new AppUser(
                username,
                passwordEncoder.encode(rawPassword),
                role
        );

        return appUserRepository.save(user);
    }

    public Optional<AppUser> findByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }

    public AppUser saveUser(AppUser user) {
        return appUserRepository.save(user);
    }

    public void saveTwoFactorSecret(String username, String secret) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        user.setTwoFactorSecret(encryptionService.encrypt(secret));
        appUserRepository.save(user);
    }

    public String getDecryptedTwoFactorSecret(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        String encryptedSecret = user.getTwoFactorSecret();

        if (encryptedSecret == null || encryptedSecret.isBlank()) {
            return null;
        }

        return encryptionService.decrypt(encryptedSecret);
    }

    public void enableTwoFactor(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        if (user.getTwoFactorSecret() == null
                || user.getTwoFactorSecret().isBlank()) {
            throw new IllegalStateException(
                    "Two-factor secret has not been configured");
        }

        user.setTwoFactorEnabled(true);
        appUserRepository.save(user);
    }
}
