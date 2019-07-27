package com.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestParamsModel {

    @JsonProperty("url")
    private String url;
    @JsonProperty("timeout")
    private int timeout;
    @JsonProperty("requests")
    private int requests;

}
