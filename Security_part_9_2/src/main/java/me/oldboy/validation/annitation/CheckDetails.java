package me.oldboy.validation.annitation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import me.oldboy.validation.validator.DetailsValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DetailsValidator.class)
public @interface CheckDetails {

    String message() default "Details fields can't be blank/empty/wrong format!";

    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
