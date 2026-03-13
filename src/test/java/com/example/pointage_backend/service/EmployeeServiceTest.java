package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.EmployeeDTO;
import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.repository.EmployeeRepository;
import com.example.pointage_backend.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Captor
    private ArgumentCaptor<Employee> employeeCaptor;

    @Test
    void saveEmployee_keepsProvidedProjectId_evenWhenAffaireNumeroDuplicated() {
        Employee existing = Employee.builder()
                .id("e1")
                .name("Alice")
                .matricule("M1")
                .affaireNumero("A-100")
                .projectId("p2")
                .supervisorId("s1")
                .build();

        when(employeeRepository.findById("e1")).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeDTO dto = EmployeeDTO.builder()
                .id("e1")
                .name("Alice")
                .matricule("M1")
                .affaireNumero("A-100")
                .projectId("p2")
                .supervisorId(null)
                .build();

        EmployeeDTO saved = employeeService.saveEmployee(dto);

        verify(employeeRepository).save(employeeCaptor.capture());
        verifyNoInteractions(projectRepository);

        assertThat(employeeCaptor.getValue().getProjectId()).isEqualTo("p2");
        assertThat(saved.getProjectId()).isEqualTo("p2");
        assertThat(saved.getSupervisorId()).isNull();
    }

    @Test
    void saveEmployee_whenAffaireNumeroMissing_preservesExistingAffaireAndProjectLink() {
        Employee existing = Employee.builder()
                .id("e1")
                .name("Alice")
                .matricule("M1")
                .affaireNumero("A-100")
                .projectId("p2")
                .supervisorId("s1")
                .build();

        when(employeeRepository.findById("e1")).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeDTO dto = EmployeeDTO.builder()
                .id("e1")
                .name("Alice")
                .matricule("M1")
                // affaireNumero omitted (null) on purpose
                .projectId(null)
                .supervisorId("s1")
                .build();

        EmployeeDTO saved = employeeService.saveEmployee(dto);

        verify(employeeRepository).save(employeeCaptor.capture());
        verifyNoInteractions(projectRepository);

        assertThat(employeeCaptor.getValue().getAffaireNumero()).isEqualTo("A-100");
        assertThat(employeeCaptor.getValue().getProjectId()).isEqualTo("p2");
        assertThat(saved.getAffaireNumero()).isEqualTo("A-100");
        assertThat(saved.getProjectId()).isEqualTo("p2");
    }
}

