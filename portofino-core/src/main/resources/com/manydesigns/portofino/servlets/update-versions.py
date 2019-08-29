#! /usr/bin/env python

import json
import sys
import urllib2

def get_latest_version(major):
    response = urllib2.urlopen("https://search.maven.org/solrsearch/select?q=g:com.manydesigns%20AND%20a:portofino%20AND%20v:"+ major + ".*&start=0&rows=1").read()
    return json.loads(response)["response"]["docs"][0]["v"]

directory = "./"
if(len(sys.argv) > 1):
    directory = sys.argv[1]
    if(not directory.endswith("/")):
        directory = directory + "/"

for version in ["4", "5"]:
    file = open(directory + version + ".x", "w")
    file.write(get_latest_version(version))
    file.close()
