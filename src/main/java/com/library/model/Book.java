package com.library.model;

/**
 * 图书数据模型
 * 存储图书的基本信息与库存状态
 */
public class Book {
    private String isbn;          // ISBN编号（唯一键）
    private String title;         // 书名
    private String author;        // 作者
    private String category;      // 分类（如：计算机科学/编程语言/Java）
    private int totalCount;       // 总册数
    private int availableCount;   // 可借册数
    private String publisher;     // 出版社
    private String publishDate;   // 出版日期
    private String description;   // 简介

    public Book() {}

    public Book(String isbn, String title, String author, String category,
                int totalCount, String publisher, String publishDate, String description) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.totalCount = totalCount;
        this.availableCount = totalCount; // 初始时可借数等于总册数
        this.publisher = publisher;
        this.publishDate = publishDate;
        this.description = description;
    }

    // ========== Getters & Setters ==========

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public int getAvailableCount() { return availableCount; }
    public void setAvailableCount(int availableCount) { this.availableCount = availableCount; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Book{isbn='" + isbn + "', title='" + title + "', author='" + author +
               "', category='" + category + "', available=" + availableCount + "/" + totalCount + "}";
    }
}
