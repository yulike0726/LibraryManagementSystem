/**
 * 借阅管理前端逻辑
 * 处理借书、还书、续借、查询借阅记录
 */

/** 借书操作 */
async function borrowBook() {
    const isbn = document.getElementById('borrowIsbn').value.trim();
    const readerId = document.getElementById('borrowReaderId').value.trim();

    if (!isbn || !readerId) {
        showToast('请输入ISBN和读者ID', 'error');
        return;
    }

    try {
        const result = await API.post('/borrow', { isbn, readerId });
        if (result.success) {
            showToast(result.message, 'success');
            document.getElementById('borrowForm').reset();
            queryRecords();
        } else {
            showToast(result.message, 'error');
        }
    } catch (e) {
        showToast('网络错误：' + e.message, 'error');
    }
}

/** 还书操作 */
async function returnBook(recordId) {
    if (!confirm('确认归还该书？')) return;
    try {
        const result = await API.post('/return/' + recordId);
        if (result.success) {
            showToast(result.message, 'success');
            queryRecords();
        } else {
            showToast(result.message, 'error');
        }
    } catch (e) {
        showToast('网络错误：' + e.message, 'error');
    }
}

/** 续借操作 */
async function renewBook(recordId) {
    if (!confirm('确认续借该书？（每本书最多续借1次）')) return;
    try {
        const result = await API.post('/renew/' + recordId);
        if (result.success) {
            showToast(result.message, 'success');
            queryRecords();
        } else {
            showToast(result.message, 'error');
        }
    } catch (e) {
        showToast('网络错误：' + e.message, 'error');
    }
}

/** 查询借阅记录 */
async function queryRecords() {
    const readerId = document.getElementById('filterReaderId').value;
    const status = document.getElementById('filterStatus').value;

    let query = '';
    if (readerId) query += '&readerId=' + encodeURIComponent(readerId);
    if (status) query += '&status=' + encodeURIComponent(status);
    query = '?' + query.substring(1);

    try {
        const result = await API.get('/borrow-records' + query);
        renderRecordTable(result.data || []);
    } catch (e) {
        console.error('查询借阅记录失败:', e);
    }
}

/** 渲染借阅记录表格 */
function renderRecordTable(records) {
    const tbody = document.getElementById('recordTableBody');
    if (!records || records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">暂无借阅记录</td></tr>';
        return;
    }
    tbody.innerHTML = records.map(r => `
        <tr>
            <td>${escapeHtml(r.recordId)}</td>
            <td>${escapeHtml(r.isbn)}</td>
            <td>${escapeHtml(r.readerId)}</td>
            <td>${formatDate(r.borrowDate)}</td>
            <td>${formatDate(r.dueDate)}</td>
            <td>${getStatusLabel(r.status)}</td>
            <td>
                ${r.status === 'BORROWED' || r.status === 'OVERDUE' ? `
                    <button class="btn btn-success btn-sm" onclick="returnBook('${r.recordId}')">📥 还书</button>
                    <button class="btn btn-warning btn-sm" onclick="renewBook('${r.recordId}')">🔄 续借</button>
                ` : formatDate(r.returnDate)}
            </td>
        </tr>
    `).join('');
}

/** 页面加载时自动查询 */
document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('recordTableBody')) {
        queryRecords();
    }
});
