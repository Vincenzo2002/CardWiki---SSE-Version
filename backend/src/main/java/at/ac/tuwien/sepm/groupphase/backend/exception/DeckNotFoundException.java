package at.ac.tuwien.sepm.groupphase.backend.exception;


public class DeckNotFoundException extends NotFoundException {

    public DeckNotFoundException() {
    }

    public DeckNotFoundException(final String message) {
        super(message);
    }

    public DeckNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DeckNotFoundException(final Exception e) {
        super(e);
    }
}
