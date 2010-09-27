--
-- PostgreSQL database dump
--

-- Started on 2010-09-27 09:14:37 CEST

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 313 (class 2612 OID 16386)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: -
--

CREATE PROCEDURAL LANGUAGE plpgsql;


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1500 (class 1259 OID 384479)
-- Dependencies: 3
-- Name: table1; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE table1 (
    id integer NOT NULL,
    testo character varying(15) NOT NULL
);


--
-- TOC entry 1501 (class 1259 OID 384482)
-- Dependencies: 1500 3
-- Name: table1_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE table1_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1807 (class 0 OID 0)
-- Dependencies: 1501
-- Name: table1_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE table1_id_seq OWNED BY table1.id;


--
-- TOC entry 1808 (class 0 OID 0)
-- Dependencies: 1501
-- Name: table1_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('table1_id_seq', 71, true);


--
-- TOC entry 1502 (class 1259 OID 384490)
-- Dependencies: 3
-- Name: table2; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE table2 (
    id1 character varying(3) NOT NULL,
    id2 character varying(3) NOT NULL,
    testo character varying(15)
);


--
-- TOC entry 1504 (class 1259 OID 388867)
-- Dependencies: 3
-- Name: table3; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE table3 (
    id integer NOT NULL,
    t2_id1 character varying(3) NOT NULL,
    t2_id2 character varying(3) NOT NULL,
    tb4_id integer,
    tb1_id integer
);


--
-- TOC entry 1503 (class 1259 OID 388865)
-- Dependencies: 1504 3
-- Name: table3_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE table3_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1809 (class 0 OID 0)
-- Dependencies: 1503
-- Name: table3_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE table3_id_seq OWNED BY table3.id;


--
-- TOC entry 1810 (class 0 OID 0)
-- Dependencies: 1503
-- Name: table3_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('table3_id_seq', 2, true);


--
-- TOC entry 1506 (class 1259 OID 388886)
-- Dependencies: 3
-- Name: table4; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE table4 (
    id integer NOT NULL,
    testo character varying(15) NOT NULL
);


--
-- TOC entry 1505 (class 1259 OID 388884)
-- Dependencies: 1506 3
-- Name: table4_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE table4_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1811 (class 0 OID 0)
-- Dependencies: 1505
-- Name: table4_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE table4_id_seq OWNED BY table4.id;


--
-- TOC entry 1812 (class 0 OID 0)
-- Dependencies: 1505
-- Name: table4_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('table4_id_seq', 3, true);


--
-- TOC entry 1784 (class 2604 OID 384484)
-- Dependencies: 1501 1500
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE table1 ALTER COLUMN id SET DEFAULT nextval('table1_id_seq'::regclass);


--
-- TOC entry 1785 (class 2604 OID 388870)
-- Dependencies: 1504 1503 1504
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE table3 ALTER COLUMN id SET DEFAULT nextval('table3_id_seq'::regclass);


--
-- TOC entry 1786 (class 2604 OID 388889)
-- Dependencies: 1506 1505 1506
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE table4 ALTER COLUMN id SET DEFAULT nextval('table4_id_seq'::regclass);


--
-- TOC entry 1798 (class 0 OID 384479)
-- Dependencies: 1500
-- Data for Name: table1; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO table1 (id, testo) VALUES (1, 'esempio');
INSERT INTO table1 (id, testo) VALUES (2, 'esempio');
INSERT INTO table1 (id, testo) VALUES (3, 'esempio');
INSERT INTO table1 (id, testo) VALUES (64, 'esempio');
INSERT INTO table1 (id, testo) VALUES (65, 'esempio');
INSERT INTO table1 (id, testo) VALUES (66, 'esempio');
INSERT INTO table1 (id, testo) VALUES (67, 'esempio');
INSERT INTO table1 (id, testo) VALUES (68, 'esempio');
INSERT INTO table1 (id, testo) VALUES (69, 'esempio');
INSERT INTO table1 (id, testo) VALUES (70, 'esempio');
INSERT INTO table1 (id, testo) VALUES (71, 'esempio');


--
-- TOC entry 1799 (class 0 OID 384490)
-- Dependencies: 1502
-- Data for Name: table2; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO table2 (id1, id2, testo) VALUES ('AAA', 'AAA', 'Ciao');
INSERT INTO table2 (id1, id2, testo) VALUES ('AbA', 'ABB', 'Miao');


--
-- TOC entry 1800 (class 0 OID 388867)
-- Dependencies: 1504
-- Data for Name: table3; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO table3 (id, t2_id1, t2_id2, tb4_id, tb1_id) VALUES (2, 'AAA', 'AAA', 1, 2);
INSERT INTO table3 (id, t2_id1, t2_id2, tb4_id, tb1_id) VALUES (1, 'AAA', 'AAA', 2, 2);


--
-- TOC entry 1801 (class 0 OID 388886)
-- Dependencies: 1506
-- Data for Name: table4; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO table4 (id, testo) VALUES (1, 'A');
INSERT INTO table4 (id, testo) VALUES (2, 'B');
INSERT INTO table4 (id, testo) VALUES (3, 'C');


--
-- TOC entry 1788 (class 2606 OID 384489)
-- Dependencies: 1500 1500
-- Name: table1_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY table1
    ADD CONSTRAINT table1_pkey PRIMARY KEY (id);


--
-- TOC entry 1790 (class 2606 OID 384494)
-- Dependencies: 1502 1502 1502
-- Name: table2_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY table2
    ADD CONSTRAINT table2_pkey PRIMARY KEY (id1, id2);


--
-- TOC entry 1792 (class 2606 OID 388872)
-- Dependencies: 1504 1504
-- Name: table3_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY table3
    ADD CONSTRAINT table3_pkey PRIMARY KEY (id);


--
-- TOC entry 1794 (class 2606 OID 388891)
-- Dependencies: 1506 1506
-- Name: table4_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY table4
    ADD CONSTRAINT table4_pkey PRIMARY KEY (id);


--
-- TOC entry 1795 (class 2606 OID 388873)
-- Dependencies: 1502 1504 1504 1789 1502
-- Name: table3_t2_id1_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY table3
    ADD CONSTRAINT table3_t2_id1_fkey FOREIGN KEY (t2_id1, t2_id2) REFERENCES table2(id1, id2);


--
-- TOC entry 1796 (class 2606 OID 388892)
-- Dependencies: 1787 1504 1500
-- Name: table3_tb1_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY table3
    ADD CONSTRAINT table3_tb1_id_fkey FOREIGN KEY (tb1_id) REFERENCES table1(id);


--
-- TOC entry 1797 (class 2606 OID 388897)
-- Dependencies: 1506 1504 1793
-- Name: table3_tb4_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY table3
    ADD CONSTRAINT table3_tb4_id_fkey FOREIGN KEY (tb4_id) REFERENCES table4(id);


