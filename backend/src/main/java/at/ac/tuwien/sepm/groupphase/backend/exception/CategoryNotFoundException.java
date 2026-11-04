package at.ac.tuwien.sepm.groupphase.backend.exception;

public class CategoryNotFoundException extends NotFoundException {

    public CategoryNotFoundException() {
    }

    public CategoryNotFoundException(final String message) {
        super(message);
    }

    public CategoryNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CategoryNotFoundException(final Exception e) {
        super(e);
    }
}
