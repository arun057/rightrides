
class DashboardController < ApplicationController
  before_filter :authenticate_user!
  def index
  	@foo = 'bar boo';

 #  	geoloqi_session = Geoloqi::Session.new(
	#   :access_token => "YOUR APPLICATION ACCESS TOKEN"
	# )
  end
end
