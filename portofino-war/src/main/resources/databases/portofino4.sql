--
-- PostgreSQL database dump
--

-- Started on 2010-08-25 11:01:45 CEST

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 331 (class 2612 OID 16386)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: -
--

CREATE PROCEDURAL LANGUAGE plpgsql;


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1526 (class 1259 OID 386710)
-- Dependencies: 5
-- Name: emailqueue; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE emailqueue (
    id integer NOT NULL,
    subject character varying(100),
    body character varying(4000),
    addressee character varying(100),
    sender character varying(100),
    date date,
    createdate date,
    failedattempts bigint,
    send boolean,
    state integer
);


--
-- TOC entry 1525 (class 1259 OID 386708)
-- Dependencies: 1526 5
-- Name: emailqueue_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE emailqueue_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1858 (class 0 OID 0)
-- Dependencies: 1525
-- Name: emailqueue_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE emailqueue_id_seq OWNED BY emailqueue.id;


--
-- TOC entry 1859 (class 0 OID 0)
-- Dependencies: 1525
-- Name: emailqueue_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('emailqueue_id_seq', 1, false);


--
-- TOC entry 1528 (class 1259 OID 386721)
-- Dependencies: 5
-- Name: emailstate; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE emailstate (
    id integer NOT NULL,
    name character varying(75) NOT NULL,
    description character varying(255)
);


--
-- TOC entry 1527 (class 1259 OID 386719)
-- Dependencies: 1528 5
-- Name: emailstate_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE emailstate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1860 (class 0 OID 0)
-- Dependencies: 1527
-- Name: emailstate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE emailstate_id_seq OWNED BY emailstate.id;


--
-- TOC entry 1861 (class 0 OID 0)
-- Dependencies: 1527
-- Name: emailstate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('emailstate_id_seq', 1, false);


--
-- TOC entry 1523 (class 1259 OID 386697)
-- Dependencies: 5
-- Name: group_; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE group_ (
    groupid integer NOT NULL,
    creatorid bigint,
    parentgroupid bigint,
    name character varying(75),
    description character varying(255),
    active boolean,
    deldate timestamp without time zone
);


--
-- TOC entry 1522 (class 1259 OID 386695)
-- Dependencies: 1523 5
-- Name: group_groupid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE group_groupid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1862 (class 0 OID 0)
-- Dependencies: 1522
-- Name: group_groupid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE group_groupid_seq OWNED BY group_.groupid;


--
-- TOC entry 1863 (class 0 OID 0)
-- Dependencies: 1522
-- Name: group_groupid_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('group_groupid_seq', 1, false);


--
-- TOC entry 1532 (class 1259 OID 386737)
-- Dependencies: 5
-- Name: msg; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE msg (
    id integer NOT NULL,
    subject character varying(100),
    body character varying(4000),
    add_userid bigint,
    sender_userid bigint,
    date date,
    createdate date,
    state integer
);


--
-- TOC entry 1531 (class 1259 OID 386735)
-- Dependencies: 5 1532
-- Name: msg_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE msg_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1864 (class 0 OID 0)
-- Dependencies: 1531
-- Name: msg_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE msg_id_seq OWNED BY msg.id;


--
-- TOC entry 1865 (class 0 OID 0)
-- Dependencies: 1531
-- Name: msg_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('msg_id_seq', 1, false);


--
-- TOC entry 1534 (class 1259 OID 386748)
-- Dependencies: 5
-- Name: msgstate; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE msgstate (
    id integer NOT NULL,
    name character varying(75) NOT NULL,
    description character varying(255)
);


--
-- TOC entry 1533 (class 1259 OID 386746)
-- Dependencies: 1534 5
-- Name: msgstate_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE msgstate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1866 (class 0 OID 0)
-- Dependencies: 1533
-- Name: msgstate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE msgstate_id_seq OWNED BY msgstate.id;


--
-- TOC entry 1867 (class 0 OID 0)
-- Dependencies: 1533
-- Name: msgstate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('msgstate_id_seq', 1, false);


--
-- TOC entry 1530 (class 1259 OID 386729)
-- Dependencies: 5
-- Name: oldpwd; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE oldpwd (
    id integer NOT NULL,
    createdate numeric(31,0),
    password character varying(30),
    userid integer
);


--
-- TOC entry 1529 (class 1259 OID 386727)
-- Dependencies: 1530 5
-- Name: oldpwd_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE oldpwd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1868 (class 0 OID 0)
-- Dependencies: 1529
-- Name: oldpwd_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE oldpwd_id_seq OWNED BY oldpwd.id;


--
-- TOC entry 1869 (class 0 OID 0)
-- Dependencies: 1529
-- Name: oldpwd_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('oldpwd_id_seq', 1, false);


