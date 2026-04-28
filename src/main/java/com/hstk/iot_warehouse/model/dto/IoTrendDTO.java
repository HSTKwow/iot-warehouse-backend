package com.hstk.iot_warehouse.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class IoTrendDTO {
    private Long materialId;        //git test
    private String range;
    private List<String> xAxis;
    private List<Long> inbound;
    private List<Long> outbound;
}
