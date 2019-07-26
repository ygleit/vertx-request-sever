package com.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RequestParamsModel {

    private final String url;
    private final int timeout;
    private final int requests;

}
