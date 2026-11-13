package org.delcom.ifs25042springbootcashflow.domain.exception;

/**
 * Exception yang dilempar saat terjadi kegagalan validasi aturan bisnis atau input domain.
 */
public class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(message);
    }
}
