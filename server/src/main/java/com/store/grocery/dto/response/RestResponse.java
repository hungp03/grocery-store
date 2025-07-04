package com.store.grocery.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestResponse<T> {
    private int statusCode;
    private T data;
    private String error;
    private Object message;
}
