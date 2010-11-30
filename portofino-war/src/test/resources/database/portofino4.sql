DROP SCHEMA IF EXISTS "public";
CREATE SCHEMA "public";
DROP TABLE IF EXISTS "emailqueue";
CREATE TABLE "public"."emailqueue" (
    "id" identity NOT NULL,
    "subject" character varying(100),
    "body" character varying(4000),
    "to_" character varying(100),
    "from_" character varying(100),
    "createdate" date,
    "state" integer,
    "attachmentpath" character varying(255),
    "attachmentdescription" character varying(255),
    "attachmentname" character varying(255)
);




--
-- TOC entry 1527 (class 1259 O""id"" 386721)
-- Dependencies: 5
-- Name: emailstate; Type: TABLE; Schema: "public"; Owner: -; Tablespace: 
--
DROP TABLE IF EXISTS "emailstate";
CREATE TABLE "public"."emailstate" (
    "id" identity NOT NULL,
    "name" character varying(75) NOT NULL,
    "description" character varying(255)
);



--
-- TOC entry 1523 (class 1259 O""id"" 386697)
-- Dependencies: 5
-- "name": group_; Type: TABLE; Schema: "public"; Owner: -; Tablespace: 
--
DROP TABLE IF EXISTS "groups";
CREATE TABLE "public"."groups" (
    "groupid" identity NOT NULL,
    "creatorid" bigint,
    "name" character varying(50),
    "description" character varying(255),
    "deletiondate" timestamp,
    "creationdate" timestamp
);



--
-- TOC entry 1531 (class 1259 O""id"" 386737)
-- Dependencies: 5
-- "name": msg; Type: TABLE; Schema: "public"; Owner: -; Tablespace: 
--
DROP TABLE IF EXISTS "msg";
CREATE TABLE "public"."msg" (
    "id" identity NOT NULL,
    "subject" character varying(255),
    "body" character varying(4000),
    "to_" bigint,
    "from_" bigint,
    "date" date,
    "creationdate" timestamp,
    "state" integer
);



--
-- TOC entry 1533 (class 1259 O""id"" 386748)
-- Dependencies: 5
-- "name": msgstate; Type: TABLE; Schema: "public"; Owner: -; Tablespace: 
--
DROP TABLE IF EXISTS "msgstate";
CREATE TABLE "public"."msgstate" (
    "id" identity NOT NULL,
    "name" character varying(50) NOT NULL,
    "description" character varying(255)
);




--
-- TOC entry 1529 (class 1259 O""id"" 386729)
-- Dependencies: 5
-- "name": oldpwd; Type: TABLE; Schema: "public"; Owner: -; Tablespace: 
--
DROP TABLE IF EXISTS "oldpwd";
CREATE TABLE "public"."oldpwd" (
    "id" identity NOT NULL,
    "createdate" timestamp,
    "password" character varying(30),
    "userid" integer
);




--
-- TOC entry 1519 (class 1259 O""id"" 386678)
-- Dependencies: 5
-- "name": user_; Type: TABLE; Schema: "public"; Owner: -; Tablespace: 
--
DROP TABLE IF EXISTS "users";
CREATE TABLE "public"."users" (
    "userid" identity NOT NULL,
    "deletiondate" timestamp,
    "creationdate" timestamp,
    "modifieddate" timestamp,
    "defaultuser" boolean,
    "extauth" boolean,
    "pwd" character varying(50),
    "lastpwdmoddate" timestamp,
    "username" character varying(50),
    "emailaddress" character varying(100),
    "firstname" character varying(50),
    "middlename" character varying(50),
    "lastname" character varying(50),
    "jobtitle" character varying(50),
    "lastlogindate" timestamp,
    "lastfailedlogindate" timestamp,
    "failedloginattempts" integer,
    "agreedtoterms" boolean,
    "state" long NOT NULL,
    "bounced" integer,
    "token" character varying(100),
    "remquestion" character varying(255),
    "remans" character varying(255),
    "gracelogincount" integer
);



--
-- TOC entry 1534 (class 1259 O""id"" 386815)
-- Dependencies: 5
-- "name": users_groups; Type: TABLE; Schema: "public"; Owner: -; Tablespace: 
--
DROP TABLE IF EXISTS "users_groups";
CREATE TABLE "public"."users_groups" (
    "groupid" integer NOT NULL,
    "userid" integer NOT NULL,
    "deletiondate" timestamp ,
    "creationdate" timestamp NOT NULL
);


--
-- TOC entry 1521 (class 1259 O""id"" 386689)
-- Dependencies: 5
-- "name": userstate; Type: TABLE; Schema: "public"; Owner: -; Tablespace: 
--
DROP TABLE IF EXISTS "userstate";
CREATE TABLE "public"."userstate" (
    "id" identity NOT NULL,
    "name" character varying(75) NOT NULL,
    "description" character varying(255)
);


INSERT INTO "public"."users"("userid", "creationdate", "pwd", "username", "emailaddress", "firstname", "lastname", "state") VALUES (1, current_timestamp, 'admin', 'admin', 'giampiero.granatella@manydesigns.com', 'admin', 'admin', 1);
INSERT INTO "public"."users"("userid", "creationdate", "pwd", "username", "emailaddress", "firstname", "lastname", "state") VALUES (2, current_timestamp, 'giampi', 'giampi', 'giampiero.granatella@manydesigns.com', 'giampi', 'giampi', 1);


