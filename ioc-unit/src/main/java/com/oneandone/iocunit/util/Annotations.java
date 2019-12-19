/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2015-2019 Mickael Jeanroy
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p>
 * copied from package com.github.mjeanroy.dbunit.commons.reflection; dbunit-plus
 */
package com.oneandone.iocunit.util;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Static Annotation Utilities.
 */
public final class Annotations {

    /**
     * The list of packages that should not be scanned for meta-annotation (since these packages may not
     * contains annotations of DbUnit+!).
     */
    private static final List<String> BLACKLISTED_PACKAGES = asList(
            "java.lang.",
            "org.junit."
    );

    // Ensure non instantiation.
    private Annotations() {
    }

    /**
     * Find expected annotation on method, if annotation is defined.
     *
     * @param method The method.
     * @param annotationClass Annotation class to look for.
     * @param <T> Type of annotation.
     * @return Annotation if found, {@code null} otherwise.
     */
    public static <T extends Annotation> T findAnnotation(Method method, Class<T> annotationClass) {
        Collection<T> annotations = findAnnotationOn(method, annotationClass, new HashSet<>());
        return annotations.isEmpty() ? null : annotations.iterator().next();
    }

    /**
     * Find expected annotation on:
     * <ul>
     * <li>Method if annotation is defined.</li>
     * <li>Class if annotation is defined.</li>
     * </ul>
     *
     * @param klass Class.
     * @param method Method in given {@code class}.
     * @param annotationClass Annotation class to look for.
     * @param <T> Type of annotation.
     * @return Annotation if found, {@code null} otherwise.
     */
    public static <T extends Annotation> T findAnnotation(Class<?> klass, Method method, Class<T> annotationClass) {
        // First, search on method.
        if(method != null) {
            T annotation = findAnnotation(method, annotationClass);
            if(annotation != null) {
                return annotation;
            }
        }

        // Then, search on class.
        return findAnnotation(klass, annotationClass);
    }

    /**
     * Find expected annotation on class, or a class in the hierarchy.
     *
     * @param klass Class.
     * @param annotationClass Annotation class to look for.
     * @param <T> Type of annotation.
     * @return Annotation if found, {@code null} otherwise.
     */
    public static <T extends Annotation> T findAnnotation(Class<?> klass, Class<T> annotationClass) {
        Collection<T> annotations = findAnnotationOn(klass, annotationClass, new HashSet<>());
        return annotations.isEmpty() ? null : annotations.iterator().next();
    }

    /**
     * Find expected annotation on class, or a class in the hierarchy.
     *
     * @param klass Class.
     * @param annotationClass Annotation class to look for.
     * @param <T> Type of annotation.
     * @return Annotation if found, {@code null} otherwise.
     */
    public static <T extends Annotation> List<T> findAnnotations(Class<?> klass, Class<T> annotationClass) {
        return findAnnotationOn(klass, annotationClass, new HashSet<>());
    }

    /**
     * Find expected annotation on method.
     *
     * @param method The method.
     * @param annotationClass Annotation class to look for.
     * @param <T> Type of annotation.
     * @return Annotation if found, {@code null} otherwise.
     */
    public static <T extends Annotation> List<T> findAnnotations(Method method, Class<T> annotationClass) {
        return findAnnotationOn(method, annotationClass, new HashSet<>());
    }

    /**
     * Find expected annotation on given element.
     *
     * @param element Class.
     * @param annotationClass Annotation class to look for.
     * @param <T> Type of annotation.
     * @return Annotation if found, {@code null} otherwise.
     */
    private static <T extends Annotation> List<T> findAnnotationOn(AnnotatedElement element, Class<T> annotationClass, 
                                                                   Set<AnnotatedElement> doneElements) {
        final List<T> results = new ArrayList<>();

        if(element == null) {
            return emptyList();
        }

        if (doneElements.contains(element))
            return emptyList();
        
        doneElements.add(element);
        
        // Is it directly present?
        if(element.isAnnotationPresent(annotationClass)) {
            results.add(element.getAnnotation(annotationClass));
        }

        // Search for meta-annotation
        for (Annotation candidate : element.getAnnotations()) {
            Class<? extends Annotation> candidateAnnotationType = candidate.annotationType();
            if(shouldScan(candidateAnnotationType)) {
                List<T> result = findAnnotationOn(candidateAnnotationType, annotationClass, doneElements);
                if(!result.isEmpty()) {
                    results.add(result.get(0));
                }
            }
        }

        if(element instanceof Class) {
            Class<?> klass = (Class<?>) element;

            // Look on interfaces.
            for (Class<?> intf : klass.getInterfaces()) {
                if(shouldScan(intf)) {
                    final List<T> subResults = findAnnotationOn(intf, annotationClass, doneElements);
                    if(!subResults.isEmpty()) {
                        results.addAll(subResults);
                    }
                }
            }

            // Go up in the class hierarchy.
            Class<?> superClass = klass.getSuperclass();
            if(shouldScan(superClass)) {
                final List<T> subResults = findAnnotationOn(superClass, annotationClass, doneElements);
                if(!subResults.isEmpty()) {
                    results.addAll(subResults);
                }
            }

            // Search in outer class.
            Class<?> declaringClass = klass.getDeclaringClass();
            if(shouldScan(declaringClass)) {
                final List<T> subResults = findAnnotationOn(declaringClass, annotationClass, doneElements);
                if(!subResults.isEmpty()) {
                    results.addAll(subResults);
                }
            }
        }

        return results;
    }

    /**
     * Check if it is worth scanning this element (i.e there is a chance to find useful
     * annotation).
     *
     * @param elementType The element class type.
     * @return {@code true} if element should be scanned, {@code false} otherwise.
     */
    private static boolean shouldScan(Class<?> elementType) {
        if(elementType == null) {
            return false;
        }

        String name = elementType.getName();

        for (String pkg : BLACKLISTED_PACKAGES) {
            if(name.startsWith(pkg)) {
                return false;
            }
        }

        return true;
    }

    public static <T extends Annotation> T getMethodAnnotation(Class<?> c, Method m, Class<T> annotationClass) {
        if(!shouldScan(c)) {
            return null;
        }
        try {
            Method actMethod = c.getMethod(m.getName(), m.getParameterTypes());
            if(actMethod.isAnnotationPresent(annotationClass)) {
                return actMethod.getAnnotation(annotationClass);
            }
            T tmp = getMethodAnnotation(c.getSuperclass(), m, annotationClass);
            if(tmp != null) {
                return tmp;
            }
            for (Class i : c.getInterfaces()) {
                tmp = getMethodAnnotation(i, m, annotationClass);
                if(tmp != null) {
                    return tmp;
                }
            }
        } catch (NoSuchMethodException e) {
            ;
        }
        return null;
    }

}

