<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html><head><title>Danh sách video | Shop 24162039</title><meta name="viewport" content="width=device-width, initial-scale=1"><link rel="stylesheet" href="${pageContext.request.contextPath}/templates/bootstrap/bootstrap.min.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/storefront.css"></head><body class="app-layout d-flex flex-column min-vh-100"><c:set var="ctx" value="${pageContext.request.contextPath}"/><%@ include file="/common/web/header.jsp" %><div class="container-xl app-content flex-grow-1 "><div class="app-page" id="page-content">
<main class="page">
  <div class="section-head">
    <div><p class="eyebrow">Thư viện video</p><h1>Tất cả video</h1></div>
    <c:if test="${not empty sessionScope.account}"><div class="hero-actions"><a class="button" href="${ctx}/video/upload">Tải video lên</a></div></c:if>
  </div>

  <c:if test="${param.saved == '1'}"><p class="success" role="status">Tải video lên thành công.</p></c:if>
  <p class="muted result-count">${total} video · trang ${page}/${pageCount} · 6 video mỗi trang</p>

  <div class="video-grid">
    <c:forEach items="${videoList}" var="v">
      <article class="video-card">
        <a class="video-media" href="${ctx}/video/detail?id=${v.id}">
          <c:url value="/video-file" var="fileUrl"><c:param name="fname" value="${v.fileName}"/></c:url>
          <video preload="metadata" muted playsinline><source src="${fileUrl}"></video>
          <span class="play-badge" aria-hidden="true">▶</span>
        </a>
        <div class="video-body">
          <h3><a href="${ctx}/video/detail?id=${v.id}"><c:out value="${v.title}"/></a></h3>
          <p class="desc"><c:out value="${v.description}"/></p>
          <p class="muted"><c:out value="${v.user.username}"/> · <fmt:formatDate value="${v.createdAt}" pattern="dd/MM/yyyy HH:mm"/></p>
        </div>
      </article>
    </c:forEach>
    <c:if test="${empty videoList}"><p class="muted empty-state">Chưa có video nào.</p></c:if>
  </div>

  <c:if test="${pageCount > 1}">
    <nav class="pagination" aria-label="Phân trang video">
      <c:choose>
        <c:when test="${page > 1}"><a href="${ctx}/video?page=${page - 1}">← Trang trước</a></c:when>
        <c:otherwise><span class="disabled">← Trang trước</span></c:otherwise>
      </c:choose>
      <c:forEach begin="1" end="${pageCount}" var="i">
        <a class="${i == page ? 'active' : ''}" href="${ctx}/video?page=${i}">${i}</a>
      </c:forEach>
      <c:choose>
        <c:when test="${page < pageCount}"><a href="${ctx}/video?page=${page + 1}">Trang sau →</a></c:when>
        <c:otherwise><span class="disabled">Trang sau →</span></c:otherwise>
      </c:choose>
    </nav>
  </c:if>
</main>
</div></div><%@ include file="/common/web/footer.jsp" %><script src="${ctx}/js/form-validation.js" defer></script><script src="${ctx}/js/avatar.js" defer></script><script src="${ctx}/js/navigation.js" defer></script></body></html>
