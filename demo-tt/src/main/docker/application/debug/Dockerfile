FROM demo-tt:${project.version}
#Link to database
COPY portofino-local.properties $CATALINA_HOME/webapps/ROOT/WEB-INF/
#For remote debugging (remember to open the port)
ENV JPDA_ADDRESS="*:8000"
ENV JPDA_TRANSPORT="dt_socket"
ENV PORTOFINO_APPLICATION_DIRECTORY=/application
ENTRYPOINT ["catalina.sh", "jpda", "run"]