INSERT INTO "public"."groups" ("groupid", "creatorid",  "name", "description",  "creationdate") VALUES (2, 1,  '"users"', 'user',  current_timestamp);
INSERT INTO "public"."groups" ("groupid", "creatorid",  "name", "description",  "creationdate") VALUES (1, 1,  'admin', 'admin', current_timestamp);

INSERT INTO "public"."users_groups" VALUES (1,1, null, current_timestamp);
INSERT INTO "public"."users_groups" VALUES (2,1, null, current_timestamp);
INSERT INTO "public"."users_groups" VALUES (2,2, null, current_timestamp);


INSERT INTO "public"."userstate" ("id", "name", "description") VALUES (1, 'active', NULL);
INSERT INTO "public"."userstate" ("id", "name", "description") VALUES (2, 'suspended', NULL);
INSERT INTO "public"."userstate" ("id", "name", "description") VALUES (3, 'banned', NULL);
INSERT INTO "public"."userstate" ("id", "name", "description") VALUES (4, 'selfregistred', NULL);

INSERT INTO "public"."emailstate" ("id", "name", "description") VALUES (0, 'sending', NULL);
INSERT INTO "public"."emailstate" ("id", "name", "description") VALUES (1, 'to be sent', NULL);
INSERT INTO "public"."emailstate" ("id", "name", "description") VALUES (2, 'sent', NULL);
INSERT INTO "public"."emailstate" ("id", "name", "description") VALUES (3, 'rejected', NULL);
INSERT INTO "public"."emailstate" ("id", "name", "description") VALUES (4, '"bounced"', NULL);

INSERT INTO "public"."msgstate" ("id", "name", "description") VALUES (1, 'sent', NULL);
INSERT INTO "public"."msgstate" ("id", "name", "description") VALUES (2, 'read', NULL);





--
-- TOC entry 1837 (class 2606 O""id"" 386819)
-- Dependencies: 1534 1534 1534
-- "name": users_groups_pkey; Type: CONSTRAINT; Schema: "public"; Owner: -; Tablespace: 
--

ALTER TABLE  "public"."users_groups"
    ADD CONSTRAINT users_groups_pkey PRIMARY KEY ("groupid", "userid", "creationdate");


--
-- TOC entry 1823 (class 2606 O""id"" 386694)
-- Dependencies: 1521 1521
-- "name": userstate_pkey; Type: CONSTRAINT; Schema: "public"; Owner: -; Tablespace: 
--


--

ALTER TABLE  "public"."groups"
    ADD CONSTRAINT fk_group_1 FOREIGN KEY ("creatorid") REFERENCES "public"."users"("userid") ;


--
-- TOC entry 1842 (class 2606 O""id"" 386754)
-- Dependencies: 1531 1533 1834
-- "name": fk_msg_1; Type: FK CONSTRAINT; Schema: "public"; Owner: -
--

ALTER TABLE  "public"."msg"
    ADD CONSTRAINT fk_msg_1 FOREIGN KEY ("state") REFERENCES "public"."msgstate"("id") ;


--
-- TOC entry 1843 (class 2606 O""id"" 386759)
-- Dependencies: 1820 1519 1531
-- "name": fk_msg_2; Type: FK CONSTRAINT; Schema: "public"; Owner: -
--

ALTER TABLE  "public"."msg"
    ADD CONSTRAINT fk_msg_2 FOREIGN KEY ("from_") REFERENCES "public"."users"("userid") ;


--
-- TOC entry 1844 (class 2606 O""id"" 386764)
-- Dependencies: 1531 1820 1519
-- "name": fk_msg_3; Type: FK CONSTRAINT; Schema: "public"; Owner: -
--

ALTER TABLE  "public"."msg"
    ADD CONSTRAINT fk_msg_3 FOREIGN KEY ("to_") REFERENCES "public"."users"("userid") ;


--
-- TOC entry 1841 (class 2606 O""id"" 386769)
-- Dependencies: 1820 1529 1519
-- "name": fk_oldpwd_1; Type: FK CONSTRAINT; Schema: "public"; Owner: -
--

ALTER TABLE  "public"."oldpwd"
    ADD CONSTRAINT fk_oldpwd_1 FOREIGN KEY ("userid") REFERENCES "public"."users"("userid") ;


--
-- TOC entry 1838 (class 2606 O""id"" 386789)
-- Dependencies: 1822 1521 1519
-- "name": fk_user_1; Type: FK CONSTRAINT; Schema: "public"; Owner: -
--

ALTER TABLE  "public"."users"
    ADD CONSTRAINT fk_user_1 FOREIGN KEY ("state") REFERENCES "public"."userstate"("id") ;


--
-- TOC entry 1845 (class 2606 O""id"" 386820)
-- Dependencies: 1534 1820 1519
-- "name": fk_usersgroups_1; Type: FK CONSTRAINT; Schema: "public"; Owner: -
--

ALTER TABLE  "public"."users_groups"
    ADD CONSTRAINT fk_usersgroups_1 FOREIGN KEY ("userid") REFERENCES "public"."users"("userid");

ALTER TABLE  "public"."users_groups"
    ADD CONSTRAINT fk_usersgroups_2 FOREIGN KEY ("groupid") REFERENCES "public"."groups"("groupid");
-- Completed on 2010-09-27 09:27:34 CEST

--
-- PostgreSQL database dump complete
--

