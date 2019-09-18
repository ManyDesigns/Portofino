FROM ${project.groupId}/${project.artifactId}:${project.version}
#Link to database
ADD portofino-local.properties $CATALINA_HOME/webapps/ROOT/WEB-INF/
#For remote debugging (remember to open the port)
ENV JPDA_ADDRESS="8000"
ENV JPDA_TRANSPORT="dt_socket"
ENTRYPOINT ["catalina.sh", "jpda", "run"]
