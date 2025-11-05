package com.example.GestionComida.web;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;

    public ApiResponse() {}

    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // Factories
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> error(String code) {
        return new ApiResponse<>(false, null, code);
    }

    // ---- Paginación tipada ----
    public static class Pagination {
        private int page;
        private int totalPages;

        public Pagination() {}
        public Pagination(int page, int totalPages) {
            this.page = page;
            this.totalPages = totalPages;
        }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }

    public static class PageResponse<U> {
        private Iterable<U> items;
        private Pagination pagination;

        public PageResponse() {}
        public PageResponse(Iterable<U> items, Pagination pagination) {
            this.items = items;
            this.pagination = pagination;
        }
        public Iterable<U> getItems() { return items; }
        public void setItems(Iterable<U> items) { this.items = items; }
        public Pagination getPagination() { return pagination; }
        public void setPagination(Pagination pagination) { this.pagination = pagination; }
    }

    public static <T> ApiResponse<PageResponse<T>> page(Iterable<T> items, int page, int totalPages) {
        return new ApiResponse<>(
                true,
                new PageResponse<>(items, new Pagination(page, totalPages)),
                null
        );
    }

    // Getters/Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    // Helper para respuestas simples tipo mapa (opcional)
    public static ApiResponse<Map<String,Object>> ofMap(Object... kv) {
        Map<String,Object> m = new HashMap<>();
        for (int i = 0; i+1 < kv.length; i+=2) {
            m.put(String.valueOf(kv[i]), kv[i+1]);
        }
        return ApiResponse.ok(m);
    }
}
