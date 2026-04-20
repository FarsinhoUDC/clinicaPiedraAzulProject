package com.piedrazul.reports.infrastructure.web;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.reports.application.service.IReportService;
import com.piedrazul.reports.domain.Report;

/**
 * @author javiersolanop777
 */
@RestController
@RequestMapping("/api/reportes")
public class ReportController {

    @Autowired
    private IReportService reportService;

    @GetMapping(value = "/citas/{medicoId}/{fecha}")
    public ResponseEntity<byte[]> getReport(
        @PathVariable Long medicoId,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    )
    {
        System.out.println("Si llega");
        Report objReport = reportService.generar(medicoId, fecha);

        if(objReport == null)
            return ResponseEntity.noContent().build();

        else
        {
            return ResponseEntity.ok()
                                 .contentType(MediaType.parseMediaType(objReport.getMimeType()))
                                 .header(
                                    HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"reporte." + objReport.getFormat() + "\""
                                 )
                                 .body(objReport.getContent());
        }
    }
}
