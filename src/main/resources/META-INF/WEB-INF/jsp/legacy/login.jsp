<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html><head><title>Đăng nhập | 24162039</title><meta name="viewport" content="width=device-width, initial-scale=1"><link rel="stylesheet" href="${pageContext.request.contextPath}/templates/bootstrap/bootstrap.min.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/storefront.css"></head><body class="app-layout d-flex flex-column min-vh-100"><c:set var="ctx" value="${pageContext.request.contextPath}"/><%@ include file="/common/web/header.jsp" %><div class="container-xl app-content flex-grow-1 "><div class="app-page" id="page-content">
<main class="card auth"><p class="eyebrow">24162039 · Nguyễn Đức Học</p><h1>Đăng nhập</h1>
<c:if test="${not empty alert}"><p class="alert" role="alert"><c:out value="${alert}"/></p></c:if>
<c:if test="${not empty pendingUsername}">
  <c:url var="activateUrl" value="/activate"><c:param name="username" value="${pendingUsername}"/></c:url>
  <p><a class="button small" href="${activateUrl}">Nhập mã OTP để kích hoạt</a></p>
</c:if>
<c:if test="${param.activated == '1'}"><p class="success">Kích hoạt thành công. Hãy đăng nhập bằng tài khoản của bạn.</p></c:if>
<c:if test="${param.reset == '1'}"><p class="success">Đặt lại mật khẩu thành công. Hãy đăng nhập bằng mật khẩu mới.</p></c:if>
<c:if test="${param.loggedout == '1'}"><p class="success">Đã đăng xuất, hủy Session và xóa các Cookie đăng nhập.</p></c:if>
<form action="${pageContext.request.contextPath}/login" method="post">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<label for="username">Tên đăng nhập</label><input id="username" name="username" aria-invalid="${not empty fieldErrors.username}" aria-describedby="username-error" class="form-control${empty fieldErrors.username ? '' : ' is-invalid'}" data-nonblank value="<c:out value='${param.username}'/>" autocomplete="username" maxlength="50" required>
<form:field-error field="username" />
<label for="password">Mật khẩu</label><input id="password" type="password" name="password" aria-invalid="${not empty fieldErrors.password}" aria-describedby="password-error" class="form-control${empty fieldErrors.password ? '' : ' is-invalid'}" maxlength="128" autocomplete="current-password" required>
<form:field-error field="password" />
<label class="check"><input type="checkbox" name="remember" ${param.remember == 'on' ? 'checked' : ''}> Ghi nhớ đăng nhập</label>
<button class="btn btn-primary" type="submit">Đăng nhập</button></form>
<p class="auth-footer"><a href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a></p>
<p class="auth-footer"><span>Chưa có tài khoản?</span> <a href="${pageContext.request.contextPath}/register">Đăng ký</a></p>
<p class="auth-footer"><a href="${pageContext.request.contextPath}/">Về trang chủ</a></p>
<p class="muted">Tài khoản thực hành: admin / 123456 · manager / 123456 · user / 123456</p>
</main></div></div><%@ include file="/common/web/footer.jsp" %><script src="${ctx}/js/form-validation.js" defer></script><script src="${ctx}/js/avatar.js" defer></script><script src="${ctx}/js/navigation.js" defer></script></body></html>
