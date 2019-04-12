#!/usr/bin/env bash
# start script via ./build-and-deploy.sh from outside
# start script via shell build-and-deploy.sh from IntelliJ

tomcatpath="/Applications/apache-tomcat"
ideapath="/Users/davidbuscholl/IdeaProjects/Masterthesis/prognosiscalculator/"
WEBAPPNAME="prognosiscalculator"

cd $tomcatpath
cd bin
./shutdown.sh

cd ..
cd webapps
ls
rmdir -f $webappname

cd ..
cd bin
./startup.sh

cd $ideapath
mvn clean package
mvn tomcat7:redeploy