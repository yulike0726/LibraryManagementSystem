package com.library.controller;

import com.library.model.Reader;
import com.library.service.ReaderService;
import com.library.util.JsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 读者管理HTTP控制器
 * 处理 /api/readers 相关请求
 */
public class ReaderController {

    private final ReaderService readerService;

    public ReaderController(ReaderService readerService) {
        this.readerService = readerService;
    }

    /** GET /api/readers — 查询读者列表 */
    public String listReaders(Map<String, String> params) {
        String keyword = params.get("keyword");
        List<Reader> readers = readerService.searchReaders(keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", readers);
        result.put("total", readers.size());
        return JsonUtil.toJson(result);
    }

    /** GET /api/readers/{id} — 获取单个读者 */
    public String getReader(String readerId) {
        Reader reader = readerService.getById(readerId);
        Map<String, Object> result = new HashMap<>();
        if (reader == null) {
            result.put("success", false);
            result.put("message", "读者不存在");
        } else {
            result.put("success", true);
            result.put("data", reader);
        }
        return JsonUtil.toJson(result);
    }

    /** POST /api/readers — 注册读者 */
    public String registerReader(String body) {
        Reader reader = JsonUtil.fromJson(body, Reader.class);
        Map<String, Object> result = new HashMap<>();
        boolean ok = readerService.register(reader);
        result.put("success", ok);
        result.put("message", ok ? "读者注册成功" : "注册失败（ID已存在或为空）");
        return JsonUtil.toJson(result);
    }

    /** PUT /api/readers/{id} — 修改读者信息 */
    public String updateReader(String readerId, String body) {
        Reader reader = JsonUtil.fromJson(body, Reader.class);
        reader.setReaderId(readerId);
        Map<String, Object> result = new HashMap<>();
        boolean ok = readerService.updateReader(reader);
        result.put("success", ok);
        result.put("message", ok ? "读者信息更新成功" : "读者不存在");
        return JsonUtil.toJson(result);
    }

    /** DELETE /api/readers/{id} — 删除读者 */
    public String deleteReader(String readerId) {
        Map<String, Object> result = new HashMap<>();
        String error = readerService.deleteReader(readerId);
        if (error == null) {
            result.put("success", true);
            result.put("message", "读者删除成功");
        } else {
            result.put("success", false);
            result.put("message", error);
        }
        return JsonUtil.toJson(result);
    }
}
