package com.app.webnongsan.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginationDTO implements Serializable{
    @Serial
    private static final long serialVersionUID = 1L;
    private Meta meta;
    private Object result;

    @Getter
    @Setter
    public static class Meta{
        private int page;
        private int pageSize;
        private int pages;
        private long total;
    }
}
