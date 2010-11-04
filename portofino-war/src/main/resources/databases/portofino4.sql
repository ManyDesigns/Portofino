SET client_encoding = 'UTF8';

--
-- TOC entry 1519 (class 1259 OID 425088)
-- Dependencies: 3
-- Name: emailqueue; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE emailqueue (
    id bigint NOT NULL,
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
-- TOC entry 1518 (class 1259 OID 425086)
-- Dependencies: 3 1519
-- Name: emailqueue_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE emailqueue_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1860 (class 0 OID 0)
-- Dependencies: 1518
-- Name: emailqueue_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE emailqueue_id_seq OWNED BY emailqueue.id;


--
-- TOC entry 1861 (class 0 OID 0)
-- Dependencies: 1518
-- Name: emailqueue_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('emailqueue_id_seq', 1, false);


--
-- TOC entry 1521 (class 1259 OID 425097)
-- Dependencies: 3
-- Name: emailstate; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE emailstate (
    id bigint NOT NULL,
    name character varying(75) NOT NULL,
    description character varying(255)
);


--
-- TOC entry 1520 (class 1259 OID 425095)
-- Dependencies: 1521 3
-- Name: emailstate_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE emailstate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1862 (class 0 OID 0)
-- Dependencies: 1520
-- Name: emailstate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE emailstate_id_seq OWNED BY emailstate.id;


--
-- TOC entry 1863 (class 0 OID 0)
-- Dependencies: 1520
-- Name: emailstate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('emailstate_id_seq', 1, false);


--
-- TOC entry 1523 (class 1259 OID 425103)
-- Dependencies: 3
-- Name: groups; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE groups (
    groupid bigint NOT NULL,
    creatorid bigint,
    name character varying(50),
    description character varying(255),
    deletiondate timestamp without time zone,
    creationdate timestamp without time zone
);


--
-- TOC entry 1522 (class 1259 OID 425101)
-- Dependencies: 1523 3
-- Name: groups_groupid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE groups_groupid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1864 (class 0 OID 0)
-- Dependencies: 1522
-- Name: groups_groupid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE groups_groupid_seq OWNED BY groups.groupid;


--
-- TOC entry 1865 (class 0 OID 0)
-- Dependencies: 1522
-- Name: groups_groupid_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('groups_groupid_seq', 1, false);


--
-- TOC entry 1525 (class 1259 OID 425109)
-- Dependencies: 3
-- Name: msg; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE msg (
    id bigint NOT NULL,
    subject character varying(255),
    body character varying(4000),
    to_ bigint,
    from_ bigint,
    creationdate timestamp without time zone,
    state integer
);


--
-- TOC entry 1524 (class 1259 OID 425107)
-- Dependencies: 3 1525
-- Name: msg_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE msg_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1866 (class 0 OID 0)
-- Dependencies: 1524
-- Name: msg_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE msg_id_seq OWNED BY msg.id;


--
-- TOC entry 1867 (class 0 OID 0)
-- Dependencies: 1524
-- Name: msg_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('msg_id_seq', 1, false);


--
-- TOC entry 1527 (class 1259 OID 425118)
-- Dependencies: 3
-- Name: msgstate; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE msgstate (
    id bigint NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255)
);


--
-- TOC entry 1526 (class 1259 OID 425116)
-- Dependencies: 3 1527
-- Name: msgstate_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE msgstate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1868 (class 0 OID 0)
-- Dependencies: 1526
-- Name: msgstate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE msgstate_id_seq OWNED BY msgstate.id;


--
-- TOC entry 1869 (class 0 OID 0)
-- Dependencies: 1526
-- Name: msgstate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('msgstate_id_seq', 1, false);


--
-- TOC entry 1529 (class 1259 OID 425124)
-- Dependencies: 3
-- Name: oldpwd; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE oldpwd (
    id bigint NOT NULL,
    createdate timestamp without time zone,
    password character varying(30),
    userid integer
);


