package bj.albon.arith.dynamic.limiter.model;

import bj.albon.arith.dynamic.limiter.config.validator.PropertiesValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author albon
 *         Date: 17-8-11
 *         Time: 下午2:36
 */
@Constraint(validatedBy = PropertiesValidator.class)
@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertiesValid {

    String message() default "dynamic properties validation error";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
