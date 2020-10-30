package com.github.ollgei.plugin.mybatisplus.sensitive.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.ollgei.plugin.mybatisplus.sensitive.type.SensitiveType;

/**
 * 标注在字段上，用以说明字段上那些类型需要脱敏
 * 脱敏后，插件在写请求后对数据脱敏后存在数据库，对读请求不拦截
 * @author ;
 */
@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SensitiveField {
    /**
     * 脱敏类型
     * 不同的脱敏类型置换*的方式不同
     * @return SensitiveType
     */
    SensitiveType value();
}
