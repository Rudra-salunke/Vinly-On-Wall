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
  <style>
  .active { font-weight: bold; }
  </style>
  <body>
  <h1><c:out value="${album.title}"/></h1>
  <h2><c:out value="${album.artist}"/></h2>

  <c:if test="${not empty album.coverBase64}">
    <img src="data:${album.coverType};base64,${album.coverBase64}"
         alt="<c:out value='${album.title}'/>" width="300">
  </c:if>

  <p>Now playing: <span id="nowPlaying"></span></p>

  <audio id="player"></audio>

  <div>
    <button id="prevBtn">Previous</button>
    <button id="playBtn">Play</button>
    <button id="nextBtn">Next</button>
  </div>

  <ul id="tracklist">
    <c:forEach var="t" items="${tracks}">
      <li data-id="${t.id}" data-title="<c:out value='${t.title}'/>">
        <c:out value="${t.trackNumber}"/>. <c:out value="${t.title}"/>
      </li>
    </c:forEach>
  </ul>

  <script src="js/script.js"></script>
  </body>
</html>
