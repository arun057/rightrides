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
    if session[:last_lat_created_time]
      @positions = Position.all(
        :conditions => "DATE(created_at) > DATE('#{session[:last_lat_created_time]}')", 
        :order => 'positions.created_at ASC'
      )
    else
      @positions = Position.all(:order => 'positions.created_at ASC')
    end
    if !@positions.blank?
      session[:last_lat_created_time] = @positions.last.created_at
      @positions = @positions.index_by { |thing| thing.name }
    end
  	render :json => @positions
  end

  def postiions
  end
end
