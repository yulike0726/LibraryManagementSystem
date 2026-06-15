package com.library.service;

import com.library.dao.ReaderDao;
import com.library.model.Reader;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 读者管理业务逻辑
 * 实现读者信息的增删改查、借阅权限管理
 */
public class ReaderService {

    private final ReaderDao readerDao;

    public ReaderService(ReaderDao readerDao) {
        this.readerDao = readerDao;
    }

    /** 获取所有读者 */
    public List<Reader> getAllReaders() {
        return readerDao.findAll();
    }

    /** 按ID查找 */
    public Reader getById(String readerId) {
        return readerDao.findById(readerId);
    }

    /** 注册读者（管理员添加时，若无密码则设置默认密码） */
    public boolean register(Reader reader) {
        if (reader.getReaderId() == null || reader.getReaderId().trim().isEmpty()) return false;
        if (readerDao.findById(reader.getReaderId()) != null) return false; // ID已存在
        if (reader.getPassword() == null || reader.getPassword().trim().isEmpty()) {
            reader.setPassword("123456"); // 默认密码
        }
        readerDao.add(reader);
        return true;
    }

    /** 更新读者信息 */
    public boolean updateReader(Reader updated) {
        Reader existing = readerDao.findById(updated.getReaderId());
        if (existing == null) return false;
        readerDao.update(updated);
        return true;
    }

    /** 删除读者（不能删除有未还书的读者） */
    public String deleteReader(String readerId) {
        Reader reader = readerDao.findById(readerId);
        if (reader == null) return "读者不存在";
        if (reader.getCurrentBorrows() > 0) return "该读者还有未归还的图书，无法删除";
        readerDao.delete(readerId);
        return null; // null表示成功
    }

    /** 搜索读者 */
    public List<Reader> searchReaders(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return readerDao.findAll();
        }
        String kw = keyword.toLowerCase().trim();
        return readerDao.findAll().stream()
                .filter(r -> r.getReaderId().toLowerCase().contains(kw)
                        || r.getName().toLowerCase().contains(kw)
                        || r.getDepartment().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    /** 增加当前借阅数 */
    public boolean incrementBorrows(String readerId) {
        Reader reader = readerDao.findById(readerId);
        if (reader == null || !reader.canBorrow()) return false;
        reader.setCurrentBorrows(reader.getCurrentBorrows() + 1);
        readerDao.update(reader);
        return true;
    }

    /** 减少当前借阅数 */
    public boolean decrementBorrows(String readerId) {
        Reader reader = readerDao.findById(readerId);
        if (reader == null || reader.getCurrentBorrows() <= 0) return false;
        reader.setCurrentBorrows(reader.getCurrentBorrows() - 1);
        readerDao.update(reader);
        return true;
    }
}
