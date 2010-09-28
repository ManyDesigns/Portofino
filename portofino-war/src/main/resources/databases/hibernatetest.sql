--
-- PostgreSQL database dump
--

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: -
--

CREATE PROCEDURAL LANGUAGE plpgsql;


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: comune; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE comune (
    regione character varying(30) NOT NULL,
    provincia character varying(30) NOT NULL,
    comune character varying(30) NOT NULL
);


--
-- Name: domanda; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE domanda (
    protocollo character varying(20) NOT NULL,
    richiedente character varying(50) NOT NULL,
    regione character varying(30) NOT NULL,
    provincia character varying(30) NOT NULL,
    comune character varying(30) NOT NULL,
    data date NOT NULL
);


--
-- Name: table1; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE table1 (
    id integer NOT NULL,
    testo character varying(15) NOT NULL
);


--
-- Name: table2; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE table2 (
    id1 character varying(3) NOT NULL,
    id2 character varying(3) NOT NULL,
    testo character varying(15)
);


--
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
-- Name: table4; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE table4 (
    id integer NOT NULL,
    testo character varying(15) NOT NULL
);


--
-- Name: table1_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE table1_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: table1_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE table1_id_seq OWNED BY table1.id;


--
-- Name: table1_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('table1_id_seq', 71, true);


--
-- Name: table3_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE table3_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: table3_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE table3_id_seq OWNED BY table3.id;


--
-- Name: table3_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('table3_id_seq', 2, true);


--
-- Name: table4_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE table4_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: table4_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE table4_id_seq OWNED BY table4.id;


--
-- Name: table4_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('table4_id_seq', 3, true);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE table1 ALTER COLUMN id SET DEFAULT nextval('table1_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE table3 ALTER COLUMN id SET DEFAULT nextval('table3_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE table4 ALTER COLUMN id SET DEFAULT nextval('table4_id_seq'::regclass);


--
-- Data for Name: comune; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO comune VALUES ('liguria', 'genova', 'genova');
INSERT INTO comune VALUES ('liguria', 'genova', 'rapallo');
INSERT INTO comune VALUES ('liguria', 'genova', 'sant''olcese');
INSERT INTO comune VALUES ('liguria', 'savona', 'savona');
INSERT INTO comune VALUES ('liguria', 'savona', 'bergeggi');
INSERT INTO comune VALUES ('liguria', 'savona', 'spotorno');
INSERT INTO comune VALUES ('liguria', 'imperia', 'imperia');
INSERT INTO comune VALUES ('liguria', 'imperia', 'san remo');
INSERT INTO comune VALUES ('liguria', 'la spezia', 'la spezia');


--
-- Data for Name: domanda; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO domanda VALUES ('0001', 'Paolo Predonzani', 'liguria', 'genova', 'sant''olcese', '2010-09-27');
INSERT INTO domanda VALUES ('0002', 'Pippo', 'liguria', 'genova', 'rapallo', '2010-09-28');


--
-- Data for Name: table1; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO table1 VALUES (1, 'esempio');
INSERT INTO table1 VALUES (2, 'esempio');
INSERT INTO table1 VALUES (3, 'esempio');
INSERT INTO table1 VALUES (64, 'esempio');
INSERT INTO table1 VALUES (65, 'esempio');
INSERT INTO table1 VALUES (66, 'esempio');
INSERT INTO table1 VALUES (67, 'esempio');
INSERT INTO table1 VALUES (68, 'esempio');
INSERT INTO table1 VALUES (69, 'esempio');
INSERT INTO table1 VALUES (70, 'esempio');
INSERT INTO table1 VALUES (71, 'esempio');


--
-- Data for Name: table2; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO table2 VALUES ('AAA', 'AAA', 'Ciao');
INSERT INTO table2 VALUES ('AbA', 'ABB', 'Miao');


--
-- Data for Name: table3; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO table3 VALUES (2, 'AAA', 'AAA', 1, 2);
INSERT INTO table3 VALUES (1, 'AAA', 'AAA', 2, 2);


--
-- Data for Name: table4; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO table4 VALUES (1, 'A');
INSERT INTO table4 VALUES (2, 'B');
INSERT INTO table4 VALUES (3, 'C');


--
-- Name: comune_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY comune
    ADD CONSTRAINT comune_pkey PRIMARY KEY (regione, provincia, comune);


--
-- Name: domanda_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY domanda
    ADD CONSTRAINT domanda_pkey PRIMARY KEY (protocollo);


--
-- Name: table1_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY table1
    ADD CONSTRAINT table1_pkey PRIMARY KEY (id);


--
-- Name: table2_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY table2
    ADD CONSTRAINT table2_pkey PRIMARY KEY (id1, id2);


--
-- Name: table3_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY table3
    ADD CONSTRAINT table3_pkey PRIMARY KEY (id);


--
-- Name: table4_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY table4
    ADD CONSTRAINT table4_pkey PRIMARY KEY (id);


--
-- Name: domanda_regione_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY domanda
    ADD CONSTRAINT domanda_regione_fkey FOREIGN KEY (regione, provincia, comune) REFERENCES comune(regione, provincia, comune);


--
-- Name: table3_t2_id1_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY table3
    ADD CONSTRAINT table3_t2_id1_fkey FOREIGN KEY (t2_id1, t2_id2) REFERENCES table2(id1, id2);


--
-- Name: table3_tb1_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY table3
    ADD CONSTRAINT table3_tb1_id_fkey FOREIGN KEY (tb1_id) REFERENCES table1(id);


--
-- Name: table3_tb4_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY table3
    ADD CONSTRAINT table3_tb4_id_fkey FOREIGN KEY (tb4_id) REFERENCES table4(id);


--
-- PostgreSQL database dump complete
--

