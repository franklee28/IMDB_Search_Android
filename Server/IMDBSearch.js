var xhr = false;
var title;

window.fbAsyncInit = function() {
	// init the FB JS SDK
	FB.init({
		appId      : '466425263401459', // App ID from the App Dashboard
		channelUrl : 'http://cs-server.usc.edu:12691/examples/servlets/channel.html',
		status     : true, // check the login status upon init?
		cookie     : true, // set sessions cookies to allow your server to access the session?
		xfbml      : true  // parse XFBML tags on this page?
	});
};

// Load the SDK's source Asynchronously
(function(d, debug){
	var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
	if (d.getElementById(id)) {return;}
	js = d.createElement('script'); js.id = id; js.async = true;
	js.src = "//connect.facebook.net/en_US/all" + (debug ? "/debug" : "") + ".js";
	ref.parentNode.insertBefore(js, ref);
}(document, /*debug*/ false));

function makeRequest() {
	xhr = false;

	title = document.movie.title.value;
	if (title == "") {
		alert ("Please Enter the Title!");
		//location.reload();
		document.movie.title.focus();
		return false;
	}
	
	var type = document.movie.title_type.value;
	if (type == "All Types") {
		type = "feature,tv_series,game";
	}
	else if (type == "Feature Film") {
		type = "feature";
	}
	else if (type == "TV Series") {
		type = "tv_series";
	}
	else if (type =="Video Game") {
		type = "game";
	}
	
	var url = "http://cs-server.usc.edu:12691/examples/servlet/IMDBSearch?title=" + title + "&title_type=" + type;
	
	url = encodeURI(url);
	
	document.getElementById('result').innerHTML = "<p align=\"center\" style=\"font\-size:30px; font\-weight:700px;\">Searching...</p>\n";
	if(window.XMLHttpRequest) {
		try {
			xhr = new XMLHttpRequest();
		}
		catch(e) {
			alert (e);
			xhr = false;
		}
	}
	else if(window.ActiveXObject) {
		try {
			xhr = new ActiveXObject("Msxml2.XMLHTTP");
		}
		catch(e) {
			try {
				xhr = new ActiveXObject("Microsoft.XMLHTTP");
			}
			catch(e) {
				xhr = false;
			}
		}
	}
	
	if (xhr) {
		xhr.open("GET", url, true);
		xhr.onreadystatechange = responseProcess;
		xhr.setRequestHeader("Connection", "Close");
		xhr.setRequestHeader("Method", "GET" + url + "HTTP/1.1");
		
		xhr.send(null);
	}
	return false;	
}

function responseProcess() {
	var i=0;
	var html = "";
	if (xhr.readyState == 4) {
		if (xhr.status == 200) {
			//JSON Processing
			var doc = eval('(' + xhr.responseText + ')');
			
			if (typeof(doc.results.result.Exception) != "undefined") {
				html = "<p align=\"center\" style=\"font\-size:30px; font\-weight:700px;\">Exception</p>\n";
				html += "<p align=\"center\" style=\"font\-size:30px; font\-weight:700px;\">" + doc.results.result.Exception + "</p>\n";
				document.getElementById('result').innerHTML = html;
				document.close();
			}
			else if (typeof(doc.results.result.Error) != "undefined") {
				html = "<p align=\"center\" style=\"font\-size:30px; font\-weight:700px;\">ERROR</p>\n";
				html += "<p align=\"center\" style=\"font\-size:30px; font\-weight:700px;\">IMDB: There was an error processing this request</p>\n";
				document.getElementById('result').innerHTML = html;
				document.close();
			}
			else {
				if (doc.results.result != 0) {
					html = "<p align=\"center\" style=\"font\-size:30px; font\-weight:700px;\">Displaying " + doc.results.result.length + " Results for " + title +"</p>";
					if (doc.results.result.length > 0) {
						html += "<table align=\"center\" border=\"2\">\n";
						html += "<tr align=\"center\"><td>Image</td><td>Title</td><td>Year</td><td>Director</td><td>Rating (10)</td><td>Link To Movie</td><td>Post to Facebook</td></tr>\n";			
						for (i=0;i < doc.results.result.length;i++) {
							html += "<tr align=\"center\"><td><img src=\"" + doc.results.result[i].cover + "\" height=\"74\" width=\"54\"></td><td>" + doc.results.result[i].title + "</td><td>" + doc.results.result[i].year + "</td><td>" + doc.results.result[i].director + "</td><td>" + doc.results.result[i].rating + "</td><td><a href=\"" + doc.results.result[i].details + "\">details</a></td><td><a href=\"javascript: onclick=publish('" + doc.results.result[i].cover + "','" + doc.results.result[i].title + "','" + doc.results.result[i].year + "','" + doc.results.result[i].rating + "','" + doc.results.result[i].details + "' );\"><img src=\"../images/facebook.jpg\" /></a></td></tr>\n";
						}
						html += "</table>\n";
					}
				}
				else {
					html = "<p align=\"center\" style=\"font\-size:30px; font\-weight:700px;\">No Results Found!</p>\n";
				}

				document.getElementById('result').innerHTML = html;
				document.close();
			}
		}
		else {
			alert("There was a problem retrieving the XML data:\n" + xhr.statusText);
		}
	}
}

function publish(cover, title, year, rating, details) {
	FB.ui(
	{
		method: 'feed',
		display: 'popup',
		name: title,
		caption: 'I am interested in this movie/series/game',
		description: (
			title + ' released in ' + year + ' has a rating of ' + rating
		),
		link: details,
		picture: cover,
		properties: {'Look at user reviews': {'text': 'here', 'href': details + 'reviews'}}
	},
	function(response) {
		if (response && response.post_id) {
			alert('Post was published.');
		}
		else {
			alert('Post was not published.');
		}
	}
);

}