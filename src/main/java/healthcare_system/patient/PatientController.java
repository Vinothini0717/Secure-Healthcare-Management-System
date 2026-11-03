package healthcare_system.patient;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import healthcare_system.audit.AuditLogService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final AuditLogService auditLogService;

    public PatientController(
            PatientService patientService,
            AuditLogService auditLogService) {
        this.patientService = patientService;
        this.auditLogService = auditLogService;
    }

    private String getCurrentUsername() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication.getName();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Patient createPatient(@Valid @RequestBody Patient patient) {

        Patient savedPatient = patientService.savePatient(patient);

        auditLogService.logAction(
                getCurrentUsername(),
                "PATIENT_CREATED",
                "Patient record created. ID: " + savedPatient.getId());

        return savedPatient;
    }

    @GetMapping
    public List<Patient> getAllPatients() {

        List<Patient> patients = patientService.getAllPatients();

        auditLogService.logAction(
                getCurrentUsername(),
                "PATIENT_LIST_VIEWED",
                "Patient records list accessed");

        return patients;
    }

    @GetMapping("/{id}")
    public Patient getPatientById(@PathVariable Long id) {

        Patient patient = patientService.getPatientById(id);

        auditLogService.logAction(
                getCurrentUsername(),
                "PATIENT_VIEWED",
                "Patient record accessed. ID: " + id);

        return patient;
    }

    @PutMapping("/{id}")
    public Patient updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody Patient updatedPatient) {

        Patient patient = patientService.updatePatient(id, updatedPatient);

        auditLogService.logAction(
                getCurrentUsername(),
                "PATIENT_UPDATED",
                "Patient record updated. ID: " + id);

        return patient;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatient(@PathVariable Long id) {

        patientService.deletePatient(id);

        auditLogService.logAction(
                getCurrentUsername(),
                "PATIENT_DELETED",
                "Patient record deleted. ID: " + id);
    }
}