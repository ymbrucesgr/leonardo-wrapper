package org.generationai.exception;

import lombok.extern.slf4j.Slf4j;
import org.generationai.entity.response.ImageResponseNotOk;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    public ImageResponseNotOk handleException(Exception e) {
        log.warn("server error: ", e);
        return ImageResponseNotOk.builder()
                .error(e.getMessage())
                .build();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(IllegalArgumentException.class)
    public ImageResponseNotOk handleIllegalArgumentException(IllegalArgumentException e) {
        log.info("Invalid input: {}", e.getMessage());
        return ImageResponseNotOk.builder()
                .error(ErrorCode.VALIDATION_ERROR.getMsg())
                .build();
    }

}
