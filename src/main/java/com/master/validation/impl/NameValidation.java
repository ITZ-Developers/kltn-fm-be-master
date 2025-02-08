package com.master.validation.impl;

import com.master.constant.MasterConstant;
import com.master.validation.NameConstraint;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class NameValidation implements ConstraintValidator<NameConstraint, String> {
    private boolean allowNull;

    @Override
    public void initialize(NameConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (!StringUtils.isNoneBlank(value) && allowNull) {
            return true;
        }
        return StringUtils.isNoneBlank(value) && Objects.requireNonNull(value).matches(MasterConstant.NAME_PATTERN);
    }
}
