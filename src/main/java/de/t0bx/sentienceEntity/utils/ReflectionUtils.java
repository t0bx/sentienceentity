/**
 SentienceEntity API License v1.1
 Copyright (c) 2025 (t0bx)

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to use, copy, modify, and integrate the Software into their own projects, including commercial and closed-source projects, subject to the following conditions:

 1. Attribution:
 You must give appropriate credit to the original author ("Tobias Schuster" or "t0bx"), provide a link to the source or official page if available, and indicate if changes were made. You must do so in a reasonable and visible manner, such as in your plugin.yml, README, or about page.

 2. No Redistribution or Resale:
 You may NOT sell, redistribute, or otherwise make the original Software or modified standalone versions of it available as a product (free or paid), plugin, or downloadable file, unless you have received prior written permission from the author. This includes publishing the plugin on any marketplace (e.g., SpigotMC, MC-Market, Polymart) or including it in paid bundles.

 3. Use as Dependency/API:
 You are allowed to use this Software as a dependency or library in your own plugin or project, including in paid products, as long as attribution is given and the Software itself is not being sold or published separately.

 4. No Misrepresentation:
 You may not misrepresent the origin of the Software. You must clearly distinguish your own modifications from the original work. The original author's name may not be removed from the source files or documentation.

 5. License Retention:
 This license notice and all conditions must be preserved in all copies or substantial portions of the Software.

 6. Disclaimer:
 THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY ARISING FROM THE USE OF THIS SOFTWARE.

 ---

 Summary (non-binding):
 You may use this plugin in your projects, even commercially, but you may not resell or republish it. Always give credit to t0bx.
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
