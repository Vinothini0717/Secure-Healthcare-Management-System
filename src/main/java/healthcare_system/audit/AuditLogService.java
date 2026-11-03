
package healthcare_system.audit;

import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(String username, String action, String details) {
        AuditLog auditLog = new AuditLog(username, action, details);
        auditLogRepository.save(auditLog);
    }
}
