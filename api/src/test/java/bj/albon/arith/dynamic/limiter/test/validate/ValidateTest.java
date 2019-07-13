package bj.albon.arith.dynamic.limiter.test.validate;

import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import org.apache.bval.jsr.ApacheValidationProvider;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;

/**
 * @author albon
 *         Date: 17-8-10
 *         Time: 下午4:42
 */
public class ValidateTest {

    public static void main(String[] args) {
        ValidatorFactory avf = Validation.byProvider(ApacheValidationProvider.class).configure()
                .buildValidatorFactory();
        Validator validator = avf.getValidator();

        DynamicProperties dynamicProperties = new DynamicProperties();
        dynamicProperties.setKey("validatorTest");
        dynamicProperties.setQpsNeedDecreaseLoadThreshold(0);
        dynamicProperties.setDynamicLimiterQpsFactor(BigDecimal.valueOf(1));
        dynamicProperties.setBucketSizeInMs(200);
        dynamicProperties.setContinuousSuccessNumCloseCircuitBreaker(0);
        Set<ConstraintViolation<DynamicProperties>> validateResult = validator.validate(dynamicProperties);

        for (ConstraintViolation<DynamicProperties> constraintViolation : validateResult) {
            System.out.println(constraintViolation.getPropertyPath() + " " + constraintViolation.getMessage());
        }
    }
}
