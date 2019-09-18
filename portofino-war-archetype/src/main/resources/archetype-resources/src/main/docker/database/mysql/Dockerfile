#Mysql database. See https://docs.docker.com/samples/library/mysql/
FROM mysql:5.7
RUN mkdir -p /docker-entrypoint-initdb.d
ADD *.sh /docker-entrypoint-initdb.d/
ADD *.sql /docker-entrypoint-initdb.d/
