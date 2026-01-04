package it.sara.demo.exception;

import it.sara.demo.dto.StatusDTO;
import it.sara.demo.web.response.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.UUID;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<GenericResponse> handleGenericException(GenericException ex) {
        GenericResponse resp = new GenericResponse();
        StatusDTO status = ex.getStatus();

        if (status.getTraceId() == null || status.getTraceId().isBlank()) {
            status.setTraceId(java.util.UUID.randomUUID().toString());
        }

        resp.setStatus(status);
        return ResponseEntity.ok(resp);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleAnyException(Exception ex) {
        log.error("Unhandled exception", ex);

        GenericResponse resp = new GenericResponse();

        StatusDTO status = new StatusDTO();
        status.setCode(GenericException.GENERIC_ERROR.getCode());
        status.setMessage(GenericException.GENERIC_ERROR.getMessage());
        status.setTraceId(java.util.UUID.randomUUID().toString());

        resp.setStatus(status);

        return ResponseEntity.ok(resp);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().isEmpty()
                ? "Validation error"
                : ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        GenericResponse res = new GenericResponse();
        StatusDTO status = new StatusDTO();
        status.setCode(400);
        status.setMessage(msg);
        status.setTraceId(UUID.randomUUID().toString());
        res.setStatus(status);

        return ResponseEntity.ok(res);
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<GenericResponse> handleNoResourceFound(NoResourceFoundException ex) {
        GenericResponse body = new GenericResponse();
        StatusDTO status = new StatusDTO();
        status.setCode(ex.getStatusCode().value());
        status.setMessage(ex.getBody().toString());
        status.setTraceId(java.util.UUID.randomUUID().toString());
        body.setStatus(status);

        return ResponseEntity.ok(body);
    }
}