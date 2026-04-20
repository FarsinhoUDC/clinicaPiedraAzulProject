package com.piedrazul.reports.application.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.piedrazul.citas.application.CitaService;
import com.piedrazul.citas.dto.CitaResponse;
import com.piedrazul.reports.application.converter.IReportConvert;
import com.piedrazul.reports.domain.Report;

/**
 * @author javiersolanop777
 */
@Service
public class ReportServiceImp implements IReportService {

    /**
     * Almacena el objeto que da formato a las citas
     */
    @Autowired
    @Qualifier("CSVConvert")
    private IReportConvert reportConvert;

    /**
     * Almacena el servicio proporcionado por el modulo de citas
     */
    @Autowired
    private CitaService citaService;

    @Override
    public Report generar(Long medicoId, LocalDate fecha) 
    {
        List<CitaResponse> listaCitas = citaService.listarPorMedicoYFecha(medicoId, fecha);

        if(listaCitas == null) return null;
        if(listaCitas.isEmpty()) return null;

        return reportConvert.convert(listaCitas);
    }
}
