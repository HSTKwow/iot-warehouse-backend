package com.hstk.iot_warehouse.common.utils;

/**
 * 统一设备 ID 比较规则：
 * - 忽略大小写
 * - 忽略常见分隔符（: - . 空格等）
 */
public final class DeviceIdUtils {

    private DeviceIdUtils() {
    }

    public static String normalize(String deviceId) {
        if (deviceId == null) {
            return null;
        }

        String trimmed = deviceId.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        StringBuilder normalized = new StringBuilder(trimmed.length());
        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                normalized.append(Character.toUpperCase(ch));
            }
        }

        return normalized.length() == 0 ? null : normalized.toString();
    }
}
