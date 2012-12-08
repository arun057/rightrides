
class DashboardController < ApplicationController
  before_filter :authenticate_user!
  def index
  	@foo = 'bar boo';

  	geoloqi_session = Geoloqi::Session.new(
	  # :access_token => "de29246efe1c3c8222daaa6df063b158" // this is the app id
	  :access_token => "50f33-ce2184e40c3539cd6c565e72c5e19732d5a8a5e1"
	)
  end
end
