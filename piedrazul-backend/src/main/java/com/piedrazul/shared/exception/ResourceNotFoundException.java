package com.piedrazul.shared.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " con id " + id + " no encontrado");
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super(resource + " con identificador '" + identifier + "' no encontrado");
    }
}
