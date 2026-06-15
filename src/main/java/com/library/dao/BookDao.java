package com.library.dao;

import com.library.model.Book;
import com.library.util.JsonUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书数据访问层
 * 负责图书数据的JSON文件读写
 */
public class BookDao {

    private final Path filePath;

    public BookDao(String dataDir) {
        this.filePath = Paths.get(dataDir, "books.json");
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            if (!Files.exists(filePath)) {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, "[]".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建图书数据文件: " + filePath, e);
        }
    }

    /** 读取所有图书 */
    public List<Book> findAll() {
        try {
            String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            return JsonUtil.fromJsonList(json, Book.class);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /** 保存所有图书（全量写入） */
    public void saveAll(List<Book> books) {
        try {
            String json = JsonUtil.toJson(books);
            Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("保存图书数据失败: " + filePath, e);
        }
    }

    /** 按ISBN查找图书 */
    public Book findByIsbn(String isbn) {
        return findAll().stream()
                .filter(b -> b.getIsbn().equals(isbn))
                .findFirst().orElse(null);
    }

    /** 添加一本图书 */
    public void add(Book book) {
        List<Book> books = findAll();
        books.add(book);
        saveAll(books);
    }

    /** 更新图书信息 */
    public void update(Book updated) {
        List<Book> books = findAll();
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getIsbn().equals(updated.getIsbn())) {
                books.set(i, updated);
                break;
            }
        }
        saveAll(books);
    }

    /** 删除图书 */
    public boolean delete(String isbn) {
        List<Book> books = findAll();
        boolean removed = books.removeIf(b -> b.getIsbn().equals(isbn));
        if (removed) saveAll(books);
        return removed;
    }
}
