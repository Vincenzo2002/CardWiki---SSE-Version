package at.ac.tuwien.sepm.groupphase.backend.exception;

public class ConflictException extends RuntimeException {
    public ConflictException() {
    }

    public ConflictException(final String message) {
        super(message);
    }

    public ConflictException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ConflictException(final Throwable cause) {
        super(cause);
    }

    public ConflictException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
