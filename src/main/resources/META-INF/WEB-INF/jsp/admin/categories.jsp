<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><title>Quản lý danh mục</title><link rel="stylesheet" href="<c:url value='/css/style.css'/>"></head><body><main class="card wide">
<%@ include file="../common.jspf" %><h1>Quản lý danh mục</h1>
<c:if test="${param.saved == '1' || param.deleted == '1'}"><p class="success">Thao tác thành công.</p></c:if><c:if test="${param.inUse == '1'}"><p class="alert">Không thể xóa danh mục đang có sản phẩm.</p></c:if><c:if test="${param.missing == '1'}"><p class="alert">Danh mục không tồn tại.</p></c:if>
<div class="toolbar"><form method="get" action="<c:url value='/admin/category'/>"><input name="q" value="<c:out value='${q}'/>" placeholder="Tìm tên danh mục"><button type="submit">Tìm kiếm</button></form><a class="button" href="<c:url value='/admin/category/new'/>">Thêm danh mục</a></div>
<div class="table-wrap"><table><thead><tr><th>Mã</th><th>Tên danh mục</th><th>Ảnh</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach items="${result.items}" var="item"><tr><td>${item.id}</td><td><c:out value="${item.name}"/></td><td><c:out value="${item.icon}"/></td><td><a href="<c:url value='/admin/category/edit'><c:param name='id' value='${item.id}'/></c:url>">Sửa</a><form class="inline" method="post" action="<c:url value='/admin/category/delete'/>"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="id" value="${item.id}"><button type="submit" onclick="return confirm('Xóa danh mục này?')">Xóa</button></form></td></tr></c:forEach>
<c:if test="${empty result.items}"><tr><td colspan="4">Không tìm thấy danh mục.</td></tr></c:if></tbody></table></div>
<p>Trang ${result.number} / ${result.totalPages} · ${result.total} danh mục</p><nav class="pagination"><c:forEach begin="1" end="${result.totalPages}" var="n"><c:url value="/admin/category" var="pageUrl"><c:param name="q" value="${q}"/><c:param name="page" value="${n}"/></c:url><a class="${n == result.number ? 'active' : ''}" href="${pageUrl}">${n}</a></c:forEach></nav>
</main></body></html>
