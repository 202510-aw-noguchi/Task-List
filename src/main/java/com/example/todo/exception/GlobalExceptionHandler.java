package com.example.todo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import com.example.todo.controller.AdminController;
import com.example.todo.controller.LoginController;
import com.example.todo.controller.TodoController;

@ControllerAdvice(assignableTypes = {TodoController.class, AdminController.class, LoginController.class})
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TodoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleTodoNotFound(TodoNotFoundException ex, Model model) {
        logger.warn("Todo not found", ex);
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusiness(BusinessException ex, Model model) {
        logger.warn("Business error: {}", ex.getErrorCode(), ex);
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("errorCode", ex.getErrorCode());
        return "error/business";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException ex, Model model) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        logger.warn("ResponseStatusException: {}", ex.getReason(), ex);
        model.addAttribute("message", ex.getReason());
        if (status == HttpStatus.NOT_FOUND) {
            return "error/404";
        }
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, Model model) {
        logger.error("Unhandled exception", ex);
        model.addAttribute("message", "Unexpected error occurred.");
        return "error/500";
    }
}
