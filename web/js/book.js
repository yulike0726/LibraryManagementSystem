/**
 * 图书管理前端逻辑
 * 处理图书检索、列表展示、增删改操作
 */

/** 搜索图书 */
async function searchBooks() {
    const keyword = document.getElementById('searchKeyword').value;
    const category = document.getElementById('searchCategory').value;
    const sortField = document.getElementById('sortField').value;
    const algo = document.getElementById('sortAlgorithm').value;

    let query = `?keyword=${encodeURIComponent(keyword)}&category=${encodeURIComponent(category)}`;
    if (sortField) query += `&sort=${sortField}&algo=${algo}`;

    try {
        const result = await API.get('/books' + query);
        renderBookTable(result.data || []);
    } catch (e) {
        console.error('搜索图书失败:', e);
        document.getElementById('bookTableBody').innerHTML =
            '<tr><td colspan="6" class="text-center">加载失败，请检查服务器是否启动</td></tr>';
    }
}

/** 渲染图书列表表格 */
function renderBookTable(books) {
    const tbody = document.getElementById('bookTableBody');
    if (!books || books.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">暂无图书数据</td></tr>';
        return;
    }
    tbody.innerHTML = books.map(b => `
        <tr>
            <td>${escapeHtml(b.isbn)}</td>
            <td>${escapeHtml(b.title)}</td>
            <td>${escapeHtml(b.author)}</td>
            <td>${escapeHtml(b.category)}</td>
            <td>
                <span style="color:${b.availableCount > 0 ? '#27ae60' : '#e74c3c'}">
                    ${b.availableCount}</span> / ${b.totalCount}
            </td>
            <td>${escapeHtml(b.publisher)}</td>
        </tr>
    `).join('');
}

/** 新增图书 */
async function addBook() {
    const book = {
        isbn: document.getElementById('addIsbn').value.trim(),
        title: document.getElementById('addTitle').value.trim(),
        author: document.getElementById('addAuthor').value.trim(),
        category: document.getElementById('addCategory').value.trim(),
        totalCount: parseInt(document.getElementById('addTotalCount').value) || 1,
        publisher: document.getElementById('addPublisher').value.trim(),
        publishDate: document.getElementById('addPublishDate').value.trim(),
        description: document.getElementById('addDescription').value.trim()
    };

    if (!book.isbn || !book.title || !book.author) {
        showToast('ISBN、书名和作者为必填项', 'error');
        return;
    }

    try {
        const result = await API.post('/books', book);
        if (result.success) {
            showToast('图书添加成功！', 'success');
            closeModal('addBookModal');
            document.getElementById('addBookForm').reset();
            searchBooks();
        } else {
            showToast(result.message || '添加失败', 'error');
        }
    } catch (e) {
        showToast('网络错误：' + e.message, 'error');
    }
}

/** 修改图书 */
async function editBook(isbn) {
    try {
        const result = await API.get('/books/' + encodeURIComponent(isbn));
        if (result.success && result.data) {
            const b = result.data;
            document.getElementById('editIsbn').value = b.isbn;
            document.getElementById('editTitle').value = b.title;
            document.getElementById('editAuthor').value = b.author;
            document.getElementById('editCategory').value = b.category;
            document.getElementById('editTotalCount').value = b.totalCount;
            document.getElementById('editPublisher').value = b.publisher;
            document.getElementById('editPublishDate').value = b.publishDate;
            document.getElementById('editDescription').value = b.description || '';
            openModal('editBookModal');
        }
    } catch (e) {
        showToast('获取图书信息失败', 'error');
    }
}

/** 提交修改 */
async function updateBook() {
    const isbn = document.getElementById('editIsbn').value.trim();
    const book = {
        title: document.getElementById('editTitle').value.trim(),
        author: document.getElementById('editAuthor').value.trim(),
        category: document.getElementById('editCategory').value.trim(),
        totalCount: parseInt(document.getElementById('editTotalCount').value) || 1,
        publisher: document.getElementById('editPublisher').value.trim(),
        publishDate: document.getElementById('editPublishDate').value.trim(),
        description: document.getElementById('editDescription').value.trim()
    };

    try {
        const result = await API.put('/books/' + isbn, book);
        if (result.success) {
            showToast('图书修改成功！', 'success');
            closeModal('editBookModal');
            searchBooks();
        } else {
            showToast(result.message || '修改失败', 'error');
        }
    } catch (e) {
        showToast('网络错误：' + e.message, 'error');
    }
}

/** 删除图书 */
async function deleteBook(isbn) {
    if (!confirm('确定要删除该图书吗？此操作不可撤销。')) return;
    try {
        const result = await API.del('/books/' + isbn);
        if (result.success) {
            showToast('图书删除成功！', 'success');
            searchBooks();
        } else {
            showToast(result.message || '删除失败', 'error');
        }
    } catch (e) {
        showToast('网络错误：' + e.message, 'error');
    }
}
