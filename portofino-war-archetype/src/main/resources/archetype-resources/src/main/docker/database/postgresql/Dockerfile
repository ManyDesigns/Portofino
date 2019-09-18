#Postgres database. See https://docs.docker.com/samples/library/postgres/
FROM postgres:11
RUN mkdir -p /docker-entrypoint-initdb.d
ADD *.sh /docker-entrypoint-initdb.d/
ADD *.sql /docker-entrypoint-initdb.d/
