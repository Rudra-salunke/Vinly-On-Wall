<%--
  Created by IntelliJ IDEA.
  User: RUDRA
  Date: 11-11-2026
  Time: 11:44
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
  <head>
    <title></title>
  </head>
  <body>
  <h1>Vinyl Wall is alive</h1>
  <p>Album name: <c:out value="${album.title}"/></p>
  <p>Album artist: <c:out value="${album.artist}"/></p>
  <c:forEach var="track" items="${tracks}">
  <p><c:out value="${track.trackNumber}"/><c:out value="${track.title}"/></p>

  </c:forEach>
  </body>
</html>
