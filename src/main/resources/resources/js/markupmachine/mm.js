function MarkupMachine() {
	var templates=new Object();
	
	this.template = function(url, values, callback) {
		if (!templates[url]) {
			$.get(url, function(data) {
				templates[url] = data;
				replace(url, values, callback);
			});
		} else {
			replace(url, values, callback);
		}
	}
	
	function replace(url, values, callback) {
		var html = templates[url];
		for (var key in values) {
			var rex = new RegExp("\{" + key + "\}" ,"g");
			html = html.replace(rex, values[key]);
		}
		callback(html);
	}
}