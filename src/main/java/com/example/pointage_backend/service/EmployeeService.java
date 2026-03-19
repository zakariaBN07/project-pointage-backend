package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.EmployeeDTO;
import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.model.Project;
import com.example.pointage_backend.repository.EmployeeRepository;
import com.example.pointage_backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        if (dto.getId() != null) {
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
        Long projectId = dto.getProjectId();
        if (projectId == null && existing != null) {
            Long existingProjectId = existing.getProjectId();
            String existingAffaireNumero = normalizeAffaireNumero(existing.getAffaireNumero());
            if (existingProjectId != null && Objects.equals(existingAffaireNumero, normalizedAffaireNumero)) {
                projectId = existingProjectId;
            }
        }

        if (projectId == null && normalizedAffaireNumero != null) {
            java.util.Optional<Project> match = projectRepository.findByCodeAffaire(normalizedAffaireNumero);
            if (match.isEmpty()) {
                Project p = Project.builder()
                        .codeAffaire(normalizedAffaireNumero)
                        .nomAffaire(normalizedAffaireNumero)
                        .heuresEstimees(java.math.BigDecimal.ZERO)
                        .build();
                projectId = projectRepository.save(p).getId();
            } else {
                projectId = match.get().getId();
            }
        }

        Employee employee = Employee.builder()
                .id(dto.getId())
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .matricule(dto.getMatricule())
                .email(dto.getEmail())
                .post(dto.getPost())
                .departement(dto.getDepartement())
                .tauxHoraire(dto.getTauxHoraire())
                .deviseTaux(dto.getDeviseTaux())
                .actif(dto.getActif())
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
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    // GET EMPLOYEES BY FILTER
    public List<EmployeeDTO> getEmployeesByFilter(Long supervisorId, Long responsableId) {
        List<Employee> employees;
        if (supervisorId != null) {
            employees = employeeRepository.findBySupervisorId(supervisorId);
        } else if (responsableId != null) {
            employees = employeeRepository.findByResponsableId(responsableId);
        } else {
            employees = employeeRepository.findAll();
        }
        return employees.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // GET BY EMAIL (for Employee self-service page)
    public List<EmployeeDTO> getEmployeesByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return List.of();
        }
        return employeeRepository.findByEmail(email).stream()
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
                .nom(employee.getNom())
                .prenom(employee.getPrenom())
                .matricule(employee.getMatricule())
                .email(employee.getEmail())
                .post(employee.getPost())
                .departement(employee.getDepartement())
                .tauxHoraire(employee.getTauxHoraire())
                .deviseTaux(employee.getDeviseTaux())
                .actif(employee.getActif())
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
