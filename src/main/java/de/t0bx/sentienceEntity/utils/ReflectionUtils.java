/**
 *Creative Commons Attribution-NonCommercial 4.0 International Public License
 * By using this code, you agree to the following terms:
 * You are free to:
 * - Share — copy and redistribute the material in any medium or format
 * - Adapt — remix, transform, and build upon the material
 * Under the following terms:
 * 1. Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made.
 * 2. NonCommercial — You may not use the material for commercial purposes.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 * Full License Text: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 * ---
 * Copyright (c) 2025 t0bx
 * This work is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License.
 */

package de.t0bx.sentienceEntity.utils;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

public class ReflectionUtils {

    public static Field[] getFields(final Class<?> cls) {
        if (cls == null) {
            return new Field[0];
        }
        final Field[] declaredFields = cls.getDeclaredFields();
        for (final Field f : declaredFields) {
            f.setAccessible(true);
        }
        return declaredFields;
    }

    public static Field getField(final Class<?> cls, final String name) {
        if (cls == null) {
            return null;
        }
        try {
            final Field field = cls.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            if (cls.getSuperclass() != null) {
                return getField(cls.getSuperclass(), name);
            }
        }
        return null;
    }

    public static Field getField(final Class<?> cls, final Class<?> dataType, final int index) {
        if (dataType == null || cls == null) {
            return null;
        }
        int currentIndex = 0;
        for (final Field f : getFields(cls)) {
            if (dataType.isAssignableFrom(f.getType())) {
                if (currentIndex++ == index) {
                    return f;
                }
            }
        }
        if (cls.getSuperclass() != null) {
            return getField(cls.getSuperclass(), dataType, index);
        }
        return null;
    }


    public static int generateValidMinecraftEntityId() {
        try {
            Class<?> entityClass = Class.forName("net.minecraft.world.entity.Entity");
            Field entityCountField = getField(entityClass, "entityCount");
            if (entityCountField == null) {
                entityCountField = getField(entityClass, AtomicInteger.class, 0);
            }

            entityCountField.setAccessible(true);
            if (entityCountField.getType().equals(AtomicInteger.class)) {
                AtomicInteger atomicInteger = (AtomicInteger) entityCountField.get(null);
                return atomicInteger.incrementAndGet();
            } else {
                int id = entityCountField.getInt(null);
                entityCountField.set(null, id + 1);
                return id;
            }
        } catch (ClassNotFoundException | IllegalAccessException exception) {
            exception.printStackTrace();
            throw new RuntimeException(exception);
        }
    }
}
