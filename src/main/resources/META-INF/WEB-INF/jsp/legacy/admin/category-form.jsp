<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html><head><title>Danh mục | 24162039</title><meta name="viewport" content="width=device-width, initial-scale=1"><link rel="stylesheet" href="${pageContext.request.contextPath}/templates/bootstrap/bootstrap.min.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/storefront.css"></head><body class="app-layout d-flex flex-column min-vh-100"><c:set var="ctx" value="${pageContext.request.contextPath}"/><%@ include file="/common/web/header.jsp" %><div class="container-xl app-content flex-grow-1 app-admin-grid"><%@ include file="/common/admin/left.jsp" %><div class="app-page" id="page-content"><main class="card auth">
<p class="eyebrow">Quản lý danh mục</p><h1>${editing ? 'Sửa danh mục' : 'Thêm danh mục'}</h1>
<c:if test="${not empty alert}"><p class="alert" role="alert"><c:out value="${alert}"/></p></c:if>
<form action="${pageContext.request.contextPath}/admin/category/${editing ? 'edit' : 'add'}" method="post" enctype="multipart/form-data"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="id" value="${category.id}">
<label for="catename">Tên danh mục</label><input id="catename" name="catename" aria-invalid="${not empty fieldErrors.catename}" aria-describedby="catename-error" class="form-control${empty fieldErrors.catename ? '' : ' is-invalid'}" data-nonblank maxlength="255" value="<c:out value='${category.name}'/>" required>
<form:field-error field="catename" />
<c:if test="${not empty category.icon}"><c:url value="/image" var="imageUrl"><c:param name="fname" value="${category.icon}"/></c:url><img class="preview" src="${imageUrl}" alt="Ảnh hiện tại"></c:if>
<label for="icon">Ảnh đại diện</label><input id="icon" type="file" name="icon" aria-invalid="${not empty fieldErrors.icon}" aria-describedby="icon-error" class="form-control${empty fieldErrors.icon ? '' : ' is-invalid'}" data-max-mb="5" accept="image/png,image/jpeg"><p class="muted">PNG/JPEG, tối đa 5 MB và 4096 × 4096 pixel. Để trống để giữ ảnh hiện tại.</p>
<form:field-error field="icon" />
<button class="btn btn-primary" type="submit">${editing ? 'Lưu thay đổi' : 'Thêm danh mục'}</button><a href="${pageContext.request.contextPath}/admin/category/list">Hủy</a>
</form></main></div></div><%@ include file="/common/web/footer.jsp" %><script src="${ctx}/js/form-validation.js" defer></script><script src="${ctx}/js/avatar.js" defer></script><script src="${ctx}/js/navigation.js" defer></script></body></html>
