package me.oldboy.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.validation.annitation.CheckDetails;

public class DetailsValidator implements ConstraintValidator<CheckDetails, DetailsCreateDto> {

    @Override
    public void initialize(CheckDetails constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(DetailsCreateDto enteredDetail, ConstraintValidatorContext context) {
        boolean isNameNotBlankAndMoreThen2Char = enteredDetail.getClientSurName().trim().length() > 2;
        boolean isSurNameNotBlankAndMoreThen2Char = enteredDetail.getClientName().trim().length() > 2;
        boolean isAgeMoreThenZero = enteredDetail.getAge() >= 0;
        if (isAgeMoreThenZero && isNameNotBlankAndMoreThen2Char && isSurNameNotBlankAndMoreThen2Char) {
            return true;
        } else {
            return false;
        }
    }
}
