package com.henrique.virtualteacher.handlers;

import com.henrique.virtualteacher.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<String> handleGenericNotFoundException(EntityNotFoundException e) {

            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(DuplicateEntityException.class)
        public ResponseEntity<String> handleGenericDuplicateEntityException(DuplicateEntityException e) {

            return new ResponseEntity<String>(e.getMessage(), HttpStatus.CONFLICT);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<String> handleGenericAuthenticationFailureException(AuthenticationException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(UnauthorizedOperationException.class)
        public ResponseEntity<String> handleGenericUnauthorizedOperationException(UnauthorizedOperationException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(ImpossibleOperationException.class)
        public ResponseEntity<String> handleGenericImpossibleOperationException(ImpossibleOperationException e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(InsufficientFundsException.class)
        public ResponseEntity<String> handleGenericInsufficientFundsException(InsufficientFundsException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }



    }
