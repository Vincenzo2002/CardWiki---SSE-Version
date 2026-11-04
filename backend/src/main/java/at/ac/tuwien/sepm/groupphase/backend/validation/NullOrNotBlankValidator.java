package at.ac.tuwien.sepm.groupphase.backend.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, String> {
   @Override
   public void initialize(final NullOrNotBlank constraint) {
   }

   public boolean isValid(final String str, final ConstraintValidatorContext context) {
       return str == null || !str.trim().isEmpty();
   }
}
