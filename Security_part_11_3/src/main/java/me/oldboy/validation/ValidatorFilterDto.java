package me.oldboy.validation;

import jakarta.validation.*;

import java.util.Set;

public class ValidatorFilterDto {

        private static ValidatorFilterDto instance;

        private ValidatorFilterDto(){
        }

        public static ValidatorFilterDto getInstance(){
            if (instance == null){
                instance = new ValidatorFilterDto();
            }
            return instance;
        }

        public <T> void isValidData(T t) {
            ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
            Validator validator = validatorFactory.getValidator();
            Set<ConstraintViolation<T>> validationResult = validator.validate(t);
            if (!validationResult.isEmpty()) {
                throw new ConstraintViolationException(validationResult);
            }
        }
}