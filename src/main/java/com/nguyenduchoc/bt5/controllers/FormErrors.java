package com.nguyenduchoc.bt5.controllers;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import com.nguyenduchoc.bt5.validation.FormValidation.FieldError;

final class FormErrors {
    private FormErrors() { }

    static void show(HttpServletRequest request, IllegalArgumentException error) {
        if (error instanceof FieldError fieldError && !"id".equals(fieldError.field())) {
            request.setAttribute("fieldErrors", Map.of(fieldError.field(), fieldError.getMessage()));
        } else {
            request.setAttribute("alert", error.getMessage());
        }
    }
}
