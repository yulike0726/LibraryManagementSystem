package com.library.service;

import com.library.dao.BookDao;
import com.library.model.Book;
import com.library.util.SortUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图书管理业务逻辑
 * 实现图书的增删改查、多条件检索、多字段排序
 */
public class BookService {

    private final BookDao bookDao;

    /** 排序方式枚举 */
    public enum SortField {
        TITLE, AUTHOR, PUBLISH_DATE, AVAILABLE_COUNT
    }

    public BookService(BookDao bookDao) {
        this.bookDao = bookDao;
    }

    /** 获取所有图书 */
    public List<Book> getAllBooks() {
        return bookDao.findAll();
    }

    /** 按ISBN查找 */
    public Book getByIsbn(String isbn) {
        return bookDao.findByIsbn(isbn);
    }

    /** 添加图书 */
    public boolean addBook(Book book) {
        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) return false;
        if (bookDao.findByIsbn(book.getIsbn()) != null) return false; // ISBN已存在
        bookDao.add(book);
        return true;
    }

    /** 更新图书 */
    public boolean updateBook(Book updated) {
        Book existing = bookDao.findByIsbn(updated.getIsbn());
        if (existing == null) return false;
        // 保持借出数量一致：已借出 = 总册数 - 可借数
        int borrowed = existing.getTotalCount() - existing.getAvailableCount();
        updated.setAvailableCount(Math.max(0, updated.getTotalCount() - borrowed));
        bookDao.update(updated);
        return true;
    }

    /** 删除图书 */
    public boolean deleteBook(String isbn) {
        return bookDao.delete(isbn);
    }

    /**
     * 多条件检索图书
     * @param keyword 关键词（匹配书名、作者、ISBN、分类）
     * @param category 分类筛选（null表示不限）
     * @return 匹配的图书列表
     */
    public List<Book> searchBooks(String keyword, String category) {
        List<Book> all = bookDao.findAll();
        return all.stream()
                .filter(b -> {
                    // 分类筛选
                    if (category != null && !category.isEmpty()) {
                        if (!b.getCategory().contains(category)) return false;
                    }
                    // 关键词匹配（模糊搜索）
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        String kw = keyword.toLowerCase().trim();
                        return b.getTitle().toLowerCase().contains(kw)
                                || b.getAuthor().toLowerCase().contains(kw)
                                || b.getIsbn().contains(kw)
                                || b.getCategory().toLowerCase().contains(kw);
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 对图书列表进行排序
     * @param books 待排序列表
     * @param field 排序字段
     * @param ascending true=升序, false=降序
     * @param algorithm 排序算法: "quick"(默认), "bubble", "merge"
     */
    public List<Book> sortBooks(List<Book> books, SortField field, boolean ascending, String algorithm) {
        List<Book> result = new ArrayList<>(books);
        Comparator<Book> cmp = getComparator(field, ascending);

        switch (algorithm != null ? algorithm.toLowerCase() : "quick") {
            case "bubble":
                SortUtil.bubbleSort(result, cmp);
                break;
            case "merge":
                SortUtil.mergeSort(result, cmp);
                break;
            default:
                SortUtil.quickSort(result, cmp);
        }
        return result;
    }

    /** 获取Comparator */
    private Comparator<Book> getComparator(SortField field, boolean ascending) {
        Comparator<Book> cmp;
        switch (field) {
            case TITLE:
                cmp = SortUtil.byString(Book::getTitle);
                break;
            case AUTHOR:
                cmp = SortUtil.byString(Book::getAuthor);
                break;
            case PUBLISH_DATE:
                cmp = SortUtil.byString(Book::getPublishDate);
                break;
            case AVAILABLE_COUNT:
                cmp = SortUtil.byInt(Book::getAvailableCount);
                break;
            default:
                cmp = SortUtil.byString(Book::getTitle);
        }
        return ascending ? cmp : cmp.reversed();
    }

    /** 入库（增加库存） */
    public boolean increaseStock(String isbn, int count) {
        Book book = bookDao.findByIsbn(isbn);
        if (book == null || count <= 0) return false;
        book.setTotalCount(book.getTotalCount() + count);
        book.setAvailableCount(book.getAvailableCount() + count);
        bookDao.update(book);
        return true;
    }

    /** 减少可借数量（借书时调用） */
    public boolean decreaseAvailable(String isbn) {
        Book book = bookDao.findByIsbn(isbn);
        if (book == null || book.getAvailableCount() <= 0) return false;
        book.setAvailableCount(book.getAvailableCount() - 1);
        bookDao.update(book);
        return true;
    }

    /** 增加可借数量（还书时调用） */
    public boolean increaseAvailable(String isbn) {
        Book book = bookDao.findByIsbn(isbn);
        if (book == null) return false;
        book.setAvailableCount(book.getAvailableCount() + 1);
        bookDao.update(book);
        return true;
    }
}
