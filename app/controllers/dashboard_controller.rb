
class DashboardController < ApplicationController
  def index
  	@foo = 'bar boo';

  	geoloqi_session = Geoloqi::Session.new(
	  :access_token => "YOUR APPLICATION ACCESS TOKEN"
	)

	result = geoloqi_session.post("trigger/create", {
	})
  end
end
