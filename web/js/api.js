/**
 * API 调用封装
 * 统一处理与后端Java服务器的HTTP通信
 */
const API_BASE = 'http://localhost:8080/api';

const API = {
    /**
     * GET 请求
     * @param {string} path - API路径，如 "/books?keyword=Java"
     * @returns {Promise<Object>} 解析后的JSON响应
     */
    async get(path) {
        const response = await fetch(API_BASE + path);
        if (!response.ok) throw new Error('HTTP ' + response.status);
        return await response.json();
    },

    /**
     * POST 请求
     * @param {string} path - API路径
     * @param {Object} data - 请求体数据
     * @returns {Promise<Object>} 解析后的JSON响应
     */
    async post(path, data) {
        const response = await fetch(API_BASE + path, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json; charset=UTF-8' },
            body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error('HTTP ' + response.status);
        return await response.json();
    },

    /**
     * PUT 请求
     */
    async put(path, data) {
        const response = await fetch(API_BASE + path, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json; charset=UTF-8' },
            body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error('HTTP ' + response.status);
        return await response.json();
    },

    /**
     * DELETE 请求
     */
    async del(path) {
        const response = await fetch(API_BASE + path, { method: 'DELETE' });
        if (!response.ok) throw new Error('HTTP ' + response.status);
        return await response.json();
    }
};

/**
 * HTML转义（防止XSS）
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * 显示Toast提示
 * @param {string} message - 消息内容
 * @param {string} type - 'success' 或 'error'
 */
function showToast(message, type) {
    const toast = document.createElement('div');
    toast.className = 'toast toast-' + (type || 'success');
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s';
        setTimeout(() => toast.remove(), 300);
    }, 2500);
}

/**
 * 格式化日期（yyyy-MM-dd -> 更友好的显示）
 */
function formatDate(dateStr) {
    if (!dateStr) return '-';
    return dateStr;
}

/**
 * 获取借阅状态的中文显示
 */
function getStatusLabel(status) {
    const map = {
        'BORROWED': '<span class="badge badge-borrowed">借出中</span>',
        'RETURNED': '<span class="badge badge-returned">已归还</span>',
        'OVERDUE': '<span class="badge badge-overdue">已超期</span>'
    };
    return map[status] || status;
}
