set TOMCATPATH="C:\Program Files\Apache\Tomcat\"
set IDEAPATH="C:\Users\David Buscholl\Documents\git\Masterthesis\prognosiscalculator\"
set WEBAPPNAME=prognosiscalculator

cd %TOMCATPATH%
cd bin
call shutdown
cd ..
cd webapps
del %WEBAPPNAME%
cd ..
cd bin
call startup
cd %IDEAPATH%
call mvn clean package
call mvn tomcat7:redeploy