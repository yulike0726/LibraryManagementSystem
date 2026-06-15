package com.library.dao;

import com.library.model.BorrowRecord;
import com.library.util.JsonUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 借阅记录数据访问层
 * 负责借阅记录数据的JSON文件读写
 */
public class BorrowRecordDao {

    private final Path filePath;

    public BorrowRecordDao(String dataDir) {
        this.filePath = Paths.get(dataDir, "borrow_records.json");
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            if (!Files.exists(filePath)) {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, "[]".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建借阅记录数据文件: " + filePath, e);
        }
    }

    /** 读取所有借阅记录 */
    public List<BorrowRecord> findAll() {
        try {
            String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            return JsonUtil.fromJsonList(json, BorrowRecord.class);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /** 保存所有借阅记录 */
    public void saveAll(List<BorrowRecord> records) {
        try {
            String json = JsonUtil.toJson(records);
            Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("保存借阅记录失败: " + filePath, e);
        }
    }

    /** 按记录ID查找 */
    public BorrowRecord findById(String recordId) {
        return findAll().stream()
                .filter(r -> r.getRecordId().equals(recordId))
                .findFirst().orElse(null);
    }

    /** 添加借阅记录 */
    public void add(BorrowRecord record) {
        List<BorrowRecord> records = findAll();
        records.add(record);
        saveAll(records);
    }

    /** 更新借阅记录 */
    public void update(BorrowRecord updated) {
        List<BorrowRecord> records = findAll();
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getRecordId().equals(updated.getRecordId())) {
                records.set(i, updated);
                break;
            }
        }
        saveAll(records);
    }
}
