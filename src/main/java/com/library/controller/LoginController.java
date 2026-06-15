package com.library.controller;

import com.library.model.Reader;
import com.library.service.ReaderService;
import com.library.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 * 处理用户登录和读者自助注册
 */
public class LoginController {

    private final ReaderService readerService;
    // 管理员默认账号
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    public LoginController(ReaderService readerService) {
        this.readerService = readerService;
    }

    /**
     * POST /api/login — 统一登录入口
     * body: { "username": "admin" or readerId, "password": "..." }
     */
    public String login(String body) {
        Map<String, Object> reqMap = JsonUtil.fromJson(body, Map.class);
        String username = (String) reqMap.get("username");
        String password = (String) reqMap.get("password");

        Map<String, Object> result = new HashMap<>();

        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请输入用户名/读者ID");
            return JsonUtil.toJson(result);
        }

        // 管理员登录
        if (ADMIN_USERNAME.equals(username.trim())) {
            if (ADMIN_PASSWORD.equals(password)) {
                result.put("success", true);
                result.put("role", "admin");
                result.put("name", "管理员");
                result.put("message", "管理员登录成功");
            } else {
                result.put("success", false);
                result.put("message", "管理员密码错误");
            }
            return JsonUtil.toJson(result);
        }

        // 读者登录
        Reader reader = readerService.getById(username.trim());
        if (reader == null) {
            result.put("success", false);
            result.put("message", "读者ID不存在");
            return JsonUtil.toJson(result);
        }
        if (reader.getPassword() == null || !reader.getPassword().equals(password)) {
            result.put("success", false);
            result.put("message", "密码错误");
            return JsonUtil.toJson(result);
        }

        result.put("success", true);
        result.put("role", "reader");
        result.put("readerId", reader.getReaderId());
        result.put("name", reader.getName());
        result.put("readerType", reader.getReaderType().name());
        result.put("currentBorrows", reader.getCurrentBorrows());
        result.put("maxBorrows", reader.getMaxBorrowLimit());
        result.put("message", "登录成功");
        return JsonUtil.toJson(result);
    }

    /**
     * POST /api/register — 读者自助注册
     * body: { "readerId": "...", "name": "...", "password": "...", ... }
     */
    public String selfRegister(String body) {
        try {
            Reader reader = JsonUtil.fromJson(body, Reader.class);
            Map<String, Object> result = new HashMap<>();

            // 基本校验
            if (reader.getReaderId() == null || reader.getReaderId().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "学号/工号不能为空");
                return JsonUtil.toJson(result);
            }
            if (reader.getName() == null || reader.getName().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "姓名不能为空");
                return JsonUtil.toJson(result);
            }
            if (reader.getPassword() == null || reader.getPassword().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "密码不能为空");
                return JsonUtil.toJson(result);
            }

            // 不允许注册 admin 作为 ID
            if ("admin".equalsIgnoreCase(reader.getReaderId().trim())) {
                result.put("success", false);
                result.put("message", "该ID已被占用");
                return JsonUtil.toJson(result);
            }

            boolean ok = readerService.register(reader);
            result.put("success", ok);
            result.put("message", ok ? "注册成功，请返回登录" : "该ID已被注册");
            return JsonUtil.toJson(result);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "注册失败: " + e.getMessage());
            return JsonUtil.toJson(err);
        }
    }
}
