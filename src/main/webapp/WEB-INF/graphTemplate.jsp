<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="${reqName}_bar" class="bar">
<h2>${reqName}</h2>
<div id="${reqName}_legend" class="legend" align="left">
</div>
<div id="${reqName}_boxes" class="boxes" align="right">

<c:forEach items="${reqStates}" var="state" varStatus="ctr">
<input type=checkbox class="${ctr.index}" id="${reqName}" onClick="select_lines(this)" checked>
<label for="${ctr.index}">${state}</label>
</c:forEach>

</div>
</div>

<div id="${reqName}_chart" class="graph" onresize="resize(g_${reqName}_chart)">
