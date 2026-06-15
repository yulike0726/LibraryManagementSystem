package com.library.model;

/**
 * 读者数据模型
 * 存储读者个人信息及借阅权限
 */
public class Reader {
    /**
     * 读者类型枚举：学生最多借5本、借期30天；教师最多借10本、借期60天
     */
    public enum ReaderType {
        STUDENT, TEACHER
    }

    private String readerId;      // 读者ID（学号/工号，唯一键）
    private String name;          // 姓名
    private String department;    // 院系/部门
    private ReaderType readerType; // 读者类型
    private String phone;         // 联系电话
    private String password;      // 登录密码
    private int currentBorrows;   // 当前借阅数

    public Reader() {}

    public Reader(String readerId, String name, String department,
                  ReaderType readerType, String phone, String password) {
        this.readerId = readerId;
        this.name = name;
        this.department = department;
        this.readerType = readerType;
        this.phone = phone;
        this.password = password;
        this.currentBorrows = 0;
    }

    /** 获取该类型读者的最大借阅数量 */
    public int getMaxBorrowLimit() {
        return readerType == ReaderType.TEACHER ? 10 : 5;
    }

    /** 获取该类型读者的借阅期限（天） */
    public int getBorrowDays() {
        return readerType == ReaderType.TEACHER ? 60 : 30;
    }

    /** 是否还能借书 */
    public boolean canBorrow() {
        return currentBorrows < getMaxBorrowLimit();
    }

    // ========== Getters & Setters ==========

    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public ReaderType getReaderType() { return readerType; }
    public void setReaderType(ReaderType readerType) { this.readerType = readerType; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getCurrentBorrows() { return currentBorrows; }
    public void setCurrentBorrows(int currentBorrows) { this.currentBorrows = currentBorrows; }

    @Override
    public String toString() {
        return "Reader{id='" + readerId + "', name='" + name + "', type=" + readerType +
               ", borrows=" + currentBorrows + "}";
    }
}
