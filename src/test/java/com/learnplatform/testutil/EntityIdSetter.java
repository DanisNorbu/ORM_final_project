package com.learnplatform.testutil;

import java.lang.reflect.Field;

public final class EntityIdSetter {

    private EntityIdSetter() {
    }

    public static void setId(Object entity, Long id) {
        try {
            Field f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Entity has no 'id' field: " + entity.getClass(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot set id via reflection", e);
        }
    }
}
