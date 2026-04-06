package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.EmployeeDTO;
import com.example.pointage_backend.model.Affaire;
import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.model.Pointage;
import com.example.pointage_backend.repository.AffaireRepository;
import com.example.pointage_backend.repository.EmployeeRepository;
import com.example.pointage_backend.repository.PointageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AffaireRepository projectRepository;
    private final PointageRepository pointageRepository;

    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional
    public EmployeeDTO saveEmployee(EmployeeDTO dto) {
        ResolvedAffaireLink affaireLink = resolveAffaireLink(dto.getProjectId(), dto.getAffaireNumero());
        String normalizedAffaireNumero = affaireLink.codeAffaire();
        Long projectId = affaireLink.projectId();

        String nom = dto.getNom();
        String prenom = dto.getPrenom();
        if ((nom == null || nom.isBlank()) && dto.getName() != null && !dto.getName().isBlank()) {
            String[] parts = dto.getName().trim().split("\\s+", 2);
            nom = parts[0];
            if (parts.length > 1) {
                prenom = parts[1];
            }
        }
        if (nom == null || nom.isBlank()) {
            nom = "Inconnu";
        }
        if (prenom == null || prenom.isBlank()) {
            prenom = "-";
        }

        String matricule = trimToNull(dto.getMatricule());
        if (matricule == null) {
            matricule = "EXT-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        Employee employee;
        if (dto.getId() != null) {
            employee = employeeRepository.findById(dto.getId())
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                            "Employee not found: " + dto.getId()));
        } else {
            employee = findExistingEmployeeForUpsert(dto, matricule, dto.getEmail());
        }

        String email = trimToNull(dto.getEmail());
        if (email == null && employee != null) {
            email = trimToNull(employee.getEmail());
        }
        if (email == null) {
            email = buildFallbackEmail(matricule, dto.getChargeDAffaireId());
        }

        if (employee != null) {
            employee.setNom(nom);
            employee.setPrenom(prenom);
            employee.setName(dto.getName());
            employee.setMatricule(matricule);
            employee.setEmail(email);
            employee.setPost(dto.getPost());
            employee.setDepartement(dto.getDepartement());
            employee.setTauxHoraire(dto.getTauxHoraire() != null ? dto.getTauxHoraire() : 0.0);
            employee.setDeviseTaux(dto.getDeviseTaux() != null ? dto.getDeviseTaux() : "EUR");
            employee.setActif(dto.getActif() != null ? dto.getActif() : true);
            employee.setAffaireNumero(normalizedAffaireNumero != null ? normalizedAffaireNumero : employee.getAffaireNumero());
            employee.setProjectId(projectId != null ? projectId : employee.getProjectId());
            employee.setClient(dto.getClient());
            employee.setSite(dto.getSite());
            employee.setPlannedHours(dto.getPlannedHours());
            employee.setStatus(dto.getStatus() != null ? dto.getStatus() : "En attente");
            employee.setPointageEntree(dto.getPointageEntree());
            employee.setPointageSortie(dto.getPointageSortie());
            employee.setChargeDAffaireId(dto.getChargeDAffaireId() != null ? dto.getChargeDAffaireId() : employee.getChargeDAffaireId());
            employee.setIngenieurId(dto.getIngenieurId() != null ? dto.getIngenieurId() : employee.getIngenieurId());
            employee.setTotHrsTravaillees(dto.getTotHrsTravaillees());
            employee.setNbrJrsTravaillees(dto.getNbrJrsTravaillees());
            employee.setNbrJrsAbsence(dto.getNbrJrsAbsence());
            employee.setTotHrsDimanche(dto.getTotHrsDimanche());
            employee.setNbrJrsFeries(dto.getNbrJrsFeries());
            employee.setNbrJrsFeriesTravailes(dto.getNbrJrsFeriesTravailes());
            employee.setNbrJrsConges(dto.getNbrJrsConges());
            employee.setNbrJrsDeplacementsMaroc(dto.getNbrJrsDeplacementsMaroc());
            employee.setNbrJrsPaniers(dto.getNbrJrsPaniers());
            employee.setNbrJrsDetente(dto.getNbrJrsDetente());
            employee.setNbrJrsDeplacementsExpatrie(dto.getNbrJrsDeplacementsExpatrie());
            employee.setNbrJrsRecuperation(dto.getNbrJrsRecuperation());
            employee.setNbrJrsMaladie(dto.getNbrJrsMaladie());
            employee.setChantierAtelier(dto.getChantierAtelier());
            employee.setProjectProgress(dto.getProjectProgress());
            employee.setUpdatedAt(LocalDateTime.now());
        } else {
            employee = Employee.builder()
                    .nom(nom)
                    .prenom(prenom)
                    .name(dto.getName())
                    .matricule(matricule)
                    .email(email)
                    .post(dto.getPost())
                    .departement(dto.getDepartement())
                    .tauxHoraire(dto.getTauxHoraire() != null ? dto.getTauxHoraire() : 0.0)
                    .deviseTaux(dto.getDeviseTaux() != null ? dto.getDeviseTaux() : "EUR")
                    .actif(dto.getActif() != null ? dto.getActif() : true)
                    .affaireNumero(normalizedAffaireNumero)
                    .projectId(projectId)
                    .client(dto.getClient())
                    .site(dto.getSite())
                    .plannedHours(dto.getPlannedHours())
                    .status(dto.getStatus() != null ? dto.getStatus() : "En attente")
                    .pointageEntree(dto.getPointageEntree())
                    .pointageSortie(dto.getPointageSortie())
                    .chargeDAffaireId(dto.getChargeDAffaireId())
                    .ingenieurId(dto.getIngenieurId())
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
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        Employee saved = employeeRepository.save(employee);
        return mapToDTO(saved);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    public List<EmployeeDTO> getEmployeesByFilter(Long chargeDAffaireId, Long ingenieurId) {
        List<Employee> employees;
        if (chargeDAffaireId != null) {
            employees = employeeRepository.findByChargeDAffaireId(chargeDAffaireId);
        } else if (ingenieurId != null) {
            employees = employeeRepository.findByIngenieurId(ingenieurId);
        } else {
            employees = employeeRepository.findAll();
        }
        return employees.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> getEmployeesByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return List.of();
        }
        return employeeRepository.findByEmail(email).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> getEmployeesByName(String name) {
        if (name == null || name.isEmpty()) {
            return List.of();
        }
        return employeeRepository.findByName(name).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private EmployeeDTO mapToDTO(Employee employee) {
        Affaire linkedAffaire = resolveAffaireForEmployee(employee);
        String codeAffaire = linkedAffaire != null
                ? normalizeAffaireNumero(linkedAffaire.getCodeAffaire())
                : normalizeAffaireNumero(employee.getAffaireNumero());

        // Sum actual pointages recorded in the system
        Double totalPointageHrs = pointageRepository.findByEmployeeIdOrderByDatePointageDesc(employee.getId()).stream()
                .mapToDouble(p -> p.getHeuresTravaillees() != null ? p.getHeuresTravaillees().doubleValue() : 0.0)
                .sum();

        // Use the pointage total if available, otherwise fallback to existing field
        Double totHrs = totalPointageHrs > 0 ? totalPointageHrs : (employee.getTotHrsTravaillees() != null ? employee.getTotHrsTravaillees() : 0.0);

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
                .affaireNumero(codeAffaire)
                .codeAffaire(codeAffaire)
                .projectId(employee.getProjectId())
                .affaireId(employee.getProjectId())
                .client(employee.getClient())
                .name(employee.getName())
                .site(employee.getSite())
                .plannedHours(employee.getPlannedHours())
                .status(employee.getStatus())
                .pointageEntree(employee.getPointageEntree())
                .pointageSortie(employee.getPointageSortie())
                .chargeDAffaireId(employee.getChargeDAffaireId())
                .ingenieurId(employee.getIngenieurId())
                .totHrsTravaillees(totHrs)
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
                .affaireProgress(employee.getProjectProgress())
                .build();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeAffaireNumero(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        String collapsed = trimmed.toLowerCase().replaceAll("[\\s_-]+", "");
        if ("-".equals(trimmed)
                || "sansaffaire".equals(collapsed)
                || "na".equals(collapsed)
                || "n/a".equalsIgnoreCase(trimmed)
                || "aucune".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }

    private Employee findExistingEmployeeForUpsert(EmployeeDTO dto, String matricule, String email) {
        String normalizedMatricule = trimToNull(matricule);
        Long chargeDAffaireId = dto.getChargeDAffaireId();
        if (normalizedMatricule != null) {
            List<Employee> byMatricule = chargeDAffaireId != null
                    ? employeeRepository.findByChargeDAffaireIdAndMatricule(chargeDAffaireId, normalizedMatricule)
                    : employeeRepository.findByMatricule(normalizedMatricule);
            if (!byMatricule.isEmpty()) {
                return byMatricule.get(0);
            }
        }

        String normalizedEmail = trimToNull(email);
        if (normalizedEmail != null && !isPlaceholderEmail(normalizedEmail)) {
            List<Employee> byEmail = chargeDAffaireId != null
                    ? employeeRepository.findByChargeDAffaireIdAndEmail(chargeDAffaireId, normalizedEmail)
                    : employeeRepository.findByEmail(normalizedEmail);
            if (!byEmail.isEmpty()) {
                return byEmail.get(0);
            }
        }

        return null;
    }

    private boolean isPlaceholderEmail(String email) {
        return email != null && email.toLowerCase().endsWith("@placeholder.local");
    }

    private String buildFallbackEmail(String matricule, Long chargeDAffaireId) {
        String normalizedMatricule = trimToNull(matricule);
        String safeMatricule = normalizedMatricule == null
                ? "employee"
                : normalizedMatricule.replaceAll("[^a-zA-Z0-9]+", "-").toLowerCase();
        String scope = chargeDAffaireId != null ? "cda-" + chargeDAffaireId : "shared";
        return "no-reply-" + scope + "-" + safeMatricule + "@placeholder.local";
    }

    private ResolvedAffaireLink resolveAffaireLink(Long projectId, String affaireNumero) {
        if (projectId != null) {
            Affaire existingProject = projectRepository.findById(projectId).orElse(null);
            if (existingProject != null) {
                return new ResolvedAffaireLink(
                        existingProject.getId(),
                        normalizeAffaireNumero(existingProject.getCodeAffaire())
                );
            }
        }

        String normalizedAffaireNumero = normalizeAffaireNumero(affaireNumero);
        if (normalizedAffaireNumero == null) {
            return new ResolvedAffaireLink(projectId, null);
        }

        Affaire affaire = projectRepository.findByCodeAffaire(normalizedAffaireNumero)
                .orElseGet(() -> projectRepository.save(
                        Affaire.builder()
                                .codeAffaire(normalizedAffaireNumero)
                                .nomAffaire(normalizedAffaireNumero)
                                .affairesCodeAffaireUnique(normalizedAffaireNumero)
                                .devise("EUR")
                                .statut("ACTIVE")
                                .description("Affaire creee automatiquement depuis l'import employe.")
                                .heuresEstimees(BigDecimal.ZERO)
                                .build()
                ));

        return new ResolvedAffaireLink(
                affaire.getId(),
                normalizeAffaireNumero(affaire.getCodeAffaire())
        );
    }

    private Affaire resolveAffaireForEmployee(Employee employee) {
        if (employee.getProjectId() != null) {
            Affaire linked = projectRepository.findById(employee.getProjectId()).orElse(null);
            if (linked != null) {
                return linked;
            }
        }

        String normalizedAffaireNumero = normalizeAffaireNumero(employee.getAffaireNumero());
        if (normalizedAffaireNumero == null) {
            return null;
        }

        return projectRepository.findByCodeAffaire(normalizedAffaireNumero).orElse(null);
    }

    private record ResolvedAffaireLink(Long projectId, String codeAffaire) {}
}
