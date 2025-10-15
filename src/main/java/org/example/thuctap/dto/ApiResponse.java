package org.example.thuctap.dto;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;

    // thêm 2 thuộc tính để hỗ trợ phân trang
    private Long totalElements;
    private Integer totalPages;

    public ApiResponse() {}

    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // 👇 thêm constructor có thông tin phân trang
    public ApiResponse(boolean success, T data, String message, Long totalElements, Integer totalPages) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    // --- Các hàm static tiện ích ---
    public static <T> ApiResponse<T> ok(T data, String msg) {
        return new ApiResponse<>(true, data, msg);
    }

    public static <T> ApiResponse<T> ok(T data, String msg, long totalElements, int totalPages) {
        return new ApiResponse<>(true, data, msg, totalElements, totalPages);
    }

    public static <T> ApiResponse<T> fail(String msg) {
        return new ApiResponse<>(false, null, msg);
    }

    // --- Getter/Setter ---
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getTotalElements() { return totalElements; }
    public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
}
