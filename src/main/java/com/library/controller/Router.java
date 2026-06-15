package com.library.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 路由分发器
 * 解析HTTP请求，根据路径和方法分发到对应的Controller
 */
public class Router implements HttpHandler {

    private final BookController bookController;
    private final ReaderController readerController;
    private final BorrowController borrowController;
    private final String webRoot;

    public Router(BookController bookController, ReaderController readerController,
                  BorrowController borrowController, String webRoot) {
        this.bookController = bookController;
        this.readerController = readerController;
        this.borrowController = borrowController;
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // CORS 头
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getRawQuery());

            String response;

            // ========== API 路由 ==========
            if (path.startsWith("/api/")) {
                String apiPath = path.substring(4); // remove /api

                // 静态文件
                if (!apiPath.startsWith("books") && !apiPath.startsWith("readers")
                        && !apiPath.startsWith("borrow") && !apiPath.startsWith("return")
                        && !apiPath.startsWith("renew") && !apiPath.startsWith("stats")
                        && !apiPath.startsWith("borrow-records")) {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.close();
                    return;
                }

                // 读取请求体
                String body = readBody(exchange);

                // ---- 图书相关 ----
                if (apiPath.equals("books") && "GET".equals(method)) {
                    response = bookController.listBooks(params);
                } else if (apiPath.equals("books") && "POST".equals(method)) {
                    response = bookController.addBook(body);
                } else if (apiPath.matches("books/[^/]+/stock") && "POST".equals(method)) {
                    String isbn = extractPathSegment(apiPath, 1);
                    response = bookController.increaseStock(isbn, body);
                } else if (apiPath.matches("books/[^/]+") && "GET".equals(method)) {
                    String isbn = extractPathSegment(apiPath, 1);
                    response = bookController.getBook(isbn);
                } else if (apiPath.matches("books/[^/]+") && "PUT".equals(method)) {
                    String isbn = extractPathSegment(apiPath, 1);
                    response = bookController.updateBook(isbn, body);
                } else if (apiPath.matches("books/[^/]+") && "DELETE".equals(method)) {
                    String isbn = extractPathSegment(apiPath, 1);
                    response = bookController.deleteBook(isbn);

                // ---- 读者相关 ----
                } else if (apiPath.equals("readers") && "GET".equals(method)) {
                    response = readerController.listReaders(params);
                } else if (apiPath.equals("readers") && "POST".equals(method)) {
                    response = readerController.registerReader(body);
                } else if (apiPath.matches("readers/[^/]+") && "GET".equals(method)) {
                    String id = extractPathSegment(apiPath, 1);
                    response = readerController.getReader(id);
                } else if (apiPath.matches("readers/[^/]+") && "PUT".equals(method)) {
                    String id = extractPathSegment(apiPath, 1);
                    response = readerController.updateReader(id, body);
                } else if (apiPath.matches("readers/[^/]+") && "DELETE".equals(method)) {
                    String id = extractPathSegment(apiPath, 1);
                    response = readerController.deleteReader(id);

                // ---- 借阅相关 ----
                } else if (apiPath.equals("borrow") && "POST".equals(method)) {
                    response = borrowController.borrowBook(body);
                } else if (apiPath.matches("return/[^/]+") && "POST".equals(method)) {
                    String recordId = extractPathSegment(apiPath, 1);
                    response = borrowController.returnBook(recordId);
                } else if (apiPath.matches("renew/[^/]+") && "POST".equals(method)) {
                    String recordId = extractPathSegment(apiPath, 1);
                    response = borrowController.renewBook(recordId);
                } else if (apiPath.equals("borrow-records") && "GET".equals(method)) {
                    response = borrowController.listRecords(params);
                } else if (apiPath.equals("stats/popular-books") && "GET".equals(method)) {
                    response = borrowController.popularBooks(params);
                } else if (apiPath.equals("stats/overdue") && "GET".equals(method)) {
                    response = borrowController.overdueRecords(params);
                } else {
                    response = "{\"success\":false,\"message\":\"未知API\"}";
                }

                sendJsonResponse(exchange, 200, response);

            } else {
                // ========== 静态文件服务 ==========
                serveStaticFile(exchange, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500,
                    "{\"success\":false,\"message\":\"服务器内部错误: " + escapeJson(e.getMessage()) + "\"}");
        }
    }

    /** 提供静态文件（前端页面） */
    private void serveStaticFile(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }
        File file = new File(webRoot, path);

        // 安全检查：防止目录穿越
        if (!file.getCanonicalPath().startsWith(new File(webRoot).getCanonicalPath())) {
            exchange.sendResponseHeaders(403, 0);
            exchange.close();
            return;
        }

        if (!file.exists() || !file.isFile()) {
            String response = "<h1>404 Not Found</h1>";
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(404, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
            return;
        }

        // 根据扩展名设置Content-Type
        String contentType = getContentType(file.getName());
        exchange.getResponseHeaders().add("Content-Type", contentType);

        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /** 发送JSON响应 */
    private void sendJsonResponse(HttpExchange exchange, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /** 解析查询参数 */
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        try {
            for (String pair : query.split("&")) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    params.put(key, value);
                }
            }
        } catch (Exception ignored) {}
        return params;
    }

    /** 读取请求体 */
    private String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    /** 提取路径段，如 "books/9781234567890" -> segments[0]="books", segments[1]="9781234567890" */
    private String extractPathSegment(String path, int index) {
        String[] segments = path.split("/");
        if (index < segments.length) {
            return segments[index];
        }
        return "";
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html; charset=UTF-8";
        if (fileName.endsWith(".css")) return "text/css; charset=UTF-8";
        if (fileName.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (fileName.endsWith(".json")) return "application/json; charset=UTF-8";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
