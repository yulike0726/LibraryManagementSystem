package com.library.util;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 简易JSON工具类
 * 不依赖第三方库，手动实现JSON序列化与反序列化
 * 支持基本类型、String、List、Map及自定义对象的嵌套
 */
public class JsonUtil {

    /**
     * 将对象序列化为JSON字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escape((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof List) return listToJson((List<?>) obj);
        if (obj instanceof Map) return mapToJson((Map<?, ?>) obj);
        if (obj instanceof Enum) return "\"" + ((Enum<?>) obj).name() + "\"";

        // 自定义对象：反射获取字段
        return objectToJson(obj);
    }

    /**
     * 从JSON字符串解析为指定类型的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) return null;
        JsonParser parser = new JsonParser(json.trim());
        Object value = parser.parseValue();
        return convertValue(value, clazz);
    }

    /**
     * 从JSON字符串解析为List
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();
        JsonParser parser = new JsonParser(json.trim());
        Object value = parser.parseValue();
        if (!(value instanceof List)) return new ArrayList<>();
        List<T> result = new ArrayList<>();
        for (Object item : (List<?>) value) {
            result.add(convertValue(item, clazz));
        }
        return result;
    }

    // ========== 序列化辅助方法 ==========

    private static String objectToJson(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        boolean first = true;
        for (Field f : fields) {
            int mod = f.getModifiers();
            // 跳过静态字段和合成字段，避免枚举常量等导致无限递归
            if (java.lang.reflect.Modifier.isStatic(mod)) continue;
            if (f.isSynthetic()) continue;
            try {
                f.setAccessible(true);
                Object value = f.get(obj);
                if (value == null) continue;
                if (!first) sb.append(",");
                sb.append("\"").append(f.getName()).append("\":").append(toJson(value));
                first = false;
            } catch (IllegalAccessException ignored) {}
        }
        sb.append("}");
        return sb.toString();
    }

    private static String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey().toString()).append("\":")
              .append(toJson(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ========== 反序列化辅助方法 ==========

    @SuppressWarnings("unchecked")
    private static <T> T convertValue(Object value, Class<T> clazz) {
        if (value == null) return null;
        if (clazz.isInstance(value)) return (T) value;
        if (clazz == String.class) return (T) value.toString();
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(((Number) value).intValue());
        }
        if (clazz == double.class || clazz == Double.class) {
            return (T) Double.valueOf(((Number) value).doubleValue());
        }
        if (clazz == boolean.class || clazz == Boolean.class) {
            return (T) value;
        }

        // 枚举类型
        if (clazz.isEnum() && value instanceof String) {
            for (T enumConst : clazz.getEnumConstants()) {
                if (enumConst.toString().equals(value)) return enumConst;
            }
        }

        // 自定义对象
        if (value instanceof Map) {
            return mapToObject((Map<String, Object>) value, clazz);
        }

        return null;
    }

    private static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                Object value = map.get(f.getName());
                if (value != null) {
                    f.set(obj, convertValue(value, f.getType()));
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create object from JSON: " + clazz.getName(), e);
        }
    }

    /**
     * 简易JSON解析器（递归下降）
     */
    private static class JsonParser {
        private final String json;
        private int pos;

        JsonParser(String json) {
            this.json = json;
            this.pos = 0;
        }

        Object parseValue() {
            skipWhitespace();
            if (pos >= json.length()) return null;
            char c = json.charAt(pos);
            if (c == '"') return parseString();
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') { pos += 4; return null; }
            return parseNumber();
        }

        String parseString() {
            pos++; // skip opening "
            StringBuilder sb = new StringBuilder();
            while (pos < json.length()) {
                char c = json.charAt(pos);
                if (c == '\\') {
                    pos++;
                    char next = json.charAt(pos);
                    switch (next) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        default: sb.append(next);
                    }
                } else if (c == '"') {
                    pos++;
                    return sb.toString();
                } else {
                    sb.append(c);
                }
                pos++;
            }
            return sb.toString();
        }

        Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // skip {
            skipWhitespace();
            if (json.charAt(pos) == '}') { pos++; return map; }
            while (pos < json.length()) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                pos++; // skip :
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (json.charAt(pos) == '}') { pos++; return map; }
                pos++; // skip ,
            }
            return map;
        }

        List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++; // skip [
            skipWhitespace();
            if (json.charAt(pos) == ']') { pos++; return list; }
            while (pos < json.length()) {
                list.add(parseValue());
                skipWhitespace();
                if (json.charAt(pos) == ']') { pos++; return list; }
                pos++; // skip ,
            }
            return list;
        }

        Number parseNumber() {
            int start = pos;
            while (pos < json.length() && (Character.isDigit(json.charAt(pos))
                    || json.charAt(pos) == '.' || json.charAt(pos) == '-' || json.charAt(pos) == 'e')) {
                pos++;
            }
            String numStr = json.substring(start, pos);
            if (numStr.contains(".")) return Double.parseDouble(numStr);
            return Integer.parseInt(numStr);
        }

        Boolean parseBoolean() {
            if (json.startsWith("true", pos)) { pos += 4; return true; }
            pos += 5; return false;
        }

        void skipWhitespace() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
        }
    }
}
