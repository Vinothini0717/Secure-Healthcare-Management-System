package healthcare_system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import healthcare_system.patient.Patient;
import healthcare_system.patient.PatientRepository;
import healthcare_system.patient.PatientService;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setName("Test Patient");
        patient.setEmail("test@example.com");
        patient.setPhone("9876543210");
    }

    @Test
    void savePatientShouldReturnSavedPatient() {

        when(patientRepository.save(patient)).thenReturn(patient);

        Patient result = patientService.savePatient(patient);

        assertNotNull(result);
        assertEquals("Test Patient", result.getName());

        verify(patientRepository).save(patient);
    }

    @Test
    void getPatientByIdShouldReturnPatient() {

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        Patient result = patientService.getPatientById(1L);

        assertNotNull(result);
        assertEquals("Test Patient", result.getName());

        verify(patientRepository).findById(1L);
    }
}