#!/bin/bash
# quick hack to replicate the functionality of generate-sprite.js on a linux machine.
# released under the BSD license below
#
# Copyright (c) 2010, Yuval Levy http://www.photopla.net/
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#       notice, this list of conditions and the following disclaimer in the
#       documentation and/or other materials provided with the distribution.
#     * Neither the name of Yuval Levy nor the names of other contributors
#       may be used to endorse or promote products derived from this software
#       without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY Yuval Levy ``AS IS'' AND ANY
# EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL Yuval Levy BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

LOCALE='en'
if [ ${#} -gt 0 ]; then
  # TODO:
  # validate input
  LOCALE=$1
fi

JSFILE=js/openid-${LOCALE}.js

if [ ! -s ${JSFILE} ]; then
  echo "locale not found"
  exit 1
fi

# TODO:
# extract openid.locale and  openid.sprite = 'en' from JSFILE
# if openid.locale not same as $LOCALE, quit with mismatch error
# if openid.sprite not same as openid.local, quit with error saying that the sprite from the other locale is used
# for now the script assumes the user to be smart enough to validate input
i=0
SMALLCMD=""
LARGECMD=""

./remcomments.sed < ${JSFILE} | sed -n '/providers_large/,/;/p' | sed -n '/: {/p' | sed 's/.*\t\(.*\) : {/\1/' > tmp.txt
while read a
do
  SMALLCMD="${SMALLCMD} images.small/${a}.ico.png"
  LARGECMD="${LARGECMD} images.large/${a}.gif"
  i=$((i+1))
done < tmp.txt
LARGECMD="${LARGECMD} -tile ${i}x1 -geometry 100x60>+0+0 large.bmp"

./remcomments.sed < ${JSFILE} | sed -n '/providers_small/,/;/p' | sed -n '/: {/p' | sed 's/.*\t\(.*\) : {/\1/' > tmp.txt
while read a
do
  SMALLCMD="${SMALLCMD} images.small/${a}.ico.png"
  i=$((i+1)) 
done < tmp.txt
SMALLCMD="${SMALLCMD} -tile ${i}x1 -geometry 16x16+4+4 small.bmp"

`montage ${SMALLCMD}`
`montage ${LARGECMD}`
convert large.bmp small.bmp -append images/openid-providers-${LOCALE}.png
rm tmp.txt
rm small.bmp
rm large.bmp
echo "done"
