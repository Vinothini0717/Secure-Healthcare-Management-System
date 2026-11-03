
package healthcare_system.user;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import healthcare_system.audit.AuditLogService;
import healthcare_system.security.JwtService;
import healthcare_system.security.TwoFactorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserService appUserService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final TwoFactorService twoFactorService;

    public AuthController(
            AppUserService appUserService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AuditLogService auditLogService,
            TwoFactorService twoFactorService) {
        this.appUserService = appUserService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
        this.twoFactorService = twoFactorService;
    }

    public record RegisterRequest(
            @NotBlank String username,
            @NotBlank String password) {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password,
            @Min(0) @Max(999999) Integer twoFactorCode) {
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {
        try {
            AppUser user = appUserService.registerUser(
                    request.username(), request.password(), "PATIENT");

            auditLogService.logAction(
                    user.getUsername(),
                    "USER_REGISTERED",
                    "Patient account registered successfully");

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "User registered successfully",
                            "username", user.getUsername(),
                            "role", user.getRole()));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", exception.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(), request.password()));

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        AppUser user = appUserService
                .findByUsername(userDetails.getUsername())
                .orElseThrow();

        if (user.isTwoFactorEnabled()) {

            if (request.twoFactorCode() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "message",
                                "Two-factor verification code is required"));
            }

            String secret =
                    appUserService.getDecryptedTwoFactorSecret(
                            userDetails.getUsername());

            if (secret == null || !twoFactorService.verifyCode(
                    secret, request.twoFactorCode())) {
                auditLogService.logAction(
                        userDetails.getUsername(),
                        "TWO_FACTOR_LOGIN_FAILED",
                        "Invalid or missing two-factor code");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "message",
                                "Invalid two-factor verification code"));
            }
        }

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .orElseThrow()
                .getAuthority()
                .replace("ROLE_", "");

        String token = jwtService.generateToken(
                userDetails.getUsername(), role);

        auditLogService.logAction(
                userDetails.getUsername(),
                "LOGIN_SUCCESS",
                "User logged in successfully");

        return ResponseEntity.ok(Map.of(
                "token", token,
                "tokenType", "Bearer",
                "expiresInMinutes", 30));
    }
}
