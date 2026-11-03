
package healthcare_system.security;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import healthcare_system.audit.AuditLogService;
import healthcare_system.user.AppUserService;

@RestController
@RequestMapping("/api/auth/2fa")
public class TwoFactorController {

    private final TwoFactorService twoFactorService;
    private final AppUserService appUserService;
    private final AuditLogService auditLogService;

    public TwoFactorController(
            TwoFactorService twoFactorService,
            AppUserService appUserService,
            AuditLogService auditLogService) {
        this.twoFactorService = twoFactorService;
        this.appUserService = appUserService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/setup")
    public ResponseEntity<?> setup(Principal principal) {

        String username = principal.getName();

        String secret = twoFactorService.generateSecretKey();

        appUserService.saveTwoFactorSecret(username, secret);

        String account = URLEncoder.encode(
                username, StandardCharsets.UTF_8);

        String issuer = URLEncoder.encode(
                "HealthcareSystem", StandardCharsets.UTF_8);

        String uri = "otpauth://totp/" + issuer + ":" + account
                + "?secret=" + secret
                + "&issuer=" + issuer
                + "&algorithm=SHA1&digits=6&period=30";

        auditLogService.logAction(
                username, "TWO_FACTOR_SETUP_STARTED",
                "Two-factor setup started");

        return ResponseEntity.ok(Map.of(
                "message", "Add this account to your authenticator app",
                "secret", secret,
                "otpauthUri", uri
        ));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(
            @RequestBody ConfirmRequest request,
            Principal principal) {

        String username = principal.getName();

        if (request.code() == null
                || request.code() < 0
                || request.code() > 999999) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Enter a valid 6-digit code"));
        }

        String secret =
                appUserService.getDecryptedTwoFactorSecret(username);

        if (secret == null
                || !twoFactorService.verifyCode(secret, request.code())) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Invalid verification code"));
        }

        appUserService.enableTwoFactor(username);

        auditLogService.logAction(
                username, "TWO_FACTOR_ENABLED",
                "Two-factor authentication enabled");

        return ResponseEntity.ok(Map.of(
                "message", "Two-factor authentication enabled successfully"
        ));
    }

    public record ConfirmRequest(Integer code) {
    }
}