--
-- TOC entry 1519 (class 1259 OID 386678)
-- Dependencies: 5
-- Name: user_; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_ (
    userid integer NOT NULL,
    deldate timestamp without time zone,
    modifieddate timestamp without time zone,
    defaultuser boolean,
    extauth boolean,
    pwd character varying(75),
    pwdencrypted boolean,
    pwdreset boolean,
    pwdmoddate timestamp without time zone,
    digest character varying(255),
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
    lockout boolean,
    lockoutdate timestamp without time zone,
    agreedtoterms boolean,
    active boolean,
    state integer NOT NULL,
    createdate timestamp without time zone
);


--
-- TOC entry 1518 (class 1259 OID 386676)
-- Dependencies: 1519 5
-- Name: user_userid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_userid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1870 (class 0 OID 0)
-- Dependencies: 1518
-- Name: user_userid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE user_userid_seq OWNED BY user_.userid;


--
-- TOC entry 1871 (class 0 OID 0)
-- Dependencies: 1518
-- Name: user_userid_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_userid_seq', 4, true);


--
-- TOC entry 1524 (class 1259 OID 386703)
-- Dependencies: 5
-- Name: users_groups; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE users_groups (
    groupid bigint NOT NULL,
    userid bigint NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    deletiondate timestamp without time zone
);


--
-- TOC entry 1521 (class 1259 OID 386689)
-- Dependencies: 5
-- Name: userstate; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE userstate (
    id integer NOT NULL,
    name character varying(75) NOT NULL,
    description character varying(255)
);


--
-- TOC entry 1520 (class 1259 OID 386687)
-- Dependencies: 1521 5
-- Name: userstate_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE userstate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1872 (class 0 OID 0)
-- Dependencies: 1520
-- Name: userstate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE userstate_id_seq OWNED BY userstate.id;


--
-- TOC entry 1873 (class 0 OID 0)
-- Dependencies: 1520
-- Name: userstate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('userstate_id_seq', 1, false);


--
-- TOC entry 1815 (class 2604 OID 386713)
-- Dependencies: 1526 1525 1526
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE emailqueue ALTER COLUMN id SET DEFAULT nextval('emailqueue_id_seq'::regclass);


--
-- TOC entry 1816 (class 2604 OID 386724)
-- Dependencies: 1527 1528 1528
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE emailstate ALTER COLUMN id SET DEFAULT nextval('emailstate_id_seq'::regclass);


--
-- TOC entry 1814 (class 2604 OID 386700)
-- Dependencies: 1522 1523 1523
-- Name: groupid; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE group_ ALTER COLUMN groupid SET DEFAULT nextval('group_groupid_seq'::regclass);


--
-- TOC entry 1818 (class 2604 OID 386740)
-- Dependencies: 1531 1532 1532
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE msg ALTER COLUMN id SET DEFAULT nextval('msg_id_seq'::regclass);


--
-- TOC entry 1819 (class 2604 OID 386751)
-- Dependencies: 1533 1534 1534
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE msgstate ALTER COLUMN id SET DEFAULT nextval('msgstate_id_seq'::regclass);


--
-- TOC entry 1817 (class 2604 OID 386732)
-- Dependencies: 1530 1529 1530
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE oldpwd ALTER COLUMN id SET DEFAULT nextval('oldpwd_id_seq'::regclass);


--
-- TOC entry 1812 (class 2604 OID 386681)
-- Dependencies: 1519 1518 1519
-- Name: userid; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE user_ ALTER COLUMN userid SET DEFAULT nextval('user_userid_seq'::regclass);


--
-- TOC entry 1813 (class 2604 OID 386692)
-- Dependencies: 1521 1520 1521
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE userstate ALTER COLUMN id SET DEFAULT nextval('userstate_id_seq'::regclass);


--
-- TOC entry 1850 (class 0 OID 386710)
-- Dependencies: 1526
-- Data for Name: emailqueue; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1851 (class 0 OID 386721)
-- Dependencies: 1528
-- Data for Name: emailstate; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1848 (class 0 OID 386697)
-- Dependencies: 1523
-- Data for Name: group_; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO group_ (groupid, creatorid, parentgroupid, name, description, active, deldate) VALUES (1, 1, NULL, 'admin', 'admin', true, NULL);
INSERT INTO group_ (groupid, creatorid, parentgroupid, name, description, active, deldate) VALUES (2, 1, NULL, 'users', 'user', true, NULL);


