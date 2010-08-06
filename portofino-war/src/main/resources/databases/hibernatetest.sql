--
-- PostgreSQL database dump
--

-- Started on 2010-08-06 15:29:16 CEST

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 307 (class 2612 OID 16386)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: -
--

CREATE PROCEDURAL LANGUAGE plpgsql;


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1494 (class 1259 OID 384479)
-- Dependencies: 3
-- Name: table1; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE table1 (
    id integer NOT NULL,
    testo character varying(15) NOT NULL
);


--
-- TOC entry 1495 (class 1259 OID 384482)
-- Dependencies: 3 1494
-- Name: table1_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE table1_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 1787 (class 0 OID 0)
-- Dependencies: 1495
-- Name: table1_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE table1_id_seq OWNED BY table1.id;


--
-- TOC entry 1788 (class 0 OID 0)
-- Dependencies: 1495
-- Name: table1_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('table1_id_seq', 4, true);


--
-- TOC entry 1496 (class 1259 OID 384490)
-- Dependencies: 3
-- Name: table2; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE table2 (
    id1 character varying(3) NOT NULL,
    id2 character varying(3) NOT NULL,
    testo character varying(15),
    id_tb1 integer NOT NULL
);


--
-- TOC entry 1774 (class 2604 OID 384484)
-- Dependencies: 1495 1494
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE table1 ALTER COLUMN id SET DEFAULT nextval('table1_id_seq'::regclass);


--
-- TOC entry 1780 (class 0 OID 384479)
-- Dependencies: 1494
-- Data for Name: table1; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO table1 (id, testo) VALUES (1, 'esempio');
INSERT INTO table1 (id, testo) VALUES (3, 'esempio');
INSERT INTO table1 (id, testo) VALUES (4, 'esempio');


--
-- TOC entry 1781 (class 0 OID 384490)
-- Dependencies: 1496
-- Data for Name: table2; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 1776 (class 2606 OID 384489)
-- Dependencies: 1494 1494
-- Name: table1_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY table1
    ADD CONSTRAINT table1_pkey PRIMARY KEY (id);


--
-- TOC entry 1778 (class 2606 OID 384494)
-- Dependencies: 1496 1496 1496
-- Name: table2_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY table2
    ADD CONSTRAINT table2_pkey PRIMARY KEY (id1, id2);


--
-- TOC entry 1779 (class 2606 OID 384495)
-- Dependencies: 1775 1496 1494
-- Name: tb_1_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY table2
    ADD CONSTRAINT tb_1_fk FOREIGN KEY (id_tb1) REFERENCES table1(id);


--
-- TOC entry 1786 (class 0 OID 0)
-- Dependencies: 3
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2010-08-06 15:29:17 CEST

--
-- PostgreSQL database dump complete
--

