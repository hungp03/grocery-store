package com.store.grocery.util.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String s){
        super(s);
    }
}