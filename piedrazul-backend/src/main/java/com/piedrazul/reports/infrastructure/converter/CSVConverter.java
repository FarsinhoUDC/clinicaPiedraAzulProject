package com.piedrazul.reports.infrastructure.converter;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Component;

import com.piedrazul.reports.application.converter.IReportConvert;
import com.piedrazul.reports.domain.Report;

/**
 * @author javiersolanop777
 */
@Component("CSVConvert")
public class CSVConverter implements IReportConvert {

    private final String FORMAT = "csv";
    private final String MIME_TYPE = "text/" + FORMAT;

    @Override
    public Report convert(List<?> prmObjects) 
    {
        Field[] arrFields = prmObjects.get(0).getClass().getDeclaredFields();
        String objContent = "";
        int i = 1;
        int varLength = arrFields.length; 

        for(Field objField : arrFields) 
        {
            objField.setAccessible(true);
            objContent += (++i <= varLength) ? objField.getName() + "," : 
                                               objField.getName() + "\n";
        }

        for(Object obj : prmObjects) 
        {
            i = 1;

            for(Field objField : arrFields) 
            {
                try
                {
                    objContent += (++i <= varLength) ? objField.get(obj) + "," :
                                                       objField.get(obj) + "\n";
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }

        return new Report(
            objContent.getBytes(StandardCharsets.UTF_8), 
            FORMAT, 
            MIME_TYPE
        );
    }
}
