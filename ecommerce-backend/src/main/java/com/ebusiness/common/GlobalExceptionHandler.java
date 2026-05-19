package com.ebusiness.common;

import javax.validation.ConstraintViolationException;

import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BusinessException.class)
  public ApiResponse<Void> handleBusinessException(BusinessException ex) {
    return ApiResponse.error(ex.getErrorCode().getCode(), ex.getMessage());
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ApiResponse<Void> handleValidationException(Exception ex) {
    String message = "Invalid parameter";
    if (ex instanceof MethodArgumentNotValidException) {
      MethodArgumentNotValidException m = (MethodArgumentNotValidException) ex;
      if (m.getBindingResult().getFieldError() != null) {
        message = m.getBindingResult().getFieldError().getDefaultMessage();
      }
    } else if (ex instanceof BindException) {
      BindException b = (BindException) ex;
      if (b.getBindingResult().getFieldError() != null) {
        message = b.getBindingResult().getFieldError().getDefaultMessage();
      }
    }
    return ApiResponse.error(ErrorCode.INVALID_PARAM.getCode(), message);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException ex) {
    return ApiResponse.error(ErrorCode.INVALID_PARAM.getCode(), ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ApiResponse<Void> handleException(Exception ex) {
    log.error("Unhandled exception", ex);
    return ApiResponse.error(ErrorCode.SERVER_ERROR.getCode(), ErrorCode.SERVER_ERROR.getMessage());
  }
}
