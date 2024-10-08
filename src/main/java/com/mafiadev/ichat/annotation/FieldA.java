package com.mafiadev.ichat.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_PARAMETER})
public @interface FieldA {
    String value() default "";

    String type() default "VARCHAR(255)";
}
