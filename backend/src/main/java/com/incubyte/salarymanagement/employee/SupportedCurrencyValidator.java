package com.incubyte.salarymanagement.employee;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class SupportedCurrencyValidator implements ConstraintValidator<SupportedCurrency, String> {
    public static final Set<String> VALUES = Set.of("USD", "INR", "GBP", "EUR", "CAD", "AUD");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && VALUES.contains(value.trim().toUpperCase(java.util.Locale.ROOT));
    }
}
