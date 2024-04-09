package com.pddon.framework.easyapi.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ImageDataURISchemeMapper {
    private static Map<String, String> scheme2Extension = new HashMap<String, String>();
    private static Map<String, String> extension2Scheme = new HashMap<String, String>();

    static {
        initSchemeSupported();
    }

    public static String getScheme(String imageExtension) {
        if (imageExtension == null || imageExtension.isEmpty()) {
            return "";
        }
        String result = extension2Scheme.get(imageExtension.toLowerCase());
        return result == null ? "" : result;
    }

    public static String getScheme(File image) {
        if (image == null) {
            return "";
        }
        String name = image.getName();
        int lastPointIndex = name.lastIndexOf(".");
        return lastPointIndex < 0 ? "" : getScheme(name.substring(lastPointIndex + 1));
    }

    public static String getExtension(String dataUrlScheme) {
        return scheme2Extension.get(dataUrlScheme);
    }

    public static String getExtensionFromImageBase64(String imageBase64, String defaultExtension) {
        int firstComma = imageBase64.indexOf(",");
        if (firstComma < 0) {
            return defaultExtension;
        }
        return scheme2Extension.get(imageBase64.subSequence(0, firstComma + 1));
    }

    private static void initSchemeSupported() {
        addScheme("jpg", "data:image/jpg;base64,");
        addScheme("jpeg", "data:image/jpeg;base64,");
        addScheme("png", "data:image/png;base64,");
        addScheme("gif", "data:image/gif;base64,");
        addScheme("icon", "data:image/x-icon;base64,");
    }

    private static void addScheme(String extension, String dataUrl) {
        scheme2Extension.put(dataUrl, extension);
        extension2Scheme.put(extension, dataUrl);
    }
}

