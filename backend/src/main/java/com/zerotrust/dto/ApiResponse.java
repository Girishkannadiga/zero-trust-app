package com.zerotrust.dto;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess()                   { return success; }
    public void setSuccess(boolean success)      { this.success = success; }
    public String getMessage()                   { return message; }
    public void setMessage(String message)       { this.message = message; }
    public T getData()                           { return data; }
    public void setData(T data)                  { this.data = data; }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public static <T> Builder<T> builder() { return new Builder<>(); }

    public static class Builder<T> {
        private final ApiResponse<T> r = new ApiResponse<>();
        public Builder<T> success(boolean v)     { r.success = v; return this; }
        public Builder<T> message(String v)      { r.message = v; return this; }
        public Builder<T> data(T v)              { r.data = v; return this; }
        public ApiResponse<T> build()            { return r; }
    }
}
