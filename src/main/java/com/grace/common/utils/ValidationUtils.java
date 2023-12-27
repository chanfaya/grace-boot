package com.grace.common.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.grace.common.exception.CustomException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * bean对象属性验证
 *
 * @author chanfa
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtils {

    private static final Validator VALID = SpringUtil.getBean(Validator.class);

    public static <T> void validate(T object, Class<?>... groups) {
        Set<ConstraintViolation<T>> validate = VALID.validate(object, groups);
        if (!validate.isEmpty()) {
            log.error("参数校验异常：{}", validate);
            throw new CustomException("参数校验异常");
        }
    }
}
