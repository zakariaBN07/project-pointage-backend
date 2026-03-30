package com.example.pointage_backend.service;

import com.example.pointage_backend.model.Employee;
import com.example.pointage_backend.model.Gestionnaire;
import com.example.pointage_backend.repository.EmployeeRepository;
import com.example.pointage_backend.repository.GestionnaireRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SyncService — Reads gestionnaires and employees directly from a configured
 * SQL Server source table/view and upserts them into the application's database.
 *
 * Configure in application.properties:
 *   sync.enabled=true
 *   sync.datasource.url / username / password   → source DB connection
 *   sync.source.gestionnaires.table             → source table/view name
 *   sync.source.employees.table                 → source table/view name
 *   sync.source.gestionnaires.col.*             → column name mapping
 *   sync.source.employees.col.*                 → column name mapping
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final GestionnaireRepository gestionnaireRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${sync.enabled:false}")
    private boolean syncEnabled;

    @Value("${sync.datasource.url:}")
    private String syncUrl;

    @Value("${sync.datasource.username:}")
    private String syncUsername;

    @Value("${sync.datasource.password:}")
    private String syncPassword;

    // Gestionnaire source config
    @Value("${sync.source.gestionnaires.table:gestionnaires_source}")
    private String gestionnaireTable;

    @Value("${sync.source.gestionnaires.col.name:nom}")
    private String colGestName;

    @Value("${sync.source.gestionnaires.col.email:email}")
    private String colGestEmail;

    @Value("${sync.source.gestionnaires.col.role:role}")
    private String colGestRole;

    @Value("${sync.source.gestionnaires.col.siege:siege}")
    private String colGestSiege;

    // Employee source config
    @Value("${sync.source.employees.table:employees}")
    private String employeeTable;

    @Value("${sync.source.employees.col.nom:nom}")
    private String colEmpNom;

    @Value("${sync.source.employees.col.prenom:prenom}")
    private String colEmpPrenom;

    @Value("${sync.source.employees.col.email:email}")
    private String colEmpEmail;

    @Value("${sync.source.employees.col.post:post}")
    private String colEmpPost;

    @Value("${sync.source.employees.col.departement:departement}")
    private String colEmpDepartement;

    @Value("${sync.source.employees.col.affaire_numero:affaire_numero}")
    private String colEmpAffaireNumero;

    @Value("${sync.source.employees.col.client:client}")
    private String colEmpClient;

    @Value("${sync.source.employees.col.site:site}")
    private String colEmpSite;

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    public SyncResult syncGestionnaires() {
        if (!syncEnabled) {
            return SyncResult.disabled("sync.enabled is false in application.properties");
        }
        int created = 0, updated = 0, errors = 0;
        List<String> messages = new ArrayList<>();

        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM " + gestionnaireTable;
            log.info("[SYNC] Fetching gestionnaires from: {}", sql);

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                Set<String> columns = getColumnNames(rs);

                while (rs.next()) {
                    try {
                        String name  = safeGet(rs, colGestName,  columns, null);
                        String email = safeGet(rs, colGestEmail, columns, null);
                        String role  = safeGet(rs, colGestRole,  columns, "chargeDAffaire");
                        String siege = safeGet(rs, colGestSiege, columns, "");

                        if (name == null || name.isBlank()) continue;

                        // Normalize role to match expected values
                        role = normalizeRole(role);

                        Optional<Gestionnaire> existing = gestionnaireRepository.findByName(name);
                        if (existing.isPresent()) {
                            Gestionnaire g = existing.get();
                            boolean changed = false;
                            if (email != null && !email.equals(g.getEmail())) { g.setEmail(email); changed = true; }
                            if (role != null && !role.equals(g.getRole()))    { g.setRole(role);   changed = true; }
                            if (siege != null && !siege.equals(g.getSiege())) { g.setSiege(siege); changed = true; }
                            if (changed) { gestionnaireRepository.save(g); updated++; }
                        } else {
                            // Generate a random default password for new accounts
                            String defaultPassword = UUID.randomUUID().toString().substring(0, 10);
                            Gestionnaire g = Gestionnaire.builder()
                                    .name(name)
                                    .email(email)
                                    .role(role)
                                    .siege(siege != null ? siege : "")
                                    .password(passwordEncoder.encode(defaultPassword))
                                    .build();
                            gestionnaireRepository.save(g);
                            created++;
                            messages.add("Créé: " + name + " (mot de passe temporaire: " + defaultPassword + ")");
                        }
                    } catch (Exception e) {
                        errors++;
                        log.error("[SYNC] Error processing gestionnaire row", e);
                        messages.add("Erreur sur une ligne: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[SYNC] Failed to sync gestionnaires from source DB", e);
            return SyncResult.error("Erreur de connexion à la source: " + e.getMessage());
        }

        log.info("[SYNC] Gestionnaires sync complete: created={}, updated={}, errors={}", created, updated, errors);
        return new SyncResult(true, null, created, updated, errors, messages);
    }

    public SyncResult syncEmployees(Long chargeDAffaireId, Long ingenieurId) {
        if (!syncEnabled) {
            return SyncResult.disabled("sync.enabled is false in application.properties");
        }
        int created = 0, updated = 0, errors = 0;
        List<String> messages = new ArrayList<>();

        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM " + employeeTable;
            log.info("[SYNC] Fetching employees from: {}", sql);

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                Set<String> columns = getColumnNames(rs);

                while (rs.next()) {
                    try {
                        String nom        = safeGet(rs, colEmpNom,          columns, "");
                        String prenom     = safeGet(rs, colEmpPrenom,        columns, "");
                        String email      = safeGet(rs, colEmpEmail,         columns, null);
                        String post       = safeGet(rs, colEmpPost,          columns, "");
                        String dept       = safeGet(rs, colEmpDepartement,   columns, "");
                        String affaire    = safeGet(rs, colEmpAffaireNumero, columns, "");
                        String client     = safeGet(rs, colEmpClient,        columns, "");
                        String site       = safeGet(rs, colEmpSite,          columns, "");

                        if (email == null || email.isBlank()) continue; // email is the key

                        // Combine nom and prenom for the "name" field
                        String fullName = (nom + " " + prenom).trim();

                        List<Employee> existing = employeeRepository.findByEmail(email);
                        if (!existing.isEmpty()) {
                            Employee emp = existing.get(0);
                            boolean changed = false;
                            if (nom != null     && !nom.equals(emp.getNom()))           { emp.setNom(nom);           changed = true; }
                            if (prenom != null  && !prenom.equals(emp.getPrenom()))     { emp.setPrenom(prenom);     changed = true; }
                            if (!fullName.isEmpty() && !fullName.equals(emp.getName())) { emp.setName(fullName);     changed = true; }
                            if (post != null    && !post.equals(emp.getPost()))         { emp.setPost(post);         changed = true; }
                            if (dept != null    && !dept.equals(emp.getDepartement()))  { emp.setDepartement(dept);  changed = true; }
                            if (affaire != null && !affaire.equals(emp.getAffaireNumero())) { emp.setAffaireNumero(affaire); changed = true; }
                            if (client != null  && !client.equals(emp.getClient()))     { emp.setClient(client);     changed = true; }
                            if (site != null    && !site.equals(emp.getSite()))         { emp.setSite(site);         changed = true; }
                            
                            // Assign manager if provided and not already set
                            if (chargeDAffaireId != null && emp.getChargeDAffaireId() == null) {
                                emp.setChargeDAffaireId(chargeDAffaireId);
                                changed = true;
                            }

                            // Assign ingenieur if provided and not already set
                            if (ingenieurId != null && emp.getIngenieurId() == null) {
                                emp.setIngenieurId(ingenieurId);
                                changed = true;
                            }

                            if (changed) { emp.setUpdatedAt(LocalDateTime.now()); employeeRepository.save(emp); updated++; }
                        } else {
                            Employee emp = Employee.builder()
                                    .nom(nom)
                                    .prenom(prenom)
                                    .name(fullName)
                                    .email(email)
                                    .post(post)
                                    .departement(dept)
                                    .affaireNumero(affaire)
                                    .client(client)
                                    .site(site)
                                    .chargeDAffaireId(chargeDAffaireId) // Assign to the charge d'affaire
                                    .ingenieurId(ingenieurId) // Assign to the ingenieur
                                    .actif(true)
                                    .status("En attente")
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build();
                            employeeRepository.save(emp);
                            created++;
                        }
                    } catch (Exception e) {
                        errors++;
                        log.error("[SYNC] Error processing employee row", e);
                        messages.add("Erreur sur une ligne: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[SYNC] Failed to sync employees from source DB", e);
            return SyncResult.error("Erreur de connexion à la source: " + e.getMessage());
        }

        log.info("[SYNC] Employees sync complete: created={}, updated={}, errors={}", created, updated, errors);
        return new SyncResult(true, null, created, updated, errors, messages);
    }

    public SyncResult syncAll() {
        SyncResult g = syncGestionnaires();
        SyncResult e = syncEmployees(null, null);
        List<String> msgs = new ArrayList<>();
        msgs.addAll(g.getMessages());
        msgs.addAll(e.getMessages());
        return new SyncResult(
            g.isSuccess() && e.isSuccess(),
            g.getError() != null ? g.getError() : e.getError(),
            g.getCreated() + e.getCreated(),
            g.getUpdated() + e.getUpdated(),
            g.getErrors() + e.getErrors(),
            msgs
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(syncUrl, syncUsername, syncPassword);
    }

    private Set<String> getColumnNames(ResultSet rs) throws SQLException {
        Set<String> cols = new HashSet<>();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            cols.add(meta.getColumnLabel(i).toLowerCase());
        }
        return cols;
    }

    private String safeGet(ResultSet rs, String column, Set<String> available, String defaultValue) {
        if (!available.contains(column.toLowerCase())) return defaultValue;
        try {
            String val = rs.getString(column);
            return val != null ? val.trim() : defaultValue;
        } catch (SQLException e) {
            return defaultValue;
        }
    }

    private String normalizeRole(String role) {
        if (role == null) return "chargeDAffaire";
        String r = role.trim().toLowerCase();
        if (r.contains("ingenieur") || r.contains("ingénieur") || r.contains("responsable")) return "ingenieur";
        if (r.contains("admin"))       return "admin";
        return "chargeDAffaire";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Result DTO
    // ─────────────────────────────────────────────────────────────────────────

    public static class SyncResult {
        private final boolean success;
        private final String error;
        private final int created;
        private final int updated;
        private final int errors;
        private final List<String> messages;

        public SyncResult(boolean success, String error, int created, int updated, int errors, List<String> messages) {
            this.success  = success;
            this.error    = error;
            this.created  = created;
            this.updated  = updated;
            this.errors   = errors;
            this.messages = messages != null ? messages : new ArrayList<>();
        }

        public static SyncResult disabled(String msg) {
            return new SyncResult(false, msg, 0, 0, 0, List.of());
        }

        public static SyncResult error(String msg) {
            return new SyncResult(false, msg, 0, 0, 0, List.of());
        }

        public boolean isSuccess()       { return success;  }
        public String getError()         { return error;    }
        public int getCreated()          { return created;  }
        public int getUpdated()          { return updated;  }
        public int getErrors()           { return errors;   }
        public List<String> getMessages(){ return messages; }
    }
}
