package com.incubyte.salarymanagement.employee;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = SupportedCurrencyValidator.class)
@Target({FIELD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface SupportedCurrency {
    String message() default "currency must be one of USD, INR, GBP, EUR, CAD, or AUD";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
