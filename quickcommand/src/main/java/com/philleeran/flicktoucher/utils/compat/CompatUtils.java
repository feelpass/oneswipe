/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.philleeran.flicktoucher.utils.compat;


import android.text.TextUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.philleeran.flicktoucher.utils.L;

public class CompatUtils {

    public static Class<?> getClass(String className) {
        if (TextUtils.isEmpty(className)) {
            return null;
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            L.e("Exception in getClass() " + e.toString());
        }

        return null;
    }

    public static Method getMethod(Class<?> targetClass, String name, Class<?>... parameterTypes) {
        if ((targetClass == null) || TextUtils.isEmpty(name)) {
            return null;
        }

        try {
            return targetClass.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            try {
                return targetClass.getDeclaredMethod(name, parameterTypes);
            } catch (NoSuchMethodException e2) {
                L.e("Exception in getDeclaredMethod() " + e2.toString() + " class:" + targetClass);
            }
            L.e("Exception in getMethod() " + e.toString() + " class:" + targetClass);
        }

        return null;
    }

    public static Field getField(Class<?> targetClass, String name) {
        if ((targetClass == null) || (TextUtils.isEmpty(name))) {
            return null;
        }

        try {
            return targetClass.getDeclaredField(name);
            
        } catch (NoSuchFieldException e) {
            L.e("Exception in getField() " + e.toString() + " class:" + targetClass);
        }

        return null;
    }

    public static Constructor<?> getConstructor(Class<?> targetClass, Class<?>... types) {
        if ((targetClass == null) || (types == null)) {
            return null;
        }

        try {
            return targetClass.getConstructor(types);
        } catch (NoSuchMethodException e) {
            try {
                return targetClass.getDeclaredConstructor(types);
            } catch (NoSuchMethodException e2) {
                L.e("Exception in getDeclaredConstructor() " + e2.toString() + " class:" + targetClass);
            }
            L.e("Exception in getConstructor() " + e.toString() + " class:" + targetClass);
        }

        return null;
    }

    public static Object newInstance(Constructor<?> constructor, Object... args) {
        if (constructor == null) {
            return null;
        }

        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            L.e("Exception in newInstance() " + e.getClass().getSimpleName() + " constructor:" + constructor + " args:" + Arrays.toString(args));
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object receiver, T defaultValue, Method method, Object... args) {
        if (method == null) {
            return defaultValue;
        }

        try {
            method.setAccessible(true);
            return (T)method.invoke(receiver, args);
        } catch (Exception e) {
            L.e(e);
            L.e("Exception in invoke() "//
                    + "exception:" + e.getClass().getSimpleName()//
                    + " receiver:" + receiver//
                    + " method:" + method//
                    + " message:" + e.getMessage()//
            );
        }

        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object receiver, T defaultValue, Field field) {
        if (field == null) {
            return defaultValue;
        }

        try {
            if (!field.isAccessible())
                field.setAccessible(true);
            return (T)field.get(receiver);
        } catch (Exception e) {
            L.e("Exception in getFieldValue() " + e.toString());
        }

        return defaultValue;
    }

    public static void setFieldValue(Object receiver, Field field, Object value) {
        if (field == null) {
            return;
        }

        try {
            if (!field.isAccessible())
                field.setAccessible(true);
            field.set(receiver, value);
        } catch (Exception e) {
            L.e("Exception in setFieldValue() "//
                    + "exception:" + e.toString()//
                    + " value:" + value//
                    + " type:" + value.getClass().getSimpleName()//
                    + " field:" + field//
                    + " message:" + e.getMessage()//
            );
        }
    }

    private CompatUtils() {
        // This class is non-instantiable.
    }
}
