<%--
  Created by IntelliJ IDEA.
  User: RUDRA
  Date: 11-11-2026
  Time: 11:44
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><c:out value="${album.title}"/></title>
  <link rel="stylesheet" href="css/style.css">
</head>
<body>

<%-- The cover is printed ONCE here as a CSS variable (--cover).
     The record label and the album card both use var(--cover) in style.css.
     If the album has no cover, the variable is simply not set. --%>
<div class="page"
     <c:if test="${not empty album.coverBase64}">
       style="--cover: url('data:${album.coverType};base64,${album.coverBase64}')"
     </c:if>
>

  <!-- ******************* LEFT COLUMN ******************* -->
  <div class="left">

    <div class="record-wrap">
      <div class="record">
        <div class="record-label"></div>
      </div>
      <div class="tonearm">
        <div class="tonearm-arm">
          <div class="tonearm-head"></div>
        </div>
      </div>
    </div>

    <div class="now-playing">
      Now playing: <span id="nowPlaying"></span>
    </div>
    <div class="seek">
    <span id="timeNow">0:00</span>
    <input type="range" id="seekBar" min="0" max="1000" value="0" step="1" aria-label="Seek">
    <span id="timeTotal">0:00</span>
    </div>
    <div class="controls">
      <button id="prevBtn">Previous</button>
      <button id="playBtn">Play</button>
      <button id="nextBtn">Next</button>
    </div>

    <div class="lyrics-box">
      <%-- TODO later (feat): lyrics --%>
    </div>

  </div>

  <!-- ******************* RIGHT COLUMN ******************* -->
  <div class="right">

    <div class="album-card">
      <div class="album-art"></div>
      <div class="album-info">
        <h1><c:out value="${album.title}"/></h1>
        <h2><c:out value="${album.artist}"/></h2>
      </div>
    </div>

    <div class="tracklist-box">
      <%-- TODO later: add a class by track count (cols-1 / cols-2 / cols-3) --%>
      <c:set var="trackCount" value="${fn:length(tracks)}" />
      <c:choose>
          <c:when test="${trackCount <= 7}">
              <c:set var="gridClass" value="cols-1" />
          </c:when>
          <c:when test="${trackCount <= 16}">
              <c:set var="gridClass" value="cols-2" />
          </c:when>
          <c:otherwise>
              <c:set var="gridClass" value="cols-3" />
          </c:otherwise>
      </c:choose>
      <ul id=tracklist class="track-list ${gridClass}">
        <c:forEach var="t" items="${tracks}">
          <li data-id="${t.id}" data-title="<c:out value='${t.title}'/>">
            <c:out value="${t.trackNumber}"/>. <c:out value="${t.title}"/>
          </li>
        </c:forEach>
      </ul>
    </div>
  </div>
</div>

<audio id="player"></audio>
<script src="js/script.js"></script>
</body>
</html>

