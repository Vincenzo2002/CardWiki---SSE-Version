package at.ac.tuwien.sepm.groupphase.backend.exception;

public class AuthenticationRequiredException extends RuntimeException {
    public AuthenticationRequiredException(final String msg) {
        super(msg);
    }
}
