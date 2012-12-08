// geoloqi

var tokens = [
  '5276-09d6f72728ee71713875d5d2c93304ba90ec040b',
  '5275-63de6cf05dbf8ea2076b29573fdf1f846967727e',
  '50f33-ce2184e40c3539cd6c565e72c5e19732d5a8a5e1'
];
var gl_location_url = "https://api.geoloqi.com/1/location/last";
var gl_profile_url = "https://api.geoloqi.com/1/account/profile";
var i = 0;
var pending_requests = 0;

function gl_profile_callback(data, position, date) {
  --pending_requests;
  console.log(data);
  current_date = new Date();
  update_date = new Date(date);
  if (current_date.getTime() - update_date.getTime() <= 21600000) // 6 hours in ms
  {
    drawMarker(data, position.latitude, position.longitude);
  }
}

function gl_location_callback(data, auth_token) {
  var position = data.location.position;
  var date = data.date;
  var url_with_token = gl_profile_url + "?callback=?&oauth_token=" + auth_token;
  console.log("calling geoloqi url: " + url_with_token);
  $.getJSON(url_with_token, function(data) {gl_profile_callback(data, position, date);});
}

function gl_refreshForToken(auth_token) {
  var url_with_token = gl_location_url + "?callback=?&oauth_token=" + auth_token;
  console.log("calling geoloqi url: " + url_with_token);
  ++pending_requests;
  $.getJSON(url_with_token, function(data) {gl_location_callback(data, auth_token);});
}

function gl_pendingRequests(){
  return pending_requests;
}

function gl_refreshAll() {
  for (j=0; j<tokens.length; j++) {
    gl_refreshForToken(tokens[j]);
  }
}


// index js
var tripRequestClickHandler = function (event) {
    var requestee = $("#requestee").val();
    var pick_up = $("#pick_up_address").val();
    var drop_off = $("#drop_off_address").val();
    var pick_up_time = new Date().getTime();
    alert("Request Mapped! Requestee: " + requestee + ", Pick-up: " + pick_up + " at "+ pick_up_time +", Drop-off: " + drop_off);
    // clear values
    $("#requestee").val("");
    $("#pick_up_address").val("");
    $("#drop_off_address").val("");
};

$(document).ready(function () {
	initializeMapping();
    $('#trip_request').click(tripRequestClickHandler);
});

// mapsupport js
var map;
var geocoder;
var customMarkers = [];
var geoloqiMarkers = [];
var directionsDisplay;
var directionsService;
var serviceAreas = new google.maps.KmlLayer('http://maps.google.com/maps/ms?ie=UTF8&oe=UTF8&authuser=0&msa=0&output=nl&msid=209738999438525933783.00000111e2265debed28b');
var geoloqi_refresh_rate = 15000; //15 sec

function initializeMapping() {
  var myOptions = {
    center: new google.maps.LatLng(40.767781718519, -73.985238918519),  // New York City
    zoom: 11,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  };
  
  map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
  geocoder = new google.maps.Geocoder();
  directionsDisplay = new google.maps.DirectionsRenderer();
  directionsService = new google.maps.DirectionsService();
 
  serviceAreas.setMap(map);
  directionsDisplay.setMap(map);

  setTimeout("autoRefreshGeoloqi();", geoloqi_refresh_rate);
}
  
function autoRefreshGeoloqi() {
  refreshMarkers();
  setTimeout("autoRefreshGeoloqi();", geoloqi_refresh_rate);
}

function refreshMarkers() {
  var pending_geoloqi_calls = gl_pendingRequests();

  if(pending_geoloqi_calls == 0){
    console.log("Num of customMarkers: " +customMarkers.length);
    clearGeoloqiMarkers();
    gl_refreshAll();
  }else{
    console.log("Skipping call to geoloqi, " + pending_geoloqi_calls + " calls currently open");
  }
}

function drawMarker(profile, latitude, longitude) {
  console.log("drawing " + profile.display_name + " at " + latitude + "," + longitude);
  var marker;
  var infowindow = new google.maps.InfoWindow();

  marker = new google.maps.Marker({
    position: new google.maps.LatLng(latitude, longitude),
    map: map,
    icon: '/assets/car_' + profile.display_name.toLowerCase() + '.png'
  });
  geoloqiMarkers.push(marker);

  console.log(marker);

  google.maps.event.addListener(marker, 'click', (function(marker, profile) {
    return function() {
      var html = "<table border='0'><tr>" +
        "<td><img src='" + profile.profile_image + "' /></td>" +
        "<td>" + profile.display_name + "<br />" + profile.phone + "</td>" +
        "</tr></table>";
      infowindow.setContent(html);
      infowindow.open(map, marker);
    }
  })(marker, profile));
}

function clearCustomMarkers() {
  while(customMarkers.length > 0) {
    var marker = customMarkers.pop();
    marker.setMap(null);
  }
}

function clearGeoloqiMarkers() {
  while(geoloqiMarkers.length > 0) {
    var marker = geoloqiMarkers.pop();
    marker.setMap(null);
  }
}

function geocode(address_string) {
  var addresses = address_string.split(" to ");
  if(addresses.length == 1) {
    geocoder.geocode({'address': address_string, 'partialmatch': true}, geocodeResult);
  } else if(addresses.length == 2) {
    displayDirections(addresses);
  } else {
    alert("Multiple destinations are not supported yet.");
  }
}

function geocodeResult(results, status) {
  if (status == 'OK' && results.length > 0) {
    var marker = new google.maps.Marker({
      position: new google.maps.LatLng(results[0].geometry.location.lat(), results[0].geometry.location.lng()),
      map: map
    });
    customMarkers.push(marker);
  } else {
    alert("Geocode was not successful for the following reason: " + status);
  }
}

function displayDirections(addresses) {
  var request = {
    origin: addresses[0],
    destination: addresses[1],
    travelMode: google.maps.TravelMode.DRIVING,
    region: 'United States'
  }
  directionsService.route(request, function(response, status) {
    if (status == google.maps.DirectionsStatus.OK) {
      directionsDisplay.setDirections(response);
    } else {
      console.log("DirectionsService was not successful for the following reason: " + status)
    }
  });
}


// function js
var item_global;
function getSpreadSheetData(){
	var rssurl = "https://spreadsheets.google.com/feeds/spreadsheets/private/full";
	$.get(rssurl, function(data) {
		var $xml = $(data);
		$xml.find("item").each(function() {
			item_global = $(this);
			//Do something with item here...
		});
	});
}
