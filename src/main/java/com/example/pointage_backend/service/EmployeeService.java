package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.EmployeeDTO;
import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.model.Project;
import com.example.pointage_backend.repository.EmployeeRepository;
import com.example.pointage_backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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

        Employee existing = null;
        if (isNotBlank(dto.getId())) {
            existing = employeeRepository.findById(dto.getId()).orElse(null);
        }

        String normalizedAffaireNumero = normalizeAffaireNumero(dto.getAffaireNumero());
        if (dto.getAffaireNumero() == null && existing != null) {
            normalizedAffaireNumero = normalizeAffaireNumero(existing.getAffaireNumero());
        }

        // Project linking strategy:
        // 1) If client sent projectId, keep it (prevents accidental relinking when affaireNumero is duplicated)
        // 2) Else, if we're updating and the employee already has a projectId and affaireNumero didn't change, keep it
        // 3) Else, resolve by affaireNumero (create if missing)
        String projectId = trimToNull(dto.getProjectId());
        if (projectId == null && existing != null) {
            String existingProjectId = trimToNull(existing.getProjectId());
            String existingAffaireNumero = normalizeAffaireNumero(existing.getAffaireNumero());
            if (existingProjectId != null && Objects.equals(existingAffaireNumero, normalizedAffaireNumero)) {
                projectId = existingProjectId;
            }
        }

        if (projectId == null && normalizedAffaireNumero != null) {
            List<Project> matches = projectRepository.findByAffaireNumero(normalizedAffaireNumero);
            if (matches.isEmpty()) {
                Project p = Project.builder()
                        .affaireNumero(normalizedAffaireNumero)
                        .name(normalizedAffaireNumero)
                        .plannedHours(null)
                        .build();
                projectId = projectRepository.save(p).getId();
            } else if (matches.size() == 1) {
                projectId = matches.get(0).getId();
            } else if (existing != null && isNotBlank(existing.getProjectId())) {
                // Prefer keeping the existing employee project if it is one of the matches
                String existingProjectId = trimToNull(existing.getProjectId());
                projectId = matches.stream()
                        .map(Project::getId)
                        .filter(id -> Objects.equals(id, existingProjectId))
                        .findFirst()
                        .orElse(null);
            }

            if (projectId == null && !matches.isEmpty()) {
                // deterministic fallback (still ambiguous, but prevents random "first" selection)
                projectId = matches.stream()
                        .filter(p -> isNotBlank(p.getId()))
                        .sorted(Comparator.comparing(Project::getId))
                        .map(Project::getId)
                        .findFirst()
                        .orElse(null);
            }
        }

        Employee employee = Employee.builder()
                .id(dto.getId())
                .name(dto.getName())
                .matricule(dto.getMatricule())
                .affaireNumero(normalizedAffaireNumero)
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
                .projectProgress(dto.getProjectProgress())
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

    // GET BY MATRICULE (for Employee self-service page)
    public List<EmployeeDTO> getEmployeesByMatricule(String matricule) {
        if (matricule == null || matricule.isEmpty()) {
            return List.of();
        }
        return employeeRepository.findByMatricule(matricule).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // GET BY NAME (for employee auth by name instead of matricule)
    public List<EmployeeDTO> getEmployeesByName(String name) {
        if (name == null || name.isEmpty()) {
            return List.of();
        }
        return employeeRepository.findByName(name).stream()
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
                .projectProgress(employee.getProjectProgress())
                .build();
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeAffaireNumero(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) return null;
        // UI sometimes uses "-" as empty placeholder
        if ("-".equals(trimmed)) return null;
        return trimmed;
    }
}
