#!/usr/bin/env ruby

#	Simple OpenID Plugin
#	http://code.google.com/p/openid-selector/
#	
#	This code is licenced under the New BSD License.
#
#
# Requires rubygems (tested with 1.3.7) therubyracer (tested with 0.7.5),
# and ImageMagick (tested with 6.6.5).
# Adjust path to ruby in the first line above

require 'rubygems'
require 'v8'

imagemagick = ""  # substitute specific path if needed
lang = ARGV[0] || "en"

cxt = V8::Context.new
cxt['openid'] = {}
cxt.load "js/openid-#{lang}.js"

# generate small montage
cmd = "#{imagemagick}montage"
i = 0
cxt['providers_large'].each do |provider_id, details|
  cmd += " images.small/#{provider_id}.ico.png"
  i += 1
end
cxt['providers_small'].each do |provider_id, details|
  cmd += " images.small/#{provider_id}.ico.png"
  i += 1
end
cmd += " -tile #{i}x1 -geometry '16x16+4+4' temp_small.bmp"
`#{cmd}`

# generate large montage
cmd = "#{imagemagick}montage"
i = 0
cxt['providers_large'].each do |provider_id, details|
  cmd += " images.large/#{provider_id}.gif"
  i += 1
end
cmd += " -tile #{i}x1 -geometry '100x60>+0+0' temp_large.bmp"
`#{cmd}`

# generate final montage
cmd = "#{imagemagick}convert temp_large.bmp temp_small.bmp -append images/openid-providers-#{lang}.png"
`#{cmd}`

# clean up
`rm temp_large.bmp temp_small.bmp`

nil