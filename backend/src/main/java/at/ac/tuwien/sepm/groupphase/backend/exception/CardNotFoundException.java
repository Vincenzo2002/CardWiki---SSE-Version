package at.ac.tuwien.sepm.groupphase.backend.exception;

public class CardNotFoundException extends NotFoundException {
        public CardNotFoundException() {
        }

        public CardNotFoundException(final String message) {
            super(message);
        }

        public CardNotFoundException(final String message, final Throwable cause) {
            super(message, cause);
        }

        public CardNotFoundException(final Exception e) {
            super(e);
        }
}
