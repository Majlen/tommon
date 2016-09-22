<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
<title>TOMMON</title>
<link rel="stylesheet" type="text/css" href="tommon.css">
<script type="text/javascript" src="dygraph-combined.js"></script>
<script type="text/javascript" src="synchronizer.js"></script>
<script type="text/javascript" src="tommon.js"></script>
</head>
<body>

<div id="nav">
<br>
<c:forEach items="${plugins}" var="plugin">
<!--<a href="${plugin.name}">${name}</a><br />-->
<input type=checkbox class="chartBox" id="${plugin.name}_selector" data-chart="${plugin.name}" onClick="select_chart(this)">
<label for="${plugin.name}_selector">${name}</label><br>
</c:forEach>
</div>
<div id="wrapper">
<div id="section">

</div>
</div>
</body>
</html>