--
-- TOC entry 1528 (class 1259 OID 425122)
-- Dependencies: 1529 3
-- Name: oldpwd_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE oldpwd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1870 (class 0 OID 0)
-- Dependencies: 1528
-- Name: oldpwd_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE oldpwd_id_seq OWNED BY oldpwd.id;


--
-- TOC entry 1871 (class 0 OID 0)
-- Dependencies: 1528
-- Name: oldpwd_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('oldpwd_id_seq', 1, false);


--
-- TOC entry 1531 (class 1259 OID 425130)
-- Dependencies: 3
-- Name: users; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE users (
    userid bigint NOT NULL,
    deletiondate timestamp without time zone,
    creationdate timestamp without time zone,
    modifieddate timestamp without time zone,
    defaultuser boolean,
    extauth boolean,
    pwd character varying(50),
    lastpwdmoddate timestamp without time zone,
    username character varying(50),
    emailaddress character varying(100) NOT NULL,
    firstname character varying(50),
    middlename character varying(50),
    lastname character varying(50),
    jobtitle character varying(50),
    lastlogindate timestamp without time zone,
    lastfailedlogindate timestamp without time zone,
    failedloginattempts integer,
    agreedtoterms boolean,
    state bigint NOT NULL,
    bounced integer,
    token character varying(100),
    remquestion character varying(255),
    remans character varying(255),
    gracelogincount integer
);


--
-- TOC entry 1532 (class 1259 OID 425137)
-- Dependencies: 3
-- Name: users_groups; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE users_groups (
    groupid bigint NOT NULL,
    userid bigint NOT NULL,
    deletiondate timestamp without time zone,
    creationdate timestamp without time zone NOT NULL
);


--
-- TOC entry 1530 (class 1259 OID 425128)
-- Dependencies: 1531 3
-- Name: users_userid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE users_userid_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1872 (class 0 OID 0)
-- Dependencies: 1530
-- Name: users_userid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE users_userid_seq OWNED BY users.userid;


--
-- TOC entry 1873 (class 0 OID 0)
-- Dependencies: 1530
-- Name: users_userid_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('users_userid_seq', 1, false);


--
-- TOC entry 1534 (class 1259 OID 425142)
-- Dependencies: 3
-- Name: userstate; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE userstate (
    id bigint NOT NULL,
    name character varying(75) NOT NULL,
    description character varying(255)
);


--
-- TOC entry 1533 (class 1259 OID 425140)
-- Dependencies: 3 1534
-- Name: userstate_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE userstate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1874 (class 0 OID 0)
-- Dependencies: 1533
-- Name: userstate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE userstate_id_seq OWNED BY userstate.id;


--
-- TOC entry 1875 (class 0 OID 0)
-- Dependencies: 1533
-- Name: userstate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('userstate_id_seq', 1, false);


--
-- TOC entry 1812 (class 2604 OID 425091)
-- Dependencies: 1518 1519 1519
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE emailqueue ALTER COLUMN id SET DEFAULT nextval('emailqueue_id_seq'::regclass);


--
-- TOC entry 1813 (class 2604 OID 425100)
-- Dependencies: 1521 1520 1521
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE emailstate ALTER COLUMN id SET DEFAULT nextval('emailstate_id_seq'::regclass);


--
-- TOC entry 1814 (class 2604 OID 425106)
-- Dependencies: 1523 1522 1523
-- Name: groupid; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE groups ALTER COLUMN groupid SET DEFAULT nextval('groups_groupid_seq'::regclass);


--
-- TOC entry 1815 (class 2604 OID 425112)
-- Dependencies: 1524 1525 1525
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE msg ALTER COLUMN id SET DEFAULT nextval('msg_id_seq'::regclass);


--
-- TOC entry 1816 (class 2604 OID 425121)
-- Dependencies: 1527 1526 1527
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE msgstate ALTER COLUMN id SET DEFAULT nextval('msgstate_id_seq'::regclass);


--
-- TOC entry 1817 (class 2604 OID 425127)
-- Dependencies: 1528 1529 1529
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE oldpwd ALTER COLUMN id SET DEFAULT nextval('oldpwd_id_seq'::regclass);


