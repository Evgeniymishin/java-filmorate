//package ru.yandex.practicum.filmorate.controller;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import ru.yandex.practicum.filmorate.exception.NotFoundException;
//import ru.yandex.practicum.filmorate.exception.ValidationException;
//import ru.yandex.practicum.filmorate.model.ErrorResponse;
//
//@Slf4j
//@RestControllerAdvice
//public class ErrorHandler {
//    private static final String UNEXPECTED_ERR_MSG = "Произошла непредвиденная ошибка.";
//
//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ErrorResponse handleValidationException(final ValidationException e) {
//        log.error("Ошибка валидации: {}", e.getMessage());
//        return new ErrorResponse(
//                e.getMessage()
//        );
//    }
//
//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ErrorResponse handleNotFoundException(final NotFoundException e) {
//        log.error("Ошибка: {}", e.getMessage());
//        return new ErrorResponse(
//                e.getMessage()
//        );
//    }
//
//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ErrorResponse handleIndexOutOfBoundsException(final IndexOutOfBoundsException e) {
//        log.error("Ошибка: {}", e.getMessage());
//        return new ErrorResponse(
//                e.getMessage()
//        );
//    }
//
//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public ErrorResponse handleThrowable(final Throwable e) {
//        log.error("Ошибка валидации: {}", UNEXPECTED_ERR_MSG);
//        return new ErrorResponse(
//                UNEXPECTED_ERR_MSG
//        );
//    }
//}
