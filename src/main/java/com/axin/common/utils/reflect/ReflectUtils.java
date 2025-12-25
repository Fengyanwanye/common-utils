package com.axin.common.utils.reflect;

import com.axin.common.core.text.Convert;
import com.axin.common.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.Date;

/**
 * @author fuchuanxin
 * @version 1.0
 * @description: TODO
 * @date 2025/12/23 16:23
 */
public class ReflectUtils {

    private static final String SETTER_PREFIX = "set";
    private static final String GETTER_PREFIX = "get";
    private static final String CGLIB_CLASS_SEPARATOR = "$$";
    private static final Logger logger = LoggerFactory.getLogger(ReflectUtils.class);

    public static <E> E invokeGetter(Object obj, String propertyName) {
        Object object = obj;
        for (String name : StringUtils.split(propertyName, ".")) {

            String getterMethodName = "get" + StringUtils.capitalize(name);
            object = invokeMethod(object, getterMethodName, new Class[0], new Object[0]);
        }
        return (E) object;
    }

    public static <E> void invokeSetter(Object obj, String propertyName, E value) {
        Object object = obj;
        String[] names = StringUtils.split(propertyName, ".");
        for (int i = 0; i < names.length; i++) {

            if (i < names.length - 1) {

                String getterMethodName = "get" + StringUtils.capitalize(names[i]);
                object = invokeMethod(object, getterMethodName, new Class[0], new Object[0]);
            } else {

                String setterMethodName = "set" + StringUtils.capitalize(names[i]);
                invokeMethodByName(object, setterMethodName, new Object[]{value});
            }
        }
    }

    public static <E> E getFieldValue(Object obj, String fieldName) {
        Field field = getAccessibleField(obj, fieldName);
        if (field == null) {

            logger.debug("在 [" + obj.getClass() + "] 中，没有找到 [" + fieldName + "] 字段 ");
            return null;
        }
        E result = null;

        try {
            result = (E) field.get(obj);
        } catch (IllegalAccessException e) {

            logger.error("不可能抛出的异常{}", e.getMessage());
        }
        return result;
    }

    public static <E> void setFieldValue(Object obj, String fieldName, E value) {
        Field field = getAccessibleField(obj, fieldName);
        if (field == null) {


            logger.debug("在 [" + obj.getClass() + "] 中，没有找到 [" + fieldName + "] 字段 ");

            return;
        }
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {

            logger.error("不可能抛出的异常: {}", e.getMessage());
        }
    }

    public static <E> E invokeMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object[] args) {
        if (obj == null || methodName == null) {
            return null;
        }
        Method method = getAccessibleMethod(obj, methodName, parameterTypes);
        if (method == null) {

            logger.debug("在 [" + obj.getClass() + "] 中，没有找到 [" + methodName + "] 方法 ");
            return null;
        }

        try {
            return (E) method.invoke(obj, args);
        } catch (Exception e) {

            String msg = "method: " + method + ", obj: " + obj + ", args: " + args + "";
            throw convertReflectionExceptionToUnchecked(msg, e);
        }
    }

    public static <E> E invokeMethodByName(Object obj, String methodName, Object[] args) {
        Method method = getAccessibleMethodByName(obj, methodName, args.length);
        if (method == null) {


            logger.debug("在 [" + obj.getClass() + "] 中，没有找到 [" + methodName + "] 方法 ");
            return null;
        }


        try {
            Class<?>[] cs = method.getParameterTypes();
            for (int i = 0; i < cs.length; i++) {

                if (args[i] != null && !args[i].getClass().equals(cs[i])) {
                    if (cs[i] == String.class) {

                        args[i] = Convert.toStr(args[i]);
                        if (StringUtils.endsWith((String) args[i], ".0")) {
                            args[i] = StringUtils.substringBefore((String) args[i], ".0");
                        }
                    } else if (cs[i] == Integer.class) {

                        args[i] = Convert.toInt(args[i]);
                    } else if (cs[i] == Long.class) {

                        args[i] = Convert.toLong(args[i]);
                    } else if (cs[i] == Double.class) {

                        args[i] = Convert.toDouble(args[i]);
                    } else if (cs[i] == Float.class) {

                        args[i] = Convert.toFloat(args[i]);
                    } else if (cs[i] == Date.class) {

                        if (args[i] instanceof String) {
                            args[i] = DateUtils.parseDate(args[i]);
                        } else {
                            args[i] = DateUtil.getJavaDate(((Double) args[i]).doubleValue());
                        }

                    } else if (cs[i] == boolean.class || cs[i] == Boolean.class) {

                        args[i] = Convert.toBool(args[i]);
                    }
                }
            }
            return (E) method.invoke(obj, args);
        } catch (Exception e) {

            String msg = "method: " + method + ", obj: " + obj + ", args: " + args + "";
            throw convertReflectionExceptionToUnchecked(msg, e);
        }
    }

    public static Field getAccessibleField(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        Validate.notBlank(fieldName, "fieldName can't be blank", new Object[0]);
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {


            try {
                Field field = superClass.getDeclaredField(fieldName);
                makeAccessible(field);
                return field;
            } catch (NoSuchFieldException e) {
            }
        }


        return null;
    }

    public static Method getAccessibleMethod(Object obj, String methodName, Class<?>... parameterTypes) {
        if (obj == null) {
            return null;
        }
        Validate.notBlank(methodName, "methodName can't be blank", new Object[0]);
        for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {


            try {
                Method method = searchType.getDeclaredMethod(methodName, parameterTypes);
                makeAccessible(method);
                return method;
            } catch (NoSuchMethodException e) {
            }
        }


        return null;
    }

    public static Method getAccessibleMethodByName(Object obj, String methodName, int argsNum) {
        if (obj == null) {
            return null;
        }
        Validate.notBlank(methodName, "methodName can't be blank", new Object[0]);
        for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {

            Method[] methods = searchType.getDeclaredMethods();
            for (Method method : methods) {

                if (method.getName().equals(methodName) && (method.getParameterTypes()).length == argsNum) {

                    makeAccessible(method);
                    return method;
                }
            }
        }
        return null;
    }

    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) &&
                !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
                Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    public static <T> Class<T> getClassGenricType(Class clazz) {
        return getClassGenricType(clazz, 0);
    }

    public static Class getClassGenricType(Class clazz, int index) {
        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {

            logger.debug(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {

            logger.debug("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: " + params.length);

            return Object.class;
        }
        if (!(params[index] instanceof Class)) {

            logger.debug(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }

        return (Class) params[index];
    }

    public static Class<?> getUserClass(Object instance) {
        if (instance == null) {
            throw new RuntimeException("Instance must not be null");
        }
        Class<?> clazz = instance.getClass();
        if (clazz != null && clazz.getName().contains("$$")) {

            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && !Object.class.equals(superClass)) {
                return superClass;
            }
        }
        return clazz;
    }

    public static RuntimeException convertReflectionExceptionToUnchecked(String msg, Exception e) {
        if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException || e instanceof NoSuchMethodException) {

            return new IllegalArgumentException(msg, e);
        }
        if (e instanceof InvocationTargetException) {
            return new RuntimeException(msg, ((InvocationTargetException) e).getTargetException());
        }
        return new RuntimeException(msg, e);
    }
}