--
-- TOC entry 1818 (class 2604 OID 425133)
-- Dependencies: 1530 1531 1531
-- Name: userid; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE users ALTER COLUMN userid SET DEFAULT nextval('users_userid_seq'::regclass);


--
-- TOC entry 1819 (class 2604 OID 425145)
-- Dependencies: 1533 1534 1534
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE userstate ALTER COLUMN id SET DEFAULT nextval('userstate_id_seq'::regclass);


--
-- TOC entry 1846 (class 0 OID 425088)
-- Dependencies: 1519
-- Data for Name: emailqueue; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1847 (class 0 OID 425097)
-- Dependencies: 1521
-- Data for Name: emailstate; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO emailstate (id, name, description) VALUES (0, 'sending', NULL);
INSERT INTO emailstate (id, name, description) VALUES (1, 'to be sent', NULL);
INSERT INTO emailstate (id, name, description) VALUES (2, 'sent', NULL);
INSERT INTO emailstate (id, name, description) VALUES (3, 'rejected', NULL);
INSERT INTO emailstate (id, name, description) VALUES (4, 'bounced', NULL);


--
-- TOC entry 1848 (class 0 OID 425103)
-- Dependencies: 1523
-- Data for Name: groups; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO groups (groupid, creatorid, name, description, deletiondate, creationdate) VALUES (2, 1, 'users', 'user', NULL, '2010-10-27 16:29:49.169171');
INSERT INTO groups (groupid, creatorid, name, description, deletiondate, creationdate) VALUES (1, 1, 'admin', 'admin', NULL, '2010-10-27 16:29:49.169171');


--
-- TOC entry 1849 (class 0 OID 425109)
-- Dependencies: 1525
-- Data for Name: msg; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1850 (class 0 OID 425118)
-- Dependencies: 1527
-- Data for Name: msgstate; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO msgstate (id, name, description) VALUES (1, 'sent', NULL);
INSERT INTO msgstate (id, name, description) VALUES (2, 'read', NULL);


--
-- TOC entry 1851 (class 0 OID 425124)
-- Dependencies: 1529
-- Data for Name: oldpwd; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1852 (class 0 OID 425130)
-- Dependencies: 1531
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO users (userid, deletiondate, creationdate, modifieddate, defaultuser, extauth, pwd, lastpwdmoddate, username, emailaddress, firstname, middlename, lastname, jobtitle, lastlogindate, lastfailedlogindate, failedloginattempts, agreedtoterms, state, bounced, token, remquestion, remans, gracelogincount) VALUES (1, NULL, '2010-10-27 16:29:49.169171', NULL, NULL, NULL, 'admin', NULL, 'admin', 'giampiero.granatella@manydesigns.com', 'admin', NULL, 'admin', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL);


--
-- TOC entry 1853 (class 0 OID 425137)
-- Dependencies: 1532
-- Data for Name: users_groups; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO users_groups (groupid, userid, deletiondate, creationdate) VALUES (2, 1, '2010-10-27 17:59:04.989', '2010-10-27 17:58:59.03');
INSERT INTO users_groups (groupid, userid, deletiondate, creationdate) VALUES (1, 1, NULL, '2010-10-28 12:27:50.623');
INSERT INTO users_groups (groupid, userid, deletiondate, creationdate) VALUES (2, 1, '2010-10-28 13:59:10.844', '2010-10-27 17:59:10.889');
INSERT INTO users_groups (groupid, userid, deletiondate, creationdate) VALUES (1, 1, '2010-10-28 13:59:10.886', '2010-10-28 12:27:25.168');


--
-- TOC entry 1854 (class 0 OID 425142)
-- Dependencies: 1534
-- Data for Name: userstate; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO userstate (id, name, description) VALUES (1, 'active', NULL);
INSERT INTO userstate (id, name, description) VALUES (2, 'suspended', NULL);
INSERT INTO userstate (id, name, description) VALUES (3, 'banned', NULL);
INSERT INTO userstate (id, name, description) VALUES (4, 'selfregistred', NULL);


