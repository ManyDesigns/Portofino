drop database tt;
create database tt;
grant usage on *.* to tt@localhost identified by 'tt';
grant all privileges on tt.* to tt@localhost ;
flush privileges;
