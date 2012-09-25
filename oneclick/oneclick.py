#! /usr/bin/env python

'''
E' necessario che questo file sia accompagnato da un file local.py
Template del contenuto di local.py:

#! /usr/bin/env python

portofino_version = "4.0.6"
tomcat_dir = "apache-tomcat-6.0.35"
portofino_path = "~/projects/portofino4"
tomcat_url = "http://mirror.nohup.it/apache/tomcat/tomcat-6/v6.0.35/bin/apache-tomcat-6.0.35.zip"
drivers = [["postgresql/postgresql/9.1-901.jdbc3/", "postgresql-9.1-901.jdbc3.jar"],
           ["mysql/mysql-connector-java/5.1.15/", "mysql-connector-java-5.1.15.jar"],
           ["net/sourceforge/jtds/jtds/1.2.4/", "jtds-1.2.4.jar"]]

'''

import local
import urllib
import zipfile
import shutil
import os

#Derived variables
base_path = "portofino-" + local.portofino_version
portofino_path = os.path.expanduser(local.portofino_path)
tomcat_path = base_path + "/" + local.tomcat_dir
tomcat_zip = local.tomcat_dir + ".zip"

print """//////////////////////
Portofino build script
//////////////////////"""

if(not os.path.exists(base_path)):
    os.mkdir(base_path)

print "Downloading Tomcat..."
urllib.urlretrieve(local.tomcat_url, tomcat_zip)

if(os.path.exists(tomcat_path)):
    shutil.rmtree(tomcat_path)

os.system("unzip " + tomcat_zip + " -d " + base_path)

shutil.rmtree(tomcat_path + "/webapps/ROOT")

shutil.copy(portofino_path + "/LICENSE.txt", base_path + "/LICENSE.txt")
shutil.copy(portofino_path + "/THIRDPARTIES.txt", base_path + "/THIRDPARTIES.txt")

print "Downloading JDBC drivers..."
for driver in local.drivers:
    urllib.urlretrieve("http://repo1.maven.org/maven2/" + driver[0] + driver[1], tomcat_path + "/lib/" + driver[1])

os.system("pushd " + portofino_path + "; mvn clean install -Dmaven.test.skip=true; popd")

shutil.copy(portofino_path + "/portofino-war/target/portofino-war-" + local.portofino_version + ".war", tomcat_path + "/webapps/ROOT.war")

shutil.copy("setenv.sh", tomcat_path + "/bin")
shutil.copy("setenv.bat", tomcat_path + "/bin")

os.chmod(tomcat_path + "/bin/catalina.sh", 0755)
os.chmod(tomcat_path + "/bin/digest.sh", 0755)
os.chmod(tomcat_path + "/bin/setclasspath.sh", 0755)
os.chmod(tomcat_path + "/bin/shutdown.sh", 0755)
os.chmod(tomcat_path + "/bin/startup.sh", 0755)
os.chmod(tomcat_path + "/bin/tool-wrapper.sh", 0755)
os.chmod(tomcat_path + "/bin/version.sh", 0755)

zipfile = base_path + ".zip"
if(os.path.exists(zipfile)):
    os.remove(zipfile)

print "Building zip..."
os.system("zip -r " + zipfile + " " + base_path)

print "Done."
