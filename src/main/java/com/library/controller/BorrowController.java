package com.library.controller;

import com.library.model.BorrowRecord;
import com.library.service.BorrowService;
import com.library.util.JsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 借阅管理HTTP控制器
 * 处理 /api/borrow 等请求
 */
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /** POST /api/borrow — 借书 */
    public String borrowBook(String body) {
        Map<String, Object> reqMap = JsonUtil.fromJson(body, Map.class);
        String isbn = (String) reqMap.get("isbn");
        String readerId = (String) reqMap.get("readerId");

        if (isbn == null || readerId == null) {
            return JsonUtil.toJson(Map.of("success", false, "message", "参数不完整：需要isbn和readerId"));
        }

        Map<String, Object> result = borrowService.borrowBook(isbn, readerId);
        return JsonUtil.toJson(result);
    }

    /** POST /api/return/{recordId} — 还书 */
    public String returnBook(String recordId) {
        Map<String, Object> result = borrowService.returnBook(recordId);
        return JsonUtil.toJson(result);
    }

    /** POST /api/renew/{recordId} — 续借 */
    public String renewBook(String recordId) {
        Map<String, Object> result = borrowService.renewBook(recordId);
        return JsonUtil.toJson(result);
    }

    /** GET /api/borrow-records — 查询借阅记录 */
    public String listRecords(Map<String, String> params) {
        String readerId = params.get("readerId");
        String isbn = params.get("isbn");
        String status = params.get("status");

        List<BorrowRecord> records = borrowService.queryRecords(readerId, isbn, status);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", records);
        result.put("total", records.size());
        return JsonUtil.toJson(result);
    }

    /** GET /api/stats/popular-books — 热门图书排行榜 */
    public String popularBooks(Map<String, String> params) {
        int topN = 10;
        try {
            String n = params.get("top");
            if (n != null) topN = Integer.parseInt(n);
        } catch (NumberFormatException ignored) {}

        List<Map<String, Object>> books = borrowService.getPopularBooks(topN);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", books);
        return JsonUtil.toJson(result);
    }

    /** GET /api/stats/overdue — 超期未还列表 */
    public String overdueRecords(Map<String, String> params) {
        List<BorrowRecord> records = borrowService.getOverdueRecords();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", records);
        result.put("total", records.size());
        return JsonUtil.toJson(result);
    }
}
