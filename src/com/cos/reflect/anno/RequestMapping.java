package com.cos.reflect.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD}) // 타겟을 어디다 할지
@Retention(RetentionPolicy.RUNTIME) // 동작을 언제 할지
public @interface RequestMapping {
	String value();
}
