package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.EmployeeDTO;
import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.model.Project;
import com.example.pointage_backend.repository.EmployeeRepository;
import com.example.pointage_backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    // GET ALL
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // SAVE / UPDATE
    public EmployeeDTO saveEmployee(EmployeeDTO dto) {

        // If an affaireNumero (project number) was provided, ensure a Project exists and link it
        String projectId = null;
        if (dto.getAffaireNumero() != null && !dto.getAffaireNumero().isEmpty()) {
            Project project = projectRepository.findByAffaireNumero(dto.getAffaireNumero())
                .orElseGet(() -> {
                Project p = Project.builder()
                    .affaireNumero(dto.getAffaireNumero())
                    .name(dto.getAffaireNumero())
                    .plannedHours(null)
                    .build();
                return projectRepository.save(p);
                });
            projectId = project.getId();
        }

        Employee employee = Employee.builder()
                .id(dto.getId())
                .name(dto.getName())
                .matricule(dto.getMatricule())
            .affaireNumero(dto.getAffaireNumero())
            .projectId(projectId)
                .client(dto.getClient())
                .site(dto.getSite())
                .plannedHours(dto.getPlannedHours())
                .status(dto.getStatus())
                .pointageEntree(dto.getPointageEntree())
                .pointageSortie(dto.getPointageSortie())
                .supervisorId(dto.getSupervisorId())
                .responsableId(dto.getResponsableId())
                .totHrsTravaillees(dto.getTotHrsTravaillees())
                .nbrJrsTravaillees(dto.getNbrJrsTravaillees())
                .nbrJrsAbsence(dto.getNbrJrsAbsence())
                .totHrsDimanche(dto.getTotHrsDimanche())
                .nbrJrsFeries(dto.getNbrJrsFeries())
                .nbrJrsFeriesTravailes(dto.getNbrJrsFeriesTravailes())
                .nbrJrsConges(dto.getNbrJrsConges())
                .nbrJrsDeplacementsMaroc(dto.getNbrJrsDeplacementsMaroc())
                .nbrJrsPaniers(dto.getNbrJrsPaniers())
                .nbrJrsDetente(dto.getNbrJrsDetente())
                .nbrJrsDeplacementsExpatrie(dto.getNbrJrsDeplacementsExpatrie())
                .nbrJrsRecuperation(dto.getNbrJrsRecuperation())
                .nbrJrsMaladie(dto.getNbrJrsMaladie())
                .chantierAtelier(dto.getChantierAtelier())
                .build();

        Employee saved = employeeRepository.save(employee);
        return mapToDTO(saved);
    }

    // DELETE
    public void deleteEmployee(String id) {
        employeeRepository.deleteById(id);
    }

    // GET EMPLOYEES BY FILTER
    public List<EmployeeDTO> getEmployeesByFilter(String supervisorId, String responsableId) {
        List<Employee> employees;
        if (supervisorId != null && !supervisorId.isEmpty()) {
            employees = employeeRepository.findBySupervisorId(supervisorId);
        } else if (responsableId != null && !responsableId.isEmpty()) {
            employees = employeeRepository.findByResponsableId(responsableId);
        } else {
            employees = employeeRepository.findAll();
        }
        return employees.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // MAPPER
    private EmployeeDTO mapToDTO(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .matricule(employee.getMatricule())
                .affaireNumero(employee.getAffaireNumero())
                .projectId(employee.getProjectId())
                .client(employee.getClient())
                .site(employee.getSite())
                .plannedHours(employee.getPlannedHours())
                .status(employee.getStatus())
                .pointageEntree(employee.getPointageEntree())
                .pointageSortie(employee.getPointageSortie())
                .supervisorId(employee.getSupervisorId())
                .responsableId(employee.getResponsableId())
                .totHrsTravaillees(employee.getTotHrsTravaillees())
                .nbrJrsTravaillees(employee.getNbrJrsTravaillees())
                .nbrJrsAbsence(employee.getNbrJrsAbsence())
                .totHrsDimanche(employee.getTotHrsDimanche())
                .nbrJrsFeries(employee.getNbrJrsFeries())
                .nbrJrsFeriesTravailes(employee.getNbrJrsFeriesTravailes())
                .nbrJrsConges(employee.getNbrJrsConges())
                .nbrJrsDeplacementsMaroc(employee.getNbrJrsDeplacementsMaroc())
                .nbrJrsPaniers(employee.getNbrJrsPaniers())
                .nbrJrsDetente(employee.getNbrJrsDetente())
                .nbrJrsDeplacementsExpatrie(employee.getNbrJrsDeplacementsExpatrie())
                .nbrJrsRecuperation(employee.getNbrJrsRecuperation())
                .nbrJrsMaladie(employee.getNbrJrsMaladie())
                .chantierAtelier(employee.getChantierAtelier())
                .build();
    }
}