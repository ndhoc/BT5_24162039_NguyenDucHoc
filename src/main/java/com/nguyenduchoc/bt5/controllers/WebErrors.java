package com.nguyenduchoc.bt5.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import com.nguyenduchoc.bt5.repository.DataAccessException;

@ControllerAdvice
public class WebErrors {
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    String database(Model model) {
        model.addAttribute("errorMessage", "Không thể kết nối cơ sở dữ liệu. Hãy kiểm tra SQL Server và cấu hình DB_URL.");
        return "legacy/error";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    String upload(Model model) {
        model.addAttribute("errorMessage", "Tệp tải lên vượt quá dung lượng cho phép.");
        return "legacy/error";
    }
}
