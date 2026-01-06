package com.beanit.iec61850bean.clientgui.util;

import java.util.Locale;
import java.util.ResourceBundle;

import com.beanit.iec61850bean.clientgui.UTF8Control;

public class MessageUtil {

    private static ResourceBundle messages = null;

    public static String getString(String key) {

        if (messages == null) {
            messages = ResourceBundle.getBundle("Messages", Locale.CHINESE, new UTF8Control());
        }
        return messages.getString(key);
    }
}
