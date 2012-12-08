class ApplicationController < ActionController::Base
  protect_from_forgery
  before_filter :set_access_control_headers

  private
  def set_access_control_headers 
    headers['X-Frame-Options'] = 'SAMEORIGIN, GOFORIT'
  end
end
