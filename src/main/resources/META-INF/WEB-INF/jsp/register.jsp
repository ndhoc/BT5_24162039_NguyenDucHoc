<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><title>Đăng ký</title><link rel="stylesheet" href="<c:url value='/css/style.css'/>"></head><body><main class="card auth">
<%@ include file="common.jspf" %><h1>Đăng ký tài khoản</h1><c:if test="${not empty error}"><p class="alert" role="alert"><c:out value="${error}"/></p></c:if>
<form method="post" action="<c:url value='/register'/>"><input type="hidden" name="csrf" value="${sessionScope.csrf}">
<label for="fullName">Họ tên</label><input id="fullName" name="fullName" value="<c:out value='${fullName}'/>" maxlength="100" required>
<label for="username">Tên đăng nhập</label><input id="username" name="username" value="<c:out value='${username}'/>" pattern="[A-Za-z0-9_]{3,50}" maxlength="50" required>
<label for="email">Email</label><input id="email" type="email" name="email" value="<c:out value='${email}'/>" maxlength="255" required>
<label for="phone">Số điện thoại</label><input id="phone" name="phone" type="tel" value="<c:out value='${phone}'/>" pattern="[+]?[0-9]{9,15}" maxlength="15" required>
<label for="password">Mật khẩu</label><input id="password" type="password" name="password" minlength="6" maxlength="128" required>
<label for="confirm">Xác nhận mật khẩu</label><input id="confirm" type="password" name="confirm" minlength="6" maxlength="128" required>
<button type="submit">Đăng ký</button></form></main></body></html>
