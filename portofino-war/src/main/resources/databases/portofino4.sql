
CREATE TABLE emailqueue (
    id bigserial NOT NULL,
    subject character varying(100),
    body character varying(4000),
    to_ character varying(100),
    from_ character varying(100),
    createdate date,
    state integer,
    attachmentpath character varying(255),
    attachmentdescription character varying(255),
    attachmentname character varying(255)
);



CREATE TABLE emailstate (
    id bigserial NOT NULL,
    name character varying(75) NOT NULL,
    description character varying(255)
);


CREATE TABLE groups (
    groupid bigserial NOT NULL,
    creatorid bigint,
    name character varying(50),
    description character varying(255),
    deletiondate timestamp,
    creationdate timestamp
);

CREATE TABLE msg (
    id bigserial NOT NULL,
    subject character varying(255),
    body character varying(4000),
    to_ bigint,
    from_ bigint,
    creationdate timestamp,
    state integer
);

CREATE TABLE msgstate (
    id bigserial NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255)
);

CREATE TABLE oldpwd (
    id bigserial NOT NULL,
    createdate timestamp,
    password character varying(30),
    userid integer
);

CREATE TABLE users (
    userid bigserial NOT NULL,
    deletiondate timestamp,
    creationdate timestamp,
    modifieddate timestamp,
    defaultuser boolean,
    extauth boolean,
    pwd character varying(50),
    lastpwdmoddate timestamp,
    username character varying(50),
    emailaddress character varying(100) NOT NULL,
    firstname character varying(50),
    middlename character varying(50),
    lastname character varying(50),
    jobtitle character varying(50),
    lastlogindate timestamp,
    lastfailedlogindate timestamp,
    failedloginattempts integer,
    agreedtoterms boolean,
    state bigint NOT NULL,
    bounced integer,
    token character varying(100),
    remquestion character varying(255),
    remans character varying(255),
    gracelogincount integer
);

CREATE TABLE users_groups (
    groupid bigint NOT NULL,
    userid bigint NOT NULL,
    deletiondate timestamp,
    creationdate timestamp NOT NULL
);

CREATE TABLE userstate (
    id bigserial NOT NULL,
    name character varying(75) NOT NULL,
    description character varying(255)
);

INSERT INTO users( creationdate, pwd, username, emailaddress, firstname, lastname, state) VALUES (current_timestamp, 'admin', 'admin', 'giampiero.granatella@manydesigns.com', 'admin', 'admin', 1);

INSERT INTO emailstate (id, name, description) VALUES (0, 'sending', NULL);
INSERT INTO emailstate (name, description) VALUES ( 'to be sent', NULL);
INSERT INTO emailstate (name, description) VALUES ( 'sent', NULL);
INSERT INTO emailstate (name, description) VALUES ( 'rejected', NULL);
INSERT INTO emailstate (name, description) VALUES ( 'bounced', NULL);

INSERT INTO msgstate (name, description) VALUES ('sent', NULL);
INSERT INTO msgstate (name, description) VALUES ('read', NULL);

INSERT INTO groups ( creatorid,  name, description, creationdate) VALUES (1, 'admins', 'admins',  current_timestamp);
INSERT INTO groups ( creatorid,  name, description, creationdate) VALUES (1, 'users', 'user', current_timestamp);


INSERT INTO userstate (name, description) VALUES ( 'active', NULL);
INSERT INTO userstate (name, description) VALUES ( 'suspended', NULL);
INSERT INTO userstate (name, description) VALUES ( 'banned', NULL);
INSERT INTO userstate (name, description) VALUES ( 'selfregistred', NULL);

INSERT INTO users_groups(
            groupid, userid, deletiondate, creationdate)
    VALUES (1, 1, null, current_timestamp);

ALTER TABLE ONLY emailqueue
    ADD CONSTRAINT "EmailQueue_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY oldpwd
    ADD CONSTRAINT "OldPwd_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY emailstate
    ADD CONSTRAINT emailstate_pkey PRIMARY KEY (id);

ALTER TABLE ONLY groups
    ADD CONSTRAINT group_pkey PRIMARY KEY (groupid);


ALTER TABLE ONLY msg
    ADD CONSTRAINT msg_pkey PRIMARY KEY (id);

ALTER TABLE ONLY msgstate
    ADD CONSTRAINT msgstate_pkey PRIMARY KEY (id);

ALTER TABLE ONLY users
    ADD CONSTRAINT user_pkey PRIMARY KEY (userid);

ALTER TABLE  public.users_groups
    ADD CONSTRAINT users_groups_pkey PRIMARY KEY (groupid, userid, creationdate);

ALTER TABLE ONLY userstate
    ADD CONSTRAINT userstate_pkey PRIMARY KEY (id);

ALTER TABLE ONLY emailqueue
    ADD CONSTRAINT fk_emailqueue_1 FOREIGN KEY (state) REFERENCES emailstate(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY groups
    ADD CONSTRAINT fk_group_1 FOREIGN KEY (creatorid) REFERENCES users(userid) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY msg
    ADD CONSTRAINT fk_msg_1 FOREIGN KEY (state) REFERENCES msgstate(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY msg
    ADD CONSTRAINT fk_msg_2 FOREIGN KEY (from_) REFERENCES users(userid) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY msg
    ADD CONSTRAINT fk_msg_3 FOREIGN KEY (to_) REFERENCES users(userid) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY oldpwd
    ADD CONSTRAINT fk_oldpwd_1 FOREIGN KEY (userid) REFERENCES users(userid) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY users
    ADD CONSTRAINT fk_user_1 FOREIGN KEY (state) REFERENCES userstate(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY users_groups
    ADD CONSTRAINT fk_usersgroups_1 FOREIGN KEY (userid) REFERENCES users(userid) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE  public.users_groups
    ADD CONSTRAINT fk_usersgroups_2 FOREIGN KEY (groupid) REFERENCES public.groups(groupid);

