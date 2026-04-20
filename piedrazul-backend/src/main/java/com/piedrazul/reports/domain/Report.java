package com.piedrazul.reports.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Report {

    private byte[] content;
    private String format;
    private String mimeType;
}
