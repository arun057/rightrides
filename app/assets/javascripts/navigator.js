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
	if(showStuff){
		showUpdate();
	}else{
		autoUpdate();
	}
});


function showUpdate(){
	$.ajax({
		url: 'last_position',
		dataType: 'json',
		type: 'GET',
		success: function(data){
			$('#positions').append($('<div>' + data.lat + ' - ' + data.lng + '</div><br/>'));
		}
	});
	setTimeout(showUpdate, 5000);
}