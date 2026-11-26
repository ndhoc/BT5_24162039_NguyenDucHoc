<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><title>Trang chủ</title><link rel="stylesheet" href="<c:url value='/css/style.css'/>"></head><body><main class="card wide">
<%@ include file="common.jspf" %><h1>BT2 – Nguyễn Đức Học</h1>
<c:choose><c:when test="${not empty account}"><p>Xin chào, <c:out value="${account.fullName}"/>.</p><c:if test="${account.roleId == 1}"><p><a class="button" href="<c:url value='/admin/category'/>">Vào trang quản trị</a></p></c:if></c:when><c:otherwise><p>Đăng ký hoặc đăng nhập để sử dụng hệ thống.</p></c:otherwise></c:choose>
</main></body></html>
