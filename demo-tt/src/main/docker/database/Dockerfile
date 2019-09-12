FROM postgres:9.4
RUN mkdir -p /docker-entrypoint-initdb.d
ADD *.sh /docker-entrypoint-initdb.d/
ADD *.sql /docker-entrypoint-initdb.d/
