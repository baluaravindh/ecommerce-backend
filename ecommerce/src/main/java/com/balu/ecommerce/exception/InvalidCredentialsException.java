package com.balu.ecommerce.exception;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException(String msg){
        super(msg);
    }
}