--
-- TOC entry 1821 (class 2606 OID 425147)
-- Dependencies: 1519 1519
-- Name: EmailQueue_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY emailqueue
    ADD CONSTRAINT "EmailQueue_pkey" PRIMARY KEY (id);


--
-- TOC entry 1831 (class 2606 OID 425149)
-- Dependencies: 1529 1529
-- Name: OldPwd_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY oldpwd
    ADD CONSTRAINT "OldPwd_pkey" PRIMARY KEY (id);


--
-- TOC entry 1823 (class 2606 OID 425151)
-- Dependencies: 1521 1521
-- Name: emailstate_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY emailstate
    ADD CONSTRAINT emailstate_pkey PRIMARY KEY (id);


--
-- TOC entry 1825 (class 2606 OID 425153)
-- Dependencies: 1523 1523
-- Name: group_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT group_pkey PRIMARY KEY (groupid);


--
-- TOC entry 1827 (class 2606 OID 425155)
-- Dependencies: 1525 1525
-- Name: msg_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY msg
    ADD CONSTRAINT msg_pkey PRIMARY KEY (id);


--
-- TOC entry 1829 (class 2606 OID 425157)
-- Dependencies: 1527 1527
-- Name: msgstate_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY msgstate
    ADD CONSTRAINT msgstate_pkey PRIMARY KEY (id);


--
-- TOC entry 1833 (class 2606 OID 425159)
-- Dependencies: 1531 1531
-- Name: user_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT user_pkey PRIMARY KEY (userid);


--
-- TOC entry 1835 (class 2606 OID 425161)
-- Dependencies: 1532 1532 1532 1532
-- Name: users_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY users_groups
    ADD CONSTRAINT users_groups_pkey PRIMARY KEY (groupid, userid, creationdate);


--
-- TOC entry 1837 (class 2606 OID 425163)
-- Dependencies: 1534 1534
-- Name: userstate_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userstate
    ADD CONSTRAINT userstate_pkey PRIMARY KEY (id);


--
-- TOC entry 1838 (class 2606 OID 425164)
-- Dependencies: 1521 1519 1822
-- Name: fk_emailqueue_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY emailqueue
    ADD CONSTRAINT fk_emailqueue_1 FOREIGN KEY (state) REFERENCES emailstate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1839 (class 2606 OID 425169)
-- Dependencies: 1531 1832 1523
-- Name: fk_group_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT fk_group_1 FOREIGN KEY (creatorid) REFERENCES users(userid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1840 (class 2606 OID 425174)
-- Dependencies: 1525 1527 1828
-- Name: fk_msg_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY msg
    ADD CONSTRAINT fk_msg_1 FOREIGN KEY (state) REFERENCES msgstate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1841 (class 2606 OID 425179)
-- Dependencies: 1832 1525 1531
-- Name: fk_msg_2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY msg
    ADD CONSTRAINT fk_msg_2 FOREIGN KEY (from_) REFERENCES users(userid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1842 (class 2606 OID 425184)
-- Dependencies: 1832 1531 1525
-- Name: fk_msg_3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY msg
    ADD CONSTRAINT fk_msg_3 FOREIGN KEY (to_) REFERENCES users(userid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1843 (class 2606 OID 425189)
-- Dependencies: 1531 1832 1529
-- Name: fk_oldpwd_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY oldpwd
    ADD CONSTRAINT fk_oldpwd_1 FOREIGN KEY (userid) REFERENCES users(userid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1844 (class 2606 OID 425236)
-- Dependencies: 1836 1531 1534
-- Name: fk_user_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fk_user_1 FOREIGN KEY (state) REFERENCES userstate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1845 (class 2606 OID 425199)
-- Dependencies: 1532 1832 1531
-- Name: fk_usersgroups_1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users_groups
    ADD CONSTRAINT fk_usersgroups_1 FOREIGN KEY (userid) REFERENCES users(userid) DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 1859 (class 0 OID 0)
-- Dependencies: 3
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2010-11-04 12:51:36 CET

--
-- PostgreSQL database dump complete
--

