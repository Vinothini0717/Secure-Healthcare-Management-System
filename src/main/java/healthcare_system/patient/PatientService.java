package healthcare_system.patient;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import healthcare_system.exception.PatientNotFoundException;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @CacheEvict(value = {"patients", "patient"}, allEntries = true)
    public Patient savePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @Cacheable(value = "patients")
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Cacheable(value = "patient", key = "#id")
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() ->
                        new PatientNotFoundException(
                                "Patient not found with ID: " + id));
    }

    @CacheEvict(value = {"patients", "patient"}, allEntries = true)
    public Patient updatePatient(Long id, Patient updatedPatient) {

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new PatientNotFoundException(
                                "Patient not found with ID: " + id));

        existingPatient.setName(updatedPatient.getName());
        existingPatient.setEmail(updatedPatient.getEmail());
        existingPatient.setPhone(updatedPatient.getPhone());

        return patientRepository.save(existingPatient);
    }

    @CacheEvict(value = {"patients", "patient"}, allEntries = true)
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException(
                    "Patient not found with ID: " + id);
        }

        patientRepository.deleteById(id);
    }
}