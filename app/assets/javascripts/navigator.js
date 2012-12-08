function autoUpdate() {
  navigator.geolocation.getCurrentPosition(function(position) {  
    var lat = position.coords.latitude;
    var lng = position.coords.longitude;

    updatePosition(lat,lng);
  });
  setTimeout(autoUpdate, 5000);
}

function updatePosition(lat, lng){
	$.ajax({
		url: '/api/update/location',
		dataType: 'json',
		type: 'POST',
		data: {
			'lat' : lat,
			'lng' : lng,
			'name' : 'somename'
		},
		success: function(){
			if(console) console.log('success');
		}
	});
}

$(document).ready(function(){
	autoUpdate();
});