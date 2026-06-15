package com.library.model;

/**
 * 借阅记录数据模型
 * 每次借书生成一条记录，跟踪借阅状态
 */
public class BorrowRecord {
    /**
     * 借阅状态
     */
    public enum Status {
        BORROWED,   // 借出中（未还）
        RETURNED,   // 已归还
        OVERDUE     // 已超期
    }

    private String recordId;      // 记录ID（唯一键）
    private String isbn;          // 图书ISBN
    private String readerId;      // 读者ID
    private String borrowDate;    // 借书日期（yyyy-MM-dd）
    private String dueDate;       // 应还日期（yyyy-MM-dd）
    private String returnDate;    // 实际还书日期（null表示未还）
    private Status status;        // 借阅状态
    private int renewCount;       // 续借次数（最多1次）

    public BorrowRecord() {}

    public BorrowRecord(String recordId, String isbn, String readerId,
                        String borrowDate, String dueDate) {
        this.recordId = recordId;
        this.isbn = isbn;
        this.readerId = readerId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.status = Status.BORROWED;
        this.renewCount = 0;
    }

    // ========== Getters & Setters ==========

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }

    public String getBorrowDate() { return borrowDate; }
    public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getRenewCount() { return renewCount; }
    public void setRenewCount(int renewCount) { this.renewCount = renewCount; }

    @Override
    public String toString() {
        return "BorrowRecord{id='" + recordId + "', isbn='" + isbn + "', reader='" +
               readerId + "', status=" + status + "}";
    }
}
