package com.apiautomation.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/** Applies {@link RetryAnalyzer} to every test without annotating each one by hand. */
public class RetryTransformer implements IAnnotationTransformer {

    @SuppressWarnings("rawtypes")
    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor constructor, Method method) {
        if (annotation.getRetryAnalyzerClass() == null
                || annotation.getRetryAnalyzerClass().equals(org.testng.internal.annotations.DisabledRetryAnalyzer.class)) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }
}
