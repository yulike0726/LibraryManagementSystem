/**
 * 读者管理前端逻辑
 * 处理读者信息的增删改查
 */

/** 搜索读者 */
async function searchReaders() {
    const keyword = document.getElementById('readerKeyword').value;
    try {
        const result = await API.get('/readers?keyword=' + encodeURIComponent(keyword));
        renderReaderTable(result.data || []);
    } catch (e) {
        console.error('搜索读者失败:', e);
        document.getElementById('readerTableBody').innerHTML =
            '<tr><td colspan="6" class="text-center">加载失败，请检查服务器</td></tr>';
    }
}

/** 渲染读者列表 */
function renderReaderTable(readers) {
    const tbody = document.getElementById('readerTableBody');
    if (!readers || readers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">暂无读者数据</td></tr>';
        return;
    }
    tbody.innerHTML = readers.map(r => `
        <tr>
            <td>${escapeHtml(r.readerId)}</td>
            <td>${escapeHtml(r.name)}</td>
            <td>${escapeHtml(r.department)}</td>
            <td>${r.readerType === 'TEACHER' ? '👨‍🏫 教师' : '🎓 学生'}</td>
            <td>${r.currentBorrows} / ${r.readerType === 'TEACHER' ? '10' : '5'} 本</td>
            <td>
                <button class="btn btn-outline btn-sm" onclick="editReader('${escapeHtml(r.readerId)}')">✏️ 编辑</button>
                <button class="btn btn-danger btn-sm" onclick="deleteReader('${escapeHtml(r.readerId)}')">🗑️ 删除</button>
            </td>
        </tr>
    `).join('');
}

/** 注册读者 */
async function registerReader() {
    const reader = {
        readerId: document.getElementById('regReaderId').value.trim(),
        name: document.getElementById('regName').value.trim(),
        department: document.getElementById('regDepartment').value.trim(),
        readerType: document.getElementById('regType').value,
        phone: document.getElementById('regPhone').value.trim()
    };

    if (!reader.readerId || !reader.name) {
        showToast('读者ID和姓名为必填项', 'error');
        return;
    }

    try {
        const result = await API.post('/readers', reader);
        if (result.success) {
            showToast('读者注册成功！', 'success');
            closeModal('regReaderModal');
            document.getElementById('regReaderForm').reset();
            searchReaders();
        } else {
            showToast(result.message || '注册失败', 'error');
        }
    } catch (e) {
        showToast('网络错误：' + e.message, 'error');
    }
}

/** 编辑读者信息 */
async function editReader(readerId) {
    try {
        const result = await API.get('/readers/' + readerId);
        if (result.success && result.data) {
            const r = result.data;
            document.getElementById('editReaderId').value = r.readerId;
            document.getElementById('editReaderName').value = r.name;
            document.getElementById('editReaderDept').value = r.department;
            document.getElementById('editReaderType').value = r.readerType;
            document.getElementById('editReaderPhone').value = r.phone || '';
            openModal('editReaderModal');
        }
    } catch (e) {
        showToast('获取读者信息失败', 'error');
    }
}

/** 提交修改 */
async function updateReader() {
    const readerId = document.getElementById('editReaderId').value.trim();
    const reader = {
        name: document.getElementById('editReaderName').value.trim(),
        department: document.getElementById('editReaderDept').value.trim(),
        readerType: document.getElementById('editReaderType').value,
        phone: document.getElementById('editReaderPhone').value.trim()
    };

    try {
        const result = await API.put('/readers/' + readerId, reader);
        if (result.success) {
            showToast('读者信息更新成功！', 'success');
            closeModal('editReaderModal');
            searchReaders();
        } else {
            showToast(result.message || '更新失败', 'error');
        }
    } catch (e) {
        showToast('网络错误：' + e.message, 'error');
    }
}

/** 删除读者 */
async function deleteReader(readerId) {
    if (!confirm('确定要删除该读者吗？')) return;
    try {
        const result = await API.del('/readers/' + readerId);
        if (result.success) {
            showToast('读者删除成功！', 'success');
            searchReaders();
        } else {
            showToast(result.message || '删除失败', 'error');
        }
    } catch (e) {
        showToast('网络错误：' + e.message, 'error');
    }
}
