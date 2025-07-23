package com.levi.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddTimeSeriesDataParam {
    private String timeStamp;
    private Double value;
    private String type;
    private String deviceName;
}
