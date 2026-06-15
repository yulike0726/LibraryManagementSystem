package com.library;

import com.library.controller.BookController;
import com.library.controller.BorrowController;
import com.library.controller.LoginController;
import com.library.controller.ReaderController;
import com.library.controller.Router;
import com.library.dao.BookDao;
import com.library.dao.BorrowRecordDao;
import com.library.dao.ReaderDao;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.ReaderService;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 图书管理系统 — 主入口
 * 启动内嵌HTTP服务器，提供Web管理界面和RESTful API
 *
 * 启动方式: java -cp bin com.library.App
 * 访问地址: http://localhost:8080
 */
public class App {

    public static void main(String[] args) throws IOException {
        // 确定数据目录和Web根目录
        String baseDir = System.getProperty("app.dir", ".");
        String dataDir = baseDir + File.separator + "data";
        String webRoot = baseDir + File.separator + "web";

        int port = 8080;
        String portEnv = System.getProperty("app.port");
        if (portEnv != null) {
            try {
                port = Integer.parseInt(portEnv);
            } catch (NumberFormatException ignored) {}
        }

        // 初始化DAO层
        BookDao bookDao = new BookDao(dataDir);
        ReaderDao readerDao = new ReaderDao(dataDir);
        BorrowRecordDao borrowRecordDao = new BorrowRecordDao(dataDir);

        // 初始化Service层
        BookService bookService = new BookService(bookDao);
        ReaderService readerService = new ReaderService(readerDao);
        BorrowService borrowService = new BorrowService(borrowRecordDao, bookService, readerService);

        // 初始化Controller层
        BookController bookController = new BookController(bookService);
        ReaderController readerController = new ReaderController(readerService);
        BorrowController borrowController = new BorrowController(borrowService);
        LoginController loginController = new LoginController(readerService);

        // 启动HTTP服务器
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new Router(bookController, readerController, borrowController, loginController, webRoot));
        server.setExecutor(null); // 使用默认单线程执行器
        server.start();

        System.out.println("============================================");
        System.out.println("  图书管理系统 (Library Management System)");
        System.out.println("  服务器已启动: http://localhost:" + port);
        System.out.println("  数据目录: " + new File(dataDir).getAbsolutePath());
        System.out.println("  Web目录: " + new File(webRoot).getAbsolutePath());
        System.out.println("  按 Ctrl+C 停止服务器");
        System.out.println("============================================");

        // 生成一些示例数据（首次运行）
        initSampleData(bookService, readerService);
    }

    /** 首次运行时初始化示例数据 */
    private static void initSampleData(BookService bookService, ReaderService readerService) {
        boolean hasBooks = !bookService.getAllBooks().isEmpty();
        boolean hasReaders = !readerService.getAllReaders().isEmpty();

        if (!hasBooks) {
            System.out.println("\n[初始化] 添加示例图书数据...");
            bookService.addBook(new com.library.model.Book(
                    "978-7-302-47974-0", "Java核心技术 卷I", "Cay S. Horstmann",
                    "计算机科学/编程语言/Java", 5, "机械工业出版社", "2020-01-01",
                    "Java程序设计的经典教材，全面讲解Java SE的核心特性"));
            bookService.addBook(new com.library.model.Book(
                    "978-7-111-56419-2", "数据结构与算法分析", "Mark Allen Weiss",
                    "计算机科学/数据结构", 3, "机械工业出版社", "2018-06-01",
                    "使用Java语言描述数据结构和算法，包含大量实例"));
            bookService.addBook(new com.library.model.Book(
                    "978-7-115-44582-4", "计算机网络：自顶向下方法", "James F. Kurose",
                    "计算机科学/网络", 4, "人民邮电出版社", "2019-03-01",
                    "计算机网络经典教材，以自顶向下的方式讲解网络协议"));
            bookService.addBook(new com.library.model.Book(
                    "978-7-121-32800-2", "MySQL必知必会", "Ben Forta",
                    "计算机科学/数据库", 6, "电子工业出版社", "2017-08-01",
                    "MySQL入门经典，短小精悍的SQL学习手册"));
            bookService.addBook(new com.library.model.Book(
                    "978-7-302-55133-3", "软件工程导论", "张海藩",
                    "计算机科学/软件工程", 5, "清华大学出版社", "2020-05-01",
                    "国内软件工程课程权威教材，覆盖软件工程各个阶段"));
            bookService.addBook(new com.library.model.Book(
                    "978-7-111-69577-4", "算法导论", "Thomas H. Cormen",
                    "计算机科学/算法", 3, "机械工业出版社", "2022-01-01",
                    "算法领域的圣经级教材，ACM/ICPC必读"));
            bookService.addBook(new com.library.model.Book(
                    "978-7-302-60430-5", "Python编程：从入门到实践", "Eric Matthes",
                    "计算机科学/编程语言/Python", 4, "人民邮电出版社", "2022-06-01",
                    "Python入门畅销书，项目驱动学习"));
            System.out.println("  已添加 " + bookService.getAllBooks().size() + " 本示例图书");
        }

        if (!hasReaders) {
            System.out.println("[初始化] 添加示例读者数据...");
            readerService.register(new com.library.model.Reader(
                    "2024001", "张三", "计算机学院",
                    com.library.model.Reader.ReaderType.STUDENT, "13800000001", "123456"));
            readerService.register(new com.library.model.Reader(
                    "2024002", "李四", "软件学院",
                    com.library.model.Reader.ReaderType.STUDENT, "13800000002", "123456"));
            readerService.register(new com.library.model.Reader(
                    "T001", "王教授", "计算机学院",
                    com.library.model.Reader.ReaderType.TEACHER, "13800000003", "123456"));
            System.out.println("  已添加 " + readerService.getAllReaders().size() + " 位示例读者");
        }
    }
}
