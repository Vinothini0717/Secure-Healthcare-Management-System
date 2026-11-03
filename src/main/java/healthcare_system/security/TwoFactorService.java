
package healthcare_system.security;

import org.springframework.stereotype.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Service
public class TwoFactorService {

    private final GoogleAuthenticator googleAuthenticator;

    public TwoFactorService() {
        this.googleAuthenticator = new GoogleAuthenticator();
    }

    public String generateSecretKey() {
        GoogleAuthenticatorKey key =
                googleAuthenticator.createCredentials();

        return key.getKey();
    }

    public boolean verifyCode(String secretKey, int verificationCode) {
        if (secretKey == null || secretKey.isBlank()) {
            return false;
        }

        if (verificationCode < 0 || verificationCode > 999999) {
            return false;
        }

        return googleAuthenticator.authorize(
                secretKey, verificationCode);
    }
}

