package com.example.pointage_backend.service;

import com.example.pointage_backend.model.Gestionnaire;
import com.example.pointage_backend.repository.GestionnaireRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelService {
    private final GestionnaireRepository gestionnaireRepository;

    public byte[] exportGestionnairesToExcel(List<Gestionnaire> gestionnaires) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Gestionnaires");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Name", "Role"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (Gestionnaire gestionnaire : gestionnaires) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(gestionnaire.getId());
                row.createCell(1).setCellValue(gestionnaire.getName());
                row.createCell(2).setCellValue(gestionnaire.getRole());
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    
}
