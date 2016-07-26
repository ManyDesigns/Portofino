#! /usr/bin/env python

'''
You must create a "local.py" using the following template:

#! /usr/bin/env python

portofino_version = "4.2.2"
tomcat_dir = "apache-tomcat-8.5.4"
portofino_path = "~/projects/portofino4"
tomcat_url = "http://mirror.nohup.it/apache/tomcat/tomcat-8/v8.5.4/bin/apache-tomcat-8.5.4.zip"
drivers = [["org/postgresql/postgresql/9.3-1102-jdbc4/", "postgresql-9.3-1102-jdbc4.jar"],
           ["mysql/mysql-connector-java/5.1.32/", "mysql-connector-java-5.1.32.jar"],
           ["net/sourceforge/jtds/jtds/1.3.1/", "jtds-1.3.1.jar"]]

'''

import local
import urllib
import zipfile
import shutil
import os

#Derived variables
build_path = "build"
portofino_dir = "portofino-" + local.portofino_version
base_path = build_path + "/" + portofino_dir
oneclick_path = build_path + "/portofino-oneclick"
portofino_path = os.path.expanduser(local.portofino_path)
tomcat_path = base_path + "/" + local.tomcat_dir
tomcat_zip = build_path + "/" + local.tomcat_dir + ".zip"

print """//////////////////////
Portofino build script
//////////////////////"""

if(os.path.exists(base_path)):
    shutil.rmtree(build_path)

os.makedirs(base_path)

print "Downloading Tomcat..."
class MyURLopener(urllib.FancyURLopener):
    def http_error_default(self, url, fp, errcode, errmsg, headers):
        raise IOError("Could not download Tomcat, error is " + errmsg + " (" + str(errcode) + ")")

urllib._urlopener = MyURLopener()

urllib.urlretrieve(local.tomcat_url, tomcat_zip)

if(os.path.exists(tomcat_path)):
    shutil.rmtree(tomcat_path)

os.system("unzip " + tomcat_zip + " -d " + base_path)

shutil.rmtree(tomcat_path + "/webapps/ROOT")
shutil.copy(portofino_path + "/README.md", base_path + "/README.md")
shutil.copy(portofino_path + "/COPYRIGHT.txt", base_path + "/COPYRIGHT.txt")
shutil.copy(portofino_path + "/LICENSE.txt", base_path + "/LICENSE.txt")
shutil.copy(portofino_path + "/THIRDPARTIES.txt", base_path + "/THIRDPARTIES.txt")

print "Downloading JDBC drivers..."
for driver in local.drivers:
    urllib.urlretrieve("http://repo1.maven.org/maven2/" + driver[0] + driver[1], tomcat_path + "/lib/" + driver[1])

command = "cd " + portofino_path + "; mvn clean install"
print "Building Portofino with command: " + command
os.system(command)

print "Generating oneclick from archetype..."
os.system("cd " + build_path + "; mvn archetype:generate -DarchetypeArtifactId=portofino-war-archetype -DarchetypeGroupId=com.manydesigns -DarchetypeVersion=" + local.portofino_version + " -DinteractiveMode=false -DgroupId=com.manydesigns -DartifactId=portofino-oneclick -Dversion=" + local.portofino_version + "")

print "Building oneclick..."
os.system("cd " + oneclick_path + "; mvn clean package")


shutil.copy(oneclick_path + "/target/portofino-oneclick-" + local.portofino_version + ".war", tomcat_path + "/webapps/ROOT.war")

shutil.copy("setenv.sh", tomcat_path + "/bin")
shutil.copy("setenv.bat", tomcat_path + "/bin")

os.chmod(tomcat_path + "/bin/catalina.sh", 0755)
os.chmod(tomcat_path + "/bin/digest.sh", 0755)
os.chmod(tomcat_path + "/bin/setclasspath.sh", 0755)
os.chmod(tomcat_path + "/bin/shutdown.sh", 0755)
os.chmod(tomcat_path + "/bin/startup.sh", 0755)
os.chmod(tomcat_path + "/bin/tool-wrapper.sh", 0755)
os.chmod(tomcat_path + "/bin/version.sh", 0755)

print "Building zip..."
cwd = os.getcwd()
os.chdir(build_path)
try:
    zipfile = portofino_dir + ".zip"
    if(os.path.exists(zipfile)):
        os.remove(zipfile)
    os.system("zip -r " + zipfile + " " + portofino_dir)
finally:
    os.chdir(cwd)

print "Done."