--
-- TOC entry 1853 (class 0 OID 386737)
-- Dependencies: 1532
-- Data for Name: msg; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1854 (class 0 OID 386748)
-- Dependencies: 1534
-- Data for Name: msgstate; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1852 (class 0 OID 386729)
-- Dependencies: 1530
-- Data for Name: oldpwd; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1846 (class 0 OID 386678)
-- Dependencies: 1519
-- Data for Name: user_; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO user_ (userid, deldate, modifieddate, defaultuser, extauth, pwd, pwdencrypted, pwdreset, pwdmoddate, digest, remquestion, remans, gracelogincount, screenname, emailaddress, greeting, comments, firstname, middlename, lastname, jobtitle, logindate, loginip, lastlogindate, lastloginip, lastfailedlogindate, failedloginattempts, lockout, lockoutdate, agreedtoterms, active, state, createdate) VALUES (1, NULL, NULL, NULL, NULL, 'admin', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin@manydesigns.com', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL);


--
-- TOC entry 1849 (class 0 OID 386703)
-- Dependencies: 1524
-- Data for Name: users_groups; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO users_groups (groupid, userid, creationdate, deletiondate) VALUES (2, 1, '2010-08-24 00:00:00', NULL);
INSERT INTO users_groups (groupid, userid, creationdate, deletiondate) VALUES (1, 1, '2010-08-24 00:00:00', NULL);


--
-- TOC entry 1847 (class 0 OID 386689)
-- Dependencies: 1521
-- Data for Name: userstate; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO userstate (id, name, description) VALUES (1, 'active', NULL);


--
-- TOC entry 1829 (class 2606 OID 386718)
-- Dependencies: 1526 1526
-- Name: EmailQueue_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY emailqueue
    ADD CONSTRAINT "EmailQueue_pkey" PRIMARY KEY (id);


--
-- TOC entry 1833 (class 2606 OID 386734)
-- Dependencies: 1530 1530
-- Name: OldPwd_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY oldpwd
    ADD CONSTRAINT "OldPwd_pkey" PRIMARY KEY (id);


--
-- TOC entry 1831 (class 2606 OID 386726)
-- Dependencies: 1528 1528
-- Name: emailstate_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY emailstate
    ADD CONSTRAINT emailstate_pkey PRIMARY KEY (id);


--
-- TOC entry 1825 (class 2606 OID 386702)
-- Dependencies: 1523 1523
-- Name: group_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY group_
    ADD CONSTRAINT group_pkey PRIMARY KEY (groupid);


--
-- TOC entry 1835 (class 2606 OID 386745)
-- Dependencies: 1532 1532
-- Name: msg_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY msg
    ADD CONSTRAINT msg_pkey PRIMARY KEY (id);


--
-- TOC entry 1837 (class 2606 OID 386753)
-- Dependencies: 1534 1534
-- Name: msgstate_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY msgstate
    ADD CONSTRAINT msgstate_pkey PRIMARY KEY (id);


--
-- TOC entry 1821 (class 2606 OID 386686)
-- Dependencies: 1519 1519
-- Name: user_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_
    ADD CONSTRAINT user_pkey PRIMARY KEY (userid);


--
-- TOC entry 1827 (class 2606 OID 386707)
-- Dependencies: 1524 1524 1524
-- Name: users_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY users_groups
    ADD CONSTRAINT users_groups_pkey PRIMARY KEY (groupid, userid);


--
-- TOC entry 1823 (class 2606 OID 386694)
-- Dependencies: 1521 1521
-- Name: userstate_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userstate
    ADD CONSTRAINT userstate_pkey PRIMARY KEY (id);


--
-- TOC entry 1841 (class 2606 OID 386774)
-- Dependencies: 1526 1830 1528
-- Name: fk_emailqueue_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY emailqueue
    ADD CONSTRAINT fk_emailqueue_1 FOREIGN KEY (state) REFERENCES emailstate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1839 (class 2606 OID 386784)
-- Dependencies: 1523 1820 1519
-- Name: fk_group_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY group_
    ADD CONSTRAINT fk_group_1 FOREIGN KEY (creatorid) REFERENCES user_(userid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1843 (class 2606 OID 386754)
-- Dependencies: 1836 1534 1532
-- Name: fk_msg_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY msg
    ADD CONSTRAINT fk_msg_1 FOREIGN KEY (state) REFERENCES msgstate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1844 (class 2606 OID 386759)
-- Dependencies: 1532 1519 1820
-- Name: fk_msg_2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY msg
    ADD CONSTRAINT fk_msg_2 FOREIGN KEY (add_userid) REFERENCES user_(userid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1845 (class 2606 OID 386764)
-- Dependencies: 1532 1820 1519
-- Name: fk_msg_3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY msg
    ADD CONSTRAINT fk_msg_3 FOREIGN KEY (sender_userid) REFERENCES user_(userid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1842 (class 2606 OID 386769)
-- Dependencies: 1820 1519 1530
-- Name: fk_oldpwd_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY oldpwd
    ADD CONSTRAINT fk_oldpwd_1 FOREIGN KEY (userid) REFERENCES user_(userid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1838 (class 2606 OID 386789)
-- Dependencies: 1521 1822 1519
-- Name: fk_user_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_
    ADD CONSTRAINT fk_user_1 FOREIGN KEY (state) REFERENCES userstate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1840 (class 2606 OID 386779)
-- Dependencies: 1820 1524 1519
-- Name: fk_usersgroups_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users_groups
    ADD CONSTRAINT fk_usersgroups_1 FOREIGN KEY (userid) REFERENCES user_(userid) DEFERRABLE INITIALLY DEFERRED;


-- Completed on 2010-08-25 11:01:45 CEST

--
-- PostgreSQL database dump complete
--

