package com.idroi.marketsense.common;

/**
 * Created by daniel.hsieh on 2018/5/11.
 */

import android.content.SharedPreferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection utils to call SharedPreferences$Editor.apply when possible,
 * falling back to commit when apply isn't available.
 */
public class SharedPreferencesCompat {
    private static final Method sApplyMethod = findApplyMethod();

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    private static Method findApplyMethod() {
        try {
            Class cls = SharedPreferences.Editor.class;
            return cls.getMethod("apply");
        } catch (NoSuchMethodException unused) {
            // fall through
        }
        return null;
    }

    public static void apply(SharedPreferences.Editor editor) {
        if (sApplyMethod != null) {
            try {
                sApplyMethod.invoke(editor);
                return;
            } catch (InvocationTargetException | IllegalAccessException unused) {
                // fall through
            }
        }
        editor.commit();
    }
}

