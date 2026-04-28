package com.hstk.iot_warehouse.model.dto;

import lombok.Data;

@Data
public class IoTrendRowDTO {
    private String bucket;
    private Integer ioType;
    private Long count;
}
