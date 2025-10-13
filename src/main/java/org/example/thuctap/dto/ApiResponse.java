package org.example.thuctap.dto;

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

    public static <T> ApiResponse<T> ok(T data, String msg) {
        return new ApiResponse<>(true, data, msg);
    }
    public static <T> ApiResponse<T> fail(String msg) {
        return new ApiResponse<>(false, null, msg);
    }

    // getters/setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
