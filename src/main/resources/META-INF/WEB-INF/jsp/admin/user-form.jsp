<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><title>Người dùng</title><link rel="stylesheet" href="<c:url value='/css/style.css'/>"></head><body><main class="card auth">
<%@ include file="../common.jspf" %><h1>${editingUser.id > 0 || formId > 0 ? 'Sửa' : 'Thêm'} người dùng</h1><c:if test="${not empty error}"><p class="alert"><c:out value="${error}"/></p></c:if>
<form method="post" action="<c:url value='/admin/user/save'/>"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="id" value="${not empty formId ? formId : editingUser.id}">
<label for="username">Tên đăng nhập</label><input id="username" name="username" value="<c:out value='${not empty username ? username : editingUser.username}'/>" pattern="[A-Za-z0-9_]{3,50}" required>
<label for="fullName">Họ tên</label><input id="fullName" name="fullName" value="<c:out value='${not empty fullName ? fullName : editingUser.fullName}'/>" maxlength="100" required>
<label for="email">Email</label><input id="email" type="email" name="email" value="<c:out value='${not empty email ? email : editingUser.email}'/>" maxlength="255" required>
<label for="phone">Số điện thoại</label><input id="phone" name="phone" value="<c:out value='${not empty phone ? phone : editingUser.phone}'/>" pattern="[+]?[0-9]{9,15}" required>
<label for="roleId">Vai trò</label><select id="roleId" name="roleId"><c:set var="selectedRole" value="${not empty roleId ? roleId : editingUser.roleId}"/><option value="5" ${selectedRole == 5 ? 'selected' : ''}>Thành viên</option><option value="2" ${selectedRole == 2 ? 'selected' : ''}>Manager</option><option value="1" ${selectedRole == 1 ? 'selected' : ''}>Admin</option></select>
<label class="check"><input type="checkbox" name="active" value="true" ${(not empty error ? active : editingUser.active) ? 'checked' : ''}> Tài khoản hoạt động</label>
<label for="password">Mật khẩu ${editingUser.id > 0 || formId > 0 ? '(để trống nếu giữ nguyên)' : ''}</label><input id="password" type="password" name="password" minlength="6" maxlength="128" ${editingUser.id > 0 || formId > 0 ? '' : 'required'}>
<label for="confirm">Xác nhận mật khẩu</label><input id="confirm" type="password" name="confirm" minlength="6" maxlength="128" ${editingUser.id > 0 || formId > 0 ? '' : 'required'}>
<button type="submit">Lưu</button></form><p><a href="<c:url value='/admin/user'/>">Quay lại</a></p>
</main></body></html>
