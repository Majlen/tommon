<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
<title>TOMMON</title>
<link rel="stylesheet" type="text/css" href="tommon.css">
<link rel="stylesheet" type="text/css" href="nouislider.css">
<link rel="stylesheet" type="text/css" href="nouislider.pips.css">
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
<div id="dateSelectorWrapper">
<div id="dateSelector"></div>
</div>
</div>
</div>
<script type="text/javascript">
var end = Math.floor(Date.now());
var slider = noUiSlider.create(document.getElementById("dateSelector"), {
	start: [msecToDate(end - 7 * 24 * 3600 * 1000), msecToDate(end)],
	connect: true,
	range: {'min': secToDate(${oldest}), 'max': msecToDate(end)},
	step: 60000,
	pips: {
		mode: 'positions',
		stepped: true,
		density: 2,
		values: [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100],
		format: {
			to: function(value) {
				return getDateTime(value);
			},
			from: function(value) {
				return getDateTime(value);
			}
		}
	}
});
slider.on('change', function(){updateGraphs();});
</script>
</body>
</html>
