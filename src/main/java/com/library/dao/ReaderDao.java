package com.library.dao;

import com.library.model.Reader;
import com.library.util.JsonUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 读者数据访问层
 * 负责读者数据的JSON文件读写
 */
public class ReaderDao {

    private final Path filePath;

    public ReaderDao(String dataDir) {
        this.filePath = Paths.get(dataDir, "readers.json");
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            if (!Files.exists(filePath)) {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, "[]".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建读者数据文件: " + filePath, e);
        }
    }

    /** 读取所有读者 */
    public List<Reader> findAll() {
        try {
            String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            return JsonUtil.fromJsonList(json, Reader.class);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /** 保存所有读者 */
    public void saveAll(List<Reader> readers) {
        try {
            String json = JsonUtil.toJson(readers);
            Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("保存读者数据失败: " + filePath, e);
        }
    }

    /** 按读者ID查找 */
    public Reader findById(String readerId) {
        return findAll().stream()
                .filter(r -> r.getReaderId().equals(readerId))
                .findFirst().orElse(null);
    }

    /** 添加读者 */
    public void add(Reader reader) {
        List<Reader> readers = findAll();
        readers.add(reader);
        saveAll(readers);
    }

    /** 更新读者信息 */
    public void update(Reader updated) {
        List<Reader> readers = findAll();
        for (int i = 0; i < readers.size(); i++) {
            if (readers.get(i).getReaderId().equals(updated.getReaderId())) {
                readers.set(i, updated);
                break;
            }
        }
        saveAll(readers);
    }

    /** 删除读者 */
    public boolean delete(String readerId) {
        List<Reader> readers = findAll();
        boolean removed = readers.removeIf(r -> r.getReaderId().equals(readerId));
        if (removed) saveAll(readers);
        return removed;
    }
}
