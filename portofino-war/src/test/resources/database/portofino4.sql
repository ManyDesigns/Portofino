
CREATE TABLE public.emailqueue (
    id identity NOT NULL,
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




--
-- TOC entry 1527 (class 1259 OID 386721)
-- Dependencies: 5
-- Name: emailstate; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE public.emailstate (
    id identity NOT NULL,
    name character varying(75) NOT NULL,
    description character varying(255)
);



--
-- TOC entry 1523 (class 1259 OID 386697)
-- Dependencies: 5
-- Name: group_; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE public.group_ (
    groupid identity NOT NULL,
    creatorid bigint,
    parentgroupid bigint,
    name character varying(75),
    description character varying(255),
    active boolean,
    deldate timestamp 
);



--
-- TOC entry 1531 (class 1259 OID 386737)
-- Dependencies: 5
-- Name: msg; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE public.msg (
    id identity NOT NULL,
    subject character varying(100),
    body character varying(4000),
    add_userid bigint,
    sender_userid bigint,
    date date,
    createdate date,
    state integer
);



--
-- TOC entry 1533 (class 1259 OID 386748)
-- Dependencies: 5
-- Name: msgstate; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE public.msgstate (
    id identity NOT NULL,
    name character varying(75) NOT NULL,
    description character varying(255)
);




--
-- TOC entry 1529 (class 1259 OID 386729)
-- Dependencies: 5
-- Name: oldpwd; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE public.oldpwd (
    id identity NOT NULL,
    createdate numeric(31,0),
    password character varying(30),
    userid integer
);




--
-- TOC entry 1519 (class 1259 OID 386678)
-- Dependencies: 5
-- Name: user_; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE public.user_ (
    userid identity NOT NULL,
    deldate timestamp ,
    modifieddate timestamp ,
    defaultuser boolean,
    extauth boolean,
    pwd character varying(75),
    pwdencrypted boolean,
    pwdreset boolean,
    pwdmoddate timestamp ,
    token character varying(255),
    remquestion character varying(75),
    remans character varying(75),
    gracelogincount integer,
    screenname character varying(75),
    emailaddress character varying(75),
    greeting character varying(255),
    comments character varying(255),
    firstname character varying(75),
    middlename character varying(75),
    lastname character varying(75),
    jobtitle character varying(100),
    logindate timestamp ,
    loginip character varying(75),
    lastlogindate timestamp ,
    lastloginip character varying(75),
    lastfailedlogindate timestamp ,
    failedloginattempts integer,
    lockout boolean,
    lockoutdate timestamp ,
    agreedtoterms boolean,
    active boolean,
    state integer NOT NULL,
    createdate timestamp ,
    bounced integer
);



--
-- TOC entry 1534 (class 1259 OID 386815)
-- Dependencies: 5
-- Name: users_groups; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE public.users_groups (
    groupid integer NOT NULL,
    userid integer NOT NULL,
    deletiondate timestamp ,
    creationdate timestamp 
);


--
-- TOC entry 1521 (class 1259 OID 386689)
-- Dependencies: 5
-- Name: userstate; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE public.userstate (
    id identity NOT NULL,
    name character varying(75) NOT NULL,
    description character varying(255)
);




INSERT INTO public.group_ (groupid, creatorid, parentgroupid, name, description, active, deldate) VALUES (2, 1, NULL, 'users', 'user', true, NULL);
INSERT INTO public.group_ (groupid, creatorid, parentgroupid, name, description, active, deldate) VALUES (1, 1, NULL, 'admin', 'admin', true, NULL);

INSERT INTO public.user_ (userid, deldate, modifieddate, defaultuser, extauth, pwd, pwdencrypted, pwdreset, pwdmoddate, token, remquestion, remans, gracelogincount, screenname, emailaddress, greeting, comments, firstname, middlename, lastname, jobtitle, logindate, loginip, lastlogindate, lastloginip, lastfailedlogindate, failedloginattempts, lockout, lockoutdate, agreedtoterms, active, state, createdate, bounced) VALUES (11, NULL, NULL, NULL, NULL, 'piero74', NULL, NULL, NULL, '', '', '', NULL, 'giampi', 'granatella@gmail.com', '', '-', '', '', '', '', NULL, '', NULL, '', NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO public.user_ (userid, deldate, modifieddate, defaultuser, extauth, pwd, pwdencrypted, pwdreset, pwdmoddate, token, remquestion, remans, gracelogincount, screenname, emailaddress, greeting, comments, firstname, middlename, lastname, jobtitle, logindate, loginip, lastlogindate, lastloginip, lastfailedlogindate, failedloginattempts, lockout, lockoutdate, agreedtoterms, active, state, createdate, bounced) VALUES (1, NULL, '2010-08-12 00:00:00', false, false, 'admin', false, false, '2010-09-24 15:48:09.458', NULL, '', NULL, 3, 'admin', 'admin@manydesigns.com', '', '', 'Giampiero', 'GG', 'Granatella', 'Ing.', NULL, '0:0:0:0:0:0:0:1%0', '2010-09-24 15:47:42.121', '', '2010-09-24 15:47:38.475', 0, false, NULL, false, true, 1, '2009-09-29 00:00:00', NULL);



INSERT INTO public.users_groups (groupid, userid, deletiondate, creationdate) VALUES (2, 1, NULL, NULL);
INSERT INTO public.users_groups (groupid, userid, deletiondate, creationdate) VALUES (1, 11, NULL, NULL);
INSERT INTO public.users_groups (groupid, userid, deletiondate, creationdate) VALUES (1, 1, NULL, '2010-09-14 00:00:00');


INSERT INTO public.userstate (id, name, description) VALUES (1, 'active', NULL);









--
-- TOC entry 1837 (class 2606 OID 386819)
-- Dependencies: 1534 1534 1534
-- Name: users_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE  public.users_groups
    ADD CONSTRAINT users_groups_pkey PRIMARY KEY (groupid, userid);


--
-- TOC entry 1823 (class 2606 OID 386694)
-- Dependencies: 1521 1521
-- Name: userstate_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--


--

ALTER TABLE  public.group_
    ADD CONSTRAINT fk_group_1 FOREIGN KEY (creatorid) REFERENCES public.user_(userid) ;


--
-- TOC entry 1842 (class 2606 OID 386754)
-- Dependencies: 1531 1533 1834
-- Name: fk_msg_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  public.msg
    ADD CONSTRAINT fk_msg_1 FOREIGN KEY (state) REFERENCES public.msgstate(id) ;


--
-- TOC entry 1843 (class 2606 OID 386759)
-- Dependencies: 1820 1519 1531
-- Name: fk_msg_2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  public.msg
    ADD CONSTRAINT fk_msg_2 FOREIGN KEY (add_userid) REFERENCES public.user_(userid) ;


--
-- TOC entry 1844 (class 2606 OID 386764)
-- Dependencies: 1531 1820 1519
-- Name: fk_msg_3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  public.msg
    ADD CONSTRAINT fk_msg_3 FOREIGN KEY (sender_userid) REFERENCES public.user_(userid) ;


--
-- TOC entry 1841 (class 2606 OID 386769)
-- Dependencies: 1820 1529 1519
-- Name: fk_oldpwd_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  public.oldpwd
    ADD CONSTRAINT fk_oldpwd_1 FOREIGN KEY (userid) REFERENCES public.user_(userid) ;


--
-- TOC entry 1838 (class 2606 OID 386789)
-- Dependencies: 1822 1521 1519
-- Name: fk_user_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  public.user_
    ADD CONSTRAINT fk_user_1 FOREIGN KEY (state) REFERENCES public.userstate(id) ;


--
-- TOC entry 1845 (class 2606 OID 386820)
-- Dependencies: 1534 1820 1519
-- Name: fk_usersgroups_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  public.users_groups
    ADD CONSTRAINT fk_usersgroups_1 FOREIGN KEY (userid) REFERENCES public.user_(userid);


-- Completed on 2010-09-27 09:27:34 CEST

--
-- PostgreSQL database dump complete
--

