<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
<title>TOMMON</title>
<link rel="stylesheet" type="text/css" href="tommon.css">
<link rel="stylesheet" type="text/css" href="nouislider.css">
<script type="text/javascript" src="dygraph-combined.js"></script>
<script type="text/javascript" src="synchronizer.js"></script>
<script type="text/javascript" src="tommon.js"></script>
<script type="text/javascript" src="nouislider.js"></script>
</head>
<body>

<div id="nav">
<br>
<c:forEach items="${plugins}" var="plugin">
<input type=checkbox class="chartBox" id="${plugin.name}_selector" data-chart="${plugin.name}" onClick="select_chart(this)">
<label for="${plugin.name}_selector">${plugin.name}</label><br>
</c:forEach>
</div>
<div id="wrapper">
<div id="section">
<div id="dateSelector"></div>
</div>
</div>
<script type="text/javascript">
var start = Math.floor(Date.now() / 1000)-1;
var slider = noUiSlider.create(document.getElementById("dateSelector"), {
	start: [start, start+1],
	connect: true,
	range: {'min': start, 'max': start+1},
	step: 60,
	pips: {
		mode: 'steps',
		stepped: true,
		density: 1
	}
});
slider.on('change', function(){updateGraphs();});
</script>
</body>
</html>
