package com.library.controller;

import com.library.model.Book;
import com.library.service.BookService;
import com.library.util.JsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书管理HTTP控制器
 * 处理 /api/books 相关请求
 */
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /** GET /api/books — 查询图书列表（支持 ?keyword=&sort=&order=&algo=） */
    public String listBooks(Map<String, String> params) {
        String keyword = params.get("keyword");
        String category = params.get("category");
        String sortField = params.get("sort");      // title, author, publishDate, availableCount
        String order = params.get("order");          // asc, desc
        String algo = params.get("algo");            // quick, bubble, merge

        List<Book> books = bookService.searchBooks(keyword, category);

        // 排序
        if (sortField != null && !sortField.isEmpty()) {
            BookService.SortField field;
            try {
                field = BookService.SortField.valueOf(sortField.toUpperCase());
            } catch (IllegalArgumentException e) {
                field = BookService.SortField.TITLE;
            }
            books = bookService.sortBooks(books, field,
                    !"desc".equalsIgnoreCase(order), algo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", books);
        result.put("total", books.size());
        return JsonUtil.toJson(result);
    }

    /** GET /api/books/{isbn} — 获取单本图书 */
    public String getBook(String isbn) {
        Book book = bookService.getByIsbn(isbn);
        Map<String, Object> result = new HashMap<>();
        if (book == null) {
            result.put("success", false);
            result.put("message", "图书不存在");
        } else {
            result.put("success", true);
            result.put("data", book);
        }
        return JsonUtil.toJson(result);
    }

    /** POST /api/books — 新增图书 */
    public String addBook(String body) {
        Book book = JsonUtil.fromJson(body, Book.class);
        Map<String, Object> result = new HashMap<>();
        boolean ok = bookService.addBook(book);
        result.put("success", ok);
        result.put("message", ok ? "图书添加成功" : "图书添加失败（ISBN已存在或为空）");
        return JsonUtil.toJson(result);
    }

    /** PUT /api/books/{isbn} — 修改图书 */
    public String updateBook(String isbn, String body) {
        Book book = JsonUtil.fromJson(body, Book.class);
        book.setIsbn(isbn);
        Map<String, Object> result = new HashMap<>();
        boolean ok = bookService.updateBook(book);
        result.put("success", ok);
        result.put("message", ok ? "图书修改成功" : "图书不存在");
        return JsonUtil.toJson(result);
    }

    /** DELETE /api/books/{isbn} — 删除图书 */
    public String deleteBook(String isbn) {
        Map<String, Object> result = new HashMap<>();
        boolean ok = bookService.deleteBook(isbn);
        result.put("success", ok);
        result.put("message", ok ? "图书删除成功" : "图书不存在");
        return JsonUtil.toJson(result);
    }

    /** POST /api/books/{isbn}/stock — 入库增加库存 */
    public String increaseStock(String isbn, String body) {
        Map<String, Object> reqMap = JsonUtil.fromJson(body, Map.class);
        int count = reqMap.get("count") instanceof Number
                ? ((Number) reqMap.get("count")).intValue() : 1;

        Map<String, Object> result = new HashMap<>();
        boolean ok = bookService.increaseStock(isbn, count);
        result.put("success", ok);
        result.put("message", ok ? "入库成功，增加 " + count + " 册" : "入库失败");
        return JsonUtil.toJson(result);
    }
}
