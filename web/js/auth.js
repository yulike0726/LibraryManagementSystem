/**
 * 认证模块
 * 检查登录状态，渲染导航栏，区分管理员和读者权限
 */

const API_B = window.location.origin + '/api';

/** 获取当前登录用户信息 */
function getAuth() {
    const raw = sessionStorage.getItem('auth');
    if (!raw) return null;
    try {
        return JSON.parse(raw);
    } catch(e) {
        sessionStorage.removeItem('auth');
        return null;
    }
}

/** 是否已登录 */
function isLoggedIn() { return getAuth() !== null; }

/** 是否是管理员 */
function isAdmin() {
    const auth = getAuth();
    return auth && auth.role === 'admin';
}

/** 是否是读者 */
function isReader() {
    const auth = getAuth();
    return auth && auth.role === 'reader';
}

/** 获取读者ID（仅读者角色） */
function getReaderId() { return getAuth()?.readerId; }

/** 跳转登录页 */
function redirectToLogin() {
    window.location.href = '/login.html';
}

/** 退出登录 */
function logout() {
    sessionStorage.removeItem('auth');
    redirectToLogin();
}

/**
 * 初始化页面：检查登录状态并渲染导航
 * @param {string} activePage - 当前页面标识: 'home','books','readers','borrow'
 */
function initPage(activePage) {
    const auth = getAuth();
    if (!auth) { redirectToLogin(); return; }

    // 渲染导航栏
    const roleLabel = auth.role === 'admin' ? '管理员' : (auth.readerType === 'TEACHER' ? '教师' : '学生');
    const userName = auth.name || auth.readerId;
    const userInfo = `<span style="color:#bdc3c7;font-size:0.85em;">${roleLabel}: ${userName}</span>
                      <a href="javascript:logout()" style="color:#e74c3c;text-decoration:none;font-size:0.85em;margin-left:8px;">退出</a>`;

    const active = 'nav-link active';
    const normal = 'nav-link';

    let navHtml = '';
    if (auth.role === 'admin') {
        // 管理员导航：全部功能
        navHtml = `
            <a href="/index.html" class="${activePage === 'home' ? active : normal}">首页</a>
            <a href="/pages/book-manage.html" class="${activePage === 'books' ? active : normal}">图书管理</a>
            <a href="/pages/reader-manage.html" class="${activePage === 'readers' ? active : normal}">读者管理</a>
            <a href="/pages/borrow-manage.html" class="${activePage === 'borrow' ? active : normal}">借阅管理</a>
            ${userInfo}`;
    } else {
        // 读者导航：仅浏览和借阅
        navHtml = `
            <a href="/index.html" class="${activePage === 'home' ? active : normal}">首页</a>
            <a href="/pages/borrow-manage.html" class="${activePage === 'borrow' ? active : normal}">借阅管理</a>
            ${userInfo}`;
    }

    // 注入到 header nav
    const navEl = document.querySelector('.header .container nav');
    if (navEl) navEl.innerHTML = navHtml;
    const h1 = document.querySelector('.header .container h1');
    if (h1) h1.innerHTML = auth.role === 'admin' ? '图书管理系统' : '图书管理系统';
}
