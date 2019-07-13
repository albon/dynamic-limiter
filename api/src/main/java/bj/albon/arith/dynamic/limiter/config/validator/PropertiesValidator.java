package bj.albon.arith.dynamic.limiter.config.validator;

import bj.albon.arith.dynamic.limiter.config.DynamicProperties;
import bj.albon.arith.dynamic.limiter.config.DefaultConfig;
import bj.albon.arith.dynamic.limiter.model.PropertiesValid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author albon
 *         Date: 17-8-11
 *         Time: 下午2:40
 */
public class PropertiesValidator implements ConstraintValidator<PropertiesValid, DynamicProperties> {
    @Override
    public void initialize(PropertiesValid constraintAnnotation) {

    }

    @Override
    public boolean isValid(DynamicProperties properties, ConstraintValidatorContext context) {
        boolean windowParamIsOk = properties.getWindowSizeInSecond() * DefaultConfig.SECOND_IN_MS
                % properties.getBucketSizeInMs() == 0;

        if (!windowParamIsOk) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("配置: %s, window 大小必须是 bucket 的倍数", properties.getKey())).addConstraintViolation();
            return false;
        }

        boolean loadThresholdIsOk = properties.getQpsNeedLimitLoadThreshold() <= properties
                .getQpsNeedDecreaseLoadThreshold();
        if (!loadThresholdIsOk) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("配置: %s, qps.need.decrease.load.threshold 必须大于或等于 qps.need.limit.load.threshold",
                            properties.getKey()))
                    .addConstraintViolation();
            return false;
        }

        boolean timeThresholdIsOk = properties.getQpsNeedLimitTimeInMsThreshold() <= properties
                .getQpsNeedDecreaseTimeInMsThreshold();
        if (!timeThresholdIsOk) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(String.format(
                    "配置: %s, qps.need.decrease.time.in.ms.threshold 必须大于或等于 qps.need.limit.time.in.ms.threshold",
                    properties.getKey())).addConstraintViolation();
            return false;
        }

        return true;
    }
}
