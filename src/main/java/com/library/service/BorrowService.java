package com.library.service;

import com.library.dao.BorrowRecordDao;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Reader;
import com.library.util.DateUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 借阅管理业务逻辑
 * 实现借书、还书、续借、超期检测、排行榜统计
 */
public class BorrowService {

    private final BorrowRecordDao recordDao;
    private final BookService bookService;
    private final ReaderService readerService;

    public BorrowService(BorrowRecordDao recordDao, BookService bookService, ReaderService readerService) {
        this.recordDao = recordDao;
        this.bookService = bookService;
        this.readerService = readerService;
    }

    /**
     * 借书操作
     * @return 结果信息，key="success"表示成功，否则返回错误信息
     */
    public Map<String, Object> borrowBook(String isbn, String readerId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 检查图书是否存在
        Book book = bookService.getByIsbn(isbn);
        if (book == null) {
            result.put("success", false);
            result.put("message", "图书不存在");
            return result;
        }

        // 2. 检查库存
        if (book.getAvailableCount() <= 0) {
            result.put("success", false);
            result.put("message", "该图书已全部借出，无可借库存");
            return result;
        }

        // 3. 检查读者是否存在
        Reader reader = readerService.getById(readerId);
        if (reader == null) {
            result.put("success", false);
            result.put("message", "读者不存在");
            return result;
        }

        // 4. 检查读者借阅数量是否已达上限
        if (!reader.canBorrow()) {
            result.put("success", false);
            result.put("message", "借阅数量已达上限（"
                    + (reader.getReaderType() == Reader.ReaderType.TEACHER ? "教师最多10本" : "学生最多5本") + "）");
            return result;
        }

        // 5. 检查是否已经借过同一本书（未归还）
        List<BorrowRecord> records = recordDao.findAll();
        boolean alreadyBorrowed = records.stream()
                .anyMatch(r -> r.getIsbn().equals(isbn) && r.getReaderId().equals(readerId)
                        && r.getStatus() != BorrowRecord.Status.RETURNED);
        if (alreadyBorrowed) {
            result.put("success", false);
            result.put("message", "您已经借过这本书且尚未归还");
            return result;
        }

        // 6. 执行借书
        String today = DateUtil.today();
        String recordId = UUID.randomUUID().toString().substring(0, 8);
        String dueDate = DateUtil.addDays(today, reader.getBorrowDays());

        BorrowRecord record = new BorrowRecord(recordId, isbn, readerId, today, dueDate);
        recordDao.add(record);

        // 更新库存和读者借阅数
        bookService.decreaseAvailable(isbn);
        readerService.incrementBorrows(readerId);

        result.put("success", true);
        result.put("message", "借书成功！应还日期：" + dueDate);
        result.put("recordId", recordId);
        result.put("dueDate", dueDate);
        return result;
    }

    /**
     * 还书操作
     */
    public Map<String, Object> returnBook(String recordId) {
        Map<String, Object> result = new HashMap<>();

        BorrowRecord record = recordDao.findById(recordId);
        if (record == null) {
            result.put("success", false);
            result.put("message", "借阅记录不存在");
            return result;
        }

        if (record.getStatus() == BorrowRecord.Status.RETURNED) {
            result.put("success", false);
            result.put("message", "该图书已归还");
            return result;
        }

        // 更新记录
        String today = DateUtil.today();
        record.setReturnDate(today);
        record.setStatus(BorrowRecord.Status.RETURNED);
        recordDao.update(record);

        // 恢复库存和读者借阅数
        bookService.increaseAvailable(record.getIsbn());
        readerService.decrementBorrows(record.getReaderId());

        // 检查是否超期
        long overdueDays = DateUtil.getOverdueDays(record.getDueDate());
        result.put("success", true);
        if (overdueDays > 0) {
            result.put("message", "还书成功！已超期 " + overdueDays + " 天");
            result.put("overdueDays", overdueDays);
        } else {
            result.put("message", "还书成功！");
        }

        return result;
    }

    /**
     * 续借操作
     */
    public Map<String, Object> renewBook(String recordId) {
        Map<String, Object> result = new HashMap<>();

        BorrowRecord record = recordDao.findById(recordId);
        if (record == null) {
            result.put("success", false);
            result.put("message", "借阅记录不存在");
            return result;
        }

        if (record.getStatus() != BorrowRecord.Status.BORROWED) {
            result.put("success", false);
            result.put("message", "该记录状态不允许续借");
            return result;
        }

        if (record.getRenewCount() >= 1) {
            result.put("success", false);
            result.put("message", "该书已续借过一次，不能再次续借");
            return result;
        }

        Reader reader = readerService.getById(record.getReaderId());
        if (reader == null) {
            result.put("success", false);
            result.put("message", "读者信息异常");
            return result;
        }

        // 从原到期日起延长借阅期限
        String newDueDate = DateUtil.addDays(record.getDueDate(), reader.getBorrowDays());
        record.setDueDate(newDueDate);
        record.setRenewCount(record.getRenewCount() + 1);
        recordDao.update(record);

        result.put("success", true);
        result.put("message", "续借成功！新的应还日期：" + newDueDate);
        result.put("newDueDate", newDueDate);
        return result;
    }

    /** 获取所有借阅记录 */
    public List<BorrowRecord> getAllRecords() {
        return recordDao.findAll();
    }

    /** 查询借阅记录 */
    public List<BorrowRecord> queryRecords(String readerId, String isbn, String status) {
        List<BorrowRecord> all = recordDao.findAll();
        return all.stream()
                .filter(r -> {
                    if (readerId != null && !readerId.isEmpty()) {
                        if (!r.getReaderId().equals(readerId)) return false;
                    }
                    if (isbn != null && !isbn.isEmpty()) {
                        if (!r.getIsbn().equals(isbn)) return false;
                    }
                    if (status != null && !status.isEmpty()) {
                        if (!r.getStatus().name().equals(status)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /** 获取超期未还记录 */
    public List<BorrowRecord> getOverdueRecords() {
        String today = DateUtil.today();
        return recordDao.findAll().stream()
                .filter(r -> r.getStatus() == BorrowRecord.Status.BORROWED)
                .peek(r -> {
                    if (DateUtil.isOverdue(r.getDueDate())) {
                        r.setStatus(BorrowRecord.Status.OVERDUE);
                    }
                })
                .filter(r -> DateUtil.isAfter(today, r.getDueDate()))
                .collect(Collectors.toList());
    }

    /** 获取热门图书排行榜（按借阅次数降序，取前N） */
    public List<Map<String, Object>> getPopularBooks(int topN) {
        Map<String, Long> borrowCount = recordDao.findAll().stream()
                .collect(Collectors.groupingBy(BorrowRecord::getIsbn, Collectors.counting()));

        // 使用链表将entry按借阅次数排序
        List<Map.Entry<String, Long>> sorted = new LinkedList<>(borrowCount.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, sorted.size()); i++) {
            Map.Entry<String, Long> entry = sorted.get(i);
            Book book = bookService.getByIsbn(entry.getKey());
            Map<String, Object> item = new HashMap<>();
            item.put("isbn", entry.getKey());
            item.put("title", book != null ? book.getTitle() : "未知图书");
            item.put("author", book != null ? book.getAuthor() : "");
            item.put("borrowCount", entry.getValue());
            item.put("rank", i + 1);
            result.add(item);
        }
        return result;
    }
}
