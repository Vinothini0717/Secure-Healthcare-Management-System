
package healthcare_system;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/test")
    public Map<String, String> testAdminAccess() {
        return Map.of(
            "message", "Admin access successful",
            "role", "ADMIN"
        );
    }
}
