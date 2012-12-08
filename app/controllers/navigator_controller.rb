class NavigatorController < ApplicationController
  def index
  end

  def location_update
  	lat = params[:lat]
  	lng = params[:lng]
  	name = params[:name]
  	Position.create(
  		:lat => lat,
  		:lng => lng,
  		:name => name
  	);
  	render :json => true
  end

  def get_last_location
  	@position = Position.last
  	render :json => @position
  end

  def postiions
  end
end
