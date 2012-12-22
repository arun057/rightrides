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


var dispatcher = function(){
  var positions = {},
        map,
        geocoder,
        customMarkers = [],
        carMarkers = {},
        directionsDisplay,
        directionsService,
        serviceAreas = new google.maps.KmlLayer('http://maps.google.com/maps/ms?ie=UTF8&oe=UTF8&authuser=0&msa=0&output=nl&msid=209738999438525933783.00000111e2265debed28b'),
        map_refresh_rate = 5000; //5 seconds

  function init(){
    initializeMapping();
    getUpdate();
  }

  function getUpdate(){
    $.ajax({
      url: 'last_position',
      dataType: 'json',
      type: 'GET',
      success: function(data){
        positions = data;
        updateMarkers();
      }
    });
  }

  function updateMarkers(){
    // do stuff here.
    if(positions){
      $.each(positions, function(key, value){
        //remove marker if it exists first.
        if(value.name){
          if(carMarkers[value.name]){
            carMarkers[value.name].setMap(null);
          }
          carMarkers[value.name] = drawMarker({name : value.name}, value.lat , value.lng);
        }
      });
    }
    setTimeout(getUpdate, map_refresh_rate);
  }

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

  }

  function drawMarker(profile, latitude, longitude) {
    var marker;
    var infowindow = new google.maps.InfoWindow();

    marker = new google.maps.Marker({
      position: new google.maps.LatLng(latitude, longitude),
      map: map,
      icon: '/assets/car_' + profile.name.toLowerCase() + '.png'
    });

    google.maps.event.addListener(marker, 'click', (function(marker, profile) {
      return function() {
        var html = "<table border='0'><tr>" +
          "<td><img src='" + profile + "' /></td>" +
          "<td>" + profile.name + "<br />" + "</td>" +
          "</tr></table>";
        infowindow.setContent(html);
        infowindow.open(map, marker);
      }
    })(marker, profile));

    return marker;
  }

  function clearCustomMarkers() {
    while(customMarkers.length > 0) {
      var marker = customMarkers.pop();
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

  return {
    init : init,
    geocode: geocode
  }
}();

$(document).ready(function () {
  $('#trip_request').click(tripRequestClickHandler);
  dispatcher.init();
});