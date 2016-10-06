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

function updateRange(date) {
	if (date < window.start) {
		window.start = date;
		window.slider.updateOptions({range: {'min': window.start, 'max': timestamp}});
		if (graphs.length == 0) {
			window.slider.set([(timestamp - 7*24*3600), null]);
		}
	}
}

function select_chart(box) {
	if (timestamp == 0) {
		timestamp = Math.floor(Date.now() / 1000);
	}
	var str = box.getAttribute("data-chart");

	if (box.checked) {
		var ajaxMinDate = new XMLHttpRequest();
		if (graphs.length == 0) {
        	ajaxMinDate.open("GET", "api/" + str + "?first", false);
        	ajaxMinDate.send();
        	var date = parseInt(ajaxMinDate.responseText);
        	updateRange(date);
        } else {
        	ajaxMinDate.open("GET", "api/" + str + "?first", true);
        	ajaxMinDate.onreadystatechange = function() {
            	if (ajax.readyState == 4 && ajax.status == 200) {
            		var date = parseInt(ajaxMinDate.responseText);
					updateRange(date);
            	}
            };
            ajaxMinDate.send();
        }

		var ajax = new XMLHttpRequest();
		ajax.open("GET", str, true);
		ajax.onreadystatechange = function() {
			if (ajax.readyState == 4 && ajax.status == 200) {
				var parent = document.getElementById("section");
				parent.insertAdjacentHTML("beforeend", ajax.responseText);
				registerGraph(str);
			}
		};
		ajax.send();
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
	var div = document.getElementById(name + "_chart");
	var range = window.slider.get();

	g = new Dygraph(div, "api/" + name + "?from=" + parseInt(range[0]) + "&to=" + parseInt(range[1]),
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

function updateGraphs() {
	var range = slider.get();
	for (var i = 0; i < graphs.length; i++) {
		var file = graphs[i].maindiv_.id.slice(0,graphs[i].maindiv_.id.lastIndexOf("_"));
		graphs[i].updateOptions({'file': "api/" + file + "?from=" + parseInt(range[0]) + "&to=" + parseInt(range[1])});
	}
}
