package com.store.grocery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Meta meta;
    private Object result;

    // Factory Method to create PaginationResponse from Page and Pageable
    public static PaginationResponse from(Page<?> page, Pageable pageable) {
        PaginationResponse response = new PaginationResponse();

        // Create and set Meta
        Meta meta = new Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        response.setMeta(meta);
        response.setResult(page.getContent());

        return response;
    }

    @Getter
    @Setter
    public static class Meta implements Serializable {
        private int page;
        private int pageSize;
        private int pages;
        private long total;
    }
}
