<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><title>Đăng nhập</title><link rel="stylesheet" href="<c:url value='/css/style.css'/>"></head><body><main class="card auth">
<%@ include file="common.jspf" %><h1>Đăng nhập</h1>
<c:if test="${param.registered == '1'}"><p class="success">Đăng ký thành công. Hãy đăng nhập.</p></c:if><c:if test="${param.loggedout == '1'}"><p class="success">Đã đăng xuất.</p></c:if>
<c:if test="${not empty error}"><p class="alert" role="alert"><c:out value="${error}"/></p></c:if>
<form method="post" action="<c:url value='/login'/>"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><label for="username">Tên đăng nhập</label><input id="username" name="username" value="<c:out value='${username}'/>" required maxlength="50" autocomplete="username"><label for="password">Mật khẩu</label><input id="password" type="password" name="password" required autocomplete="current-password"><button type="submit">Đăng nhập</button></form>
<p><a href="<c:url value='/register'/>">Chưa có tài khoản? Đăng ký</a></p></main></body></html>
