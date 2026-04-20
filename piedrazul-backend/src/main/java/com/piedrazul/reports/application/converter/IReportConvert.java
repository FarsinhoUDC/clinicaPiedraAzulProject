package com.piedrazul.reports.application.converter;

import java.util.List;

import com.piedrazul.reports.domain.Report;

/**
 * @author javiersolanop777
 */
public interface IReportConvert {

    /**
     * Metodo para convertir los objetos de una lista en formato de reporte
     * 
     * @param prmObjects Recibe la lista 

     * @return El reporte
     */
    Report convert(List<?> prmObjects);
}
