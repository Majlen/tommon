var graphs = [];
var timestamp = 0;
var sync = null;

if (!Date.now) {
	Date.now = function() { return new Date().getTime(); }
}

function resize(div) {
	div.resize();
}

function select_lines(box) {
	var str = box.id + "_chart";

	for (var i=0; i<graphs.length; i++) {
		if (graphs[i].toString().replace("]", "").split(" ")[1] == str) {
			graphs[i].setVisibility(parseInt(box.className), box.checked);
			break;
		}
	}
}

function select_chart(box) {
	var str = box.getAttribute("data-chart");

	if (box.checked) {
		var ajax = new XMLHttpRequest();
		ajax.open("GET", str, true);
		ajax.send();
		ajax.onreadystatechange = function() {
			if (ajax.readyState == 4 && ajax.status == 200) {
				var parent = document.getElementById("section");
				parent.insertAdjacentHTML("beforeend", ajax.responseText);
				registerGraph(str);
			}
		};
	} else {
		if (sync != null) {
			sync.detach();
			sync = null;
		}

		for (var i = 0; i < graphs.length; i++) {
			if (graphs[i].toString().replace("]", "").split(" ")[1] == str + "_chart") {
				graphs[i].destroy();
				graphs.splice(i, 1);
				break;
			}
		}
		var elem = document.getElementById(str + "_bar");
		elem.parentNode.removeChild(elem);
		elem = document.getElementById(str + "_chart");
		elem.parentNode.removeChild(elem);

		synchronizeGraphs();
	}
}

function registerGraph(name) {
	if (timestamp == 0) {
		timestamp = Math.floor(Date.now() / 1000);
	}
	var div = document.getElementById(name + "_chart");

	g = new Dygraph(div, "api/" + name + "?from=" + (timestamp - 7*24*3600) + "&to=" + timestamp,
		{
			includeZero: true,
			drawGapEdgePoints: true,
			stackedGraph: false,
			showRangeSelector: true,
			showRoller: true,
			labelsKMG2: true,
			legend: "always",
			labelsDiv: document.getElementById(name + "_legend"),
			width: div.offsetWidth,
		}
	);

	div.setAttribute("style", "height:320px,width:100%");

	graphs.push(g);
	if (sync != null) {
		sync.detach();
		sync = null;
	}
	synchronizeGraphs();
}

function synchronizeGraphs() {
	if (graphs.length > 1) {
		sync = Dygraph.synchronize(graphs, {zoom: true, selection: true, range: false});
	}
}
