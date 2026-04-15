package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.EmployeeDTO;
import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.model.Gestionnaire;
import com.example.pointage_backend.repository.EmployeeRepository;
import com.example.pointage_backend.repository.GestionnaireRepository;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyncService {

    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);
    private final JdbcTemplate jdbcTemplate;
    private final GestionnaireRepository gestionnaireRepository;
    private final EmployeeRepository employeeRepository;

    @Value("${sync.source.gestionnaires.table:gestionnaires_source}")
    private String gestionnaireTable;

    @Value("${sync.source.employees.table:employees_source}")
    private String employeeTable;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SyncResult {
        private String message;
        private int count;
        private boolean success;
    }

    @Transactional
    public SyncResult syncGestionnaires() {
        try {
            List<Map<String, Object>> sourceRows = jdbcTemplate.queryForList(
                "SELECT nom, email, role, siege FROM " + gestionnaireTable
            );
            int count = 0;
            for (Map<String, Object> row : sourceRows) {
                String name = (String) row.get("nom");
                String email = (String) row.get("email");
                if (name == null) continue;

                Optional<Gestionnaire> existing = gestionnaireRepository.findByName(name);
                Gestionnaire g = existing.orElse(new Gestionnaire());
                g.setName(name);
                g.setEmail(email);
                g.setRole((String) row.get("role"));
                g.setSiege((String) row.get("siege"));
                
                if (g.getId() == null) {
                    g.setPassword("default_sync_password"); 
                }

                gestionnaireRepository.save(g);
                count++;
            }
            return SyncResult.builder().message("Sync successful for " + count + " gestionnaires").count(count).success(true).build();
        } catch (Exception e) {
            logger.error("Sync gestionnaires failed", e);
            return SyncResult.builder().message("Sync failed: " + e.getMessage()).count(0).success(false).build();
        }
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAvailableEmployeesFromSource() {
        try {
            // Include matricule in source query
            List<Map<String, Object>> sourceRows = jdbcTemplate.queryForList(
                "SELECT nom, prenom, matricule, email, post, departement, affaire_numero, client, site FROM " + employeeTable
            );
            
            return sourceRows.stream().map(row -> EmployeeDTO.builder()
                .nom((String) row.get("nom"))
                .prenom((String) row.get("prenom"))
                .matricule((String) row.get("matricule"))
                .email((String) row.get("email"))
                .post((String) row.get("post"))
                .departement((String) row.get("departement"))
                .affaireNumero((String) row.get("affaire_numero"))
                .client((String) row.get("client"))
                .site((String) row.get("site"))
                .name(((String) row.get("nom")) + " " + ((String) row.get("prenom")))
                .build()
            ).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to fetch available employees from source", e);
            return new ArrayList<>();
        }
    }

    @Transactional
    public SyncResult syncEmployees(Long chargeDAffaireId, Long ingenieurId) {
        try {
            List<Map<String, Object>> sourceRows = jdbcTemplate.queryForList(
                "SELECT nom, prenom, matricule, email, post, departement, affaire_numero, client, site FROM " + employeeTable
            );
            if (sourceRows.isEmpty()) {
                return SyncResult.builder().message("No employees to sync").count(0).success(true).build();
            }

            // Map existing employees for fast lookup
            List<Employee> allExisting = employeeRepository.findAll();
            Map<String, Employee> byMatricule = allExisting.stream()
                .filter(e -> e.getMatricule() != null)
                .collect(Collectors.toMap(Employee::getMatricule, e -> e, (e1, e2) -> e1));
            Map<String, Employee> byEmail = allExisting.stream()
                .filter(e -> e.getEmail() != null)
                .collect(Collectors.toMap(Employee::getEmail, e -> e, (e1, e2) -> e1));

            List<Employee> toSave = new ArrayList<>();
            int count = 0;
            for (Map<String, Object> row : sourceRows) {
                String matricule = (String) row.get("matricule");
                String email = (String) row.get("email");
                if (matricule == null && email == null) continue;

                Employee e = (matricule != null) ? byMatricule.get(matricule) : null;
                if (e == null && email != null) e = byEmail.get(email);
                
                if (e == null) e = new Employee();

                e.setNom((String) row.get("nom"));
                e.setPrenom((String) row.get("prenom"));
                e.setMatricule(matricule);
                e.setEmail(email);
                e.setPost((String) row.get("post"));
                e.setDepartement((String) row.get("departement"));
                e.setAffaireNumero((String) row.get("affaire_numero"));
                e.setClient((String) row.get("client"));
                e.setSite((String) row.get("site"));

                if (chargeDAffaireId != null && e.getChargeDAffaireId() == null) {
                    e.setChargeDAffaireId(chargeDAffaireId);
                }
                if (ingenieurId != null && e.getIngenieurId() == null) {
                    e.setIngenieurId(ingenieurId);
                }

                toSave.add(e);
                count++;
            }
            
            if (!toSave.isEmpty()) {
                employeeRepository.saveAll(toSave);
            }
            
            return SyncResult.builder().message("Sync successful for " + count + " employees").count(count).success(true).build();
        } catch (Exception e) {
            logger.error("Sync employees failed", e);
            return SyncResult.builder().message("Sync failed: " + e.getMessage()).count(0).success(false).build();
        }
    }

    @Transactional
    public SyncResult syncSelectedEmployees(List<String> matricules, Long ingenieurId) {
        if (matricules == null || matricules.isEmpty()) {
            return SyncResult.builder().message("No employees selected").count(0).success(true).build();
        }
        try {
            // Fetch all source rows and filter by matricule
            List<Map<String, Object>> sourceRows = jdbcTemplate.queryForList(
                "SELECT nom, prenom, matricule, email, post, departement, affaire_numero, client, site FROM " + employeeTable
            );
            
            int count = 0;
            for (Map<String, Object> row : sourceRows) {
                String m = (String) row.get("matricule");
                if (m != null && matricules.contains(m)) {
                    List<Employee> existingList = employeeRepository.findByMatricule(m);
                    Employee e = existingList.isEmpty() ? new Employee() : existingList.get(0);
                    
                    e.setNom((String) row.get("nom"));
                    e.setPrenom((String) row.get("prenom"));
                    e.setMatricule(m);
                    e.setEmail((String) row.get("email"));
                    e.setPost((String) row.get("post"));
                    e.setDepartement((String) row.get("departement"));
                    e.setAffaireNumero((String) row.get("affaire_numero"));
                    e.setClient((String) row.get("client"));
                    e.setSite((String) row.get("site"));
                    e.setIngenieurId(ingenieurId);
                    e.setActif(true);

                    employeeRepository.save(e);
                    count++;
                }
            }
            return SyncResult.builder().message("Successfully synced " + count + " selected employees").count(count).success(true).build();
        } catch (Exception e) {
            logger.error("Selective sync failed", e);
            return SyncResult.builder().message("Selective sync failed: " + e.getMessage()).count(0).success(false).build();
        }
    }

    @Transactional
    public SyncResult syncAll() {
        SyncResult g = syncGestionnaires();
        SyncResult e = syncEmployees(null, null);
        return SyncResult.builder()
                .message("Full sync: G(" + g.getMessage() + "), E(" + e.getMessage() + ")")
                .count(g.getCount() + e.getCount())
                .success(g.isSuccess() && e.isSuccess())
                .build();
    }
}
