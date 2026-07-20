package com.arcac.managerkobo.database;


public class KoboDataException extends RuntimeException {
    public KoboDataException(String message) { super(message); }
    public KoboDataException(String message, Throwable cause) { super(message, cause); }
}
