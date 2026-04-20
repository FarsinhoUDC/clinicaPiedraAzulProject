package com.piedrazul.reports.application.service;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.piedrazul.reports.domain.Report;

/**
 * @author javiersolanop777
 */
public interface IReportService {

    /**
     * Metodo para generar un reporte de las citas de un medico por fecha
     * 
     * @param medicoId Recibe el id del medico
     * @param fecha Recibe la fecha que se debe consultar
     * 
     * @return El reporte generado
     */
    Report generar(
        Long medicoId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    );
}
