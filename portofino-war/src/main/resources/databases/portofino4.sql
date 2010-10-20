
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


CREATE TABLE group_ (
    groupid bigserial NOT NULL,
    creatorid bigint,
    parentgroupid bigint,
    name character varying(75),
    description character varying(255),
    active boolean,
    deldate timestamp without time zone
);

CREATE TABLE msg (
    id bigserial NOT NULL,
    subject character varying(100),
    body character varying(4000),
    add_userid bigint,
    sender_userid bigint,
    date date,
    createdate date,
    state integer
);



CREATE TABLE msgstate (
    id bigserial NOT NULL,
    name character varying(75) NOT NULL,
    description character varying(255)
);

CREATE TABLE oldpwd (
    id bigserial NOT NULL,
    createdate numeric(31,0),
    password character varying(30),
    userid integer
);

CREATE TABLE user_ (
    userid bigserial NOT NULL,
    deldate timestamp without time zone,
    modifieddate timestamp without time zone,
    defaultuser boolean,
    extauth boolean,
    pwd character varying(75),
    pwdmoddate timestamp without time zone,
    token character varying(255),
    remquestion character varying(75),
    remans character varying(75),
    gracelogincount integer,
    screenname character varying(75),
    emailaddress character varying(75),
    greeting character varying(255),
    comments text,
    firstname character varying(75),
    middlename character varying(75),
    lastname character varying(75),
    jobtitle character varying(100),
    logindate timestamp without time zone,
    loginip character varying(75),
    lastlogindate timestamp without time zone,
    lastloginip character varying(75),
    lastfailedlogindate timestamp without time zone,
    failedloginattempts integer,
    agreedtoterms boolean,
    state integer NOT NULL,
    createdate timestamp without time zone,
    bounced integer
);


CREATE TABLE users_groups (
    groupid bigint NOT NULL,
    userid bigint NOT NULL,
    deletiondate timestamp without time zone,
    creationdate timestamp without time zone
);

CREATE TABLE userstate (
    id bigserial NOT NULL,
    name character varying(75) NOT NULL,
    description character varying(255)
);


INSERT INTO emailstate (id, name, description) VALUES (1, 'to be sent', NULL);
INSERT INTO emailstate (id, name, description) VALUES (2, 'sent', NULL);
INSERT INTO emailstate (id, name, description) VALUES (3, 'rejected', NULL);
INSERT INTO emailstate (id, name, description) VALUES (4, 'bounced', NULL);
INSERT INTO emailstate (id, name, description) VALUES (0, 'sending', NULL);
INSERT INTO group_ (groupid, creatorid, parentgroupid, name, description, active, deldate) VALUES (2, 1, NULL, 'users', 'user', true, NULL);
INSERT INTO group_ (groupid, creatorid, parentgroupid, name, description, active, deldate) VALUES (1, 1, NULL, 'admin', 'admin', true, NULL);

INSERT INTO user_ ( deldate, modifieddate, defaultuser, extauth, pwd, pwdmoddate, token, remquestion, remans, gracelogincount, screenname, emailaddress, greeting, comments, firstname, middlename, lastname, jobtitle, logindate, loginip, lastlogindate, lastloginip, lastfailedlogindate, failedloginattempts, agreedtoterms,  state, createdate, bounced) VALUES ( NULL, '2010-08-12 00:00:00', false, false, 'admin', '2010-10-04 17:29:29.045', NULL, '', NULL, 3, 'admin', 'giampiero.granatella@manydesigns.com', '', '', 'Giampiero', 'GG', 'Granatella', 'Ing.', NULL, '0:0:0:0:0:0:0:1%0', '2010-10-18 15:56:13.836', '', '2010-10-13 16:41:28.07', 0, false, 1, '2009-09-29 00:00:00', NULL);

INSERT INTO users_groups (groupid, userid, deletiondate, creationdate) VALUES (1, 1, NULL, '2010-09-14 00:00:00');

INSERT INTO userstate (id, name, description) VALUES (1, 'active', NULL);

ALTER TABLE ONLY emailqueue
    ADD CONSTRAINT "EmailQueue_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY oldpwd
    ADD CONSTRAINT "OldPwd_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY emailstate
    ADD CONSTRAINT emailstate_pkey PRIMARY KEY (id);

ALTER TABLE ONLY group_
    ADD CONSTRAINT group_pkey PRIMARY KEY (groupid);


ALTER TABLE ONLY msg
    ADD CONSTRAINT msg_pkey PRIMARY KEY (id);

ALTER TABLE ONLY msgstate
    ADD CONSTRAINT msgstate_pkey PRIMARY KEY (id);

ALTER TABLE ONLY user_
    ADD CONSTRAINT user_pkey PRIMARY KEY (userid);

ALTER TABLE ONLY users_groups
    ADD CONSTRAINT users_groups_pkey PRIMARY KEY (groupid, userid);

ALTER TABLE ONLY userstate
    ADD CONSTRAINT userstate_pkey PRIMARY KEY (id);

ALTER TABLE ONLY emailqueue
    ADD CONSTRAINT fk_emailqueue_1 FOREIGN KEY (state) REFERENCES emailstate(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY group_
    ADD CONSTRAINT fk_group_1 FOREIGN KEY (creatorid) REFERENCES user_(userid) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY msg
    ADD CONSTRAINT fk_msg_1 FOREIGN KEY (state) REFERENCES msgstate(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY msg
    ADD CONSTRAINT fk_msg_2 FOREIGN KEY (add_userid) REFERENCES user_(userid) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY msg
    ADD CONSTRAINT fk_msg_3 FOREIGN KEY (sender_userid) REFERENCES user_(userid) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY oldpwd
    ADD CONSTRAINT fk_oldpwd_1 FOREIGN KEY (userid) REFERENCES user_(userid) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY user_
    ADD CONSTRAINT fk_user_1 FOREIGN KEY (state) REFERENCES userstate(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY users_groups
    ADD CONSTRAINT fk_usersgroups_1 FOREIGN KEY (userid) REFERENCES user_(userid) DEFERRABLE INITIALLY DEFERRED;

