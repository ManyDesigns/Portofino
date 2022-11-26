
DROP TABLE IF EXISTS comune;
CREATE TABLE comune (
    regione character varying(30) NOT NULL,
    provincia character varying(30) NOT NULL,
    comune character varying(30) NOT NULL,
    CONSTRAINT comune_pkey PRIMARY KEY (regione, provincia, comune)
);


--
-- Name: domanda; Type: TABLE; Schema: public; Owner: -; Tablespace:
--
DROP TABLE IF EXISTS public.domanda;
CREATE TABLE public.domanda (
    protocollo character varying(20) NOT NULL,
    richiedente character varying(50) NOT NULL,
    regione character varying(30) NOT NULL,
    provincia character varying(30) NOT NULL,
    comune character varying(30) NOT NULL,
    data date NOT NULL,
    CONSTRAINT domanda_pkey PRIMARY KEY (protocollo)
);


--
-- Name: table1; Type: TABLE; Schema: public; Owner: -; Tablespace:
--
DROP TABLE IF EXISTS public.table1;
CREATE TABLE public.table1 (
    id IDENTITY NOT NULL,
    testo character varying(15) NOT NULL
);


--
-- Name: table2; Type: TABLE; Schema: public; Owner: -; Tablespace:
--
DROP TABLE IF EXISTS public.table2;
CREATE TABLE public.table2 (
    id1 character varying(3),
    id2 character varying(3),
    testo character varying(15),
    primary key(id1, id2)
);


--
-- Name: table3; Type: TABLE; Schema: public; Owner: -; Tablespace:
--
DROP VIEW IF EXISTS public.test_view_1; --view depends on this table
DROP TABLE IF EXISTS public.table3;
CREATE TABLE public.table3 (
    id IDENTITY NOT NULL,
    t2_id1 character varying(3) NOT NULL,
    t2_id2 character varying(3) NOT NULL,
    tb4_id integer,
    tb1_id integer
);


--
-- Name: table4; Type: TABLE; Schema: public; Owner: -; Tablespace:
--
DROP TABLE IF EXISTS public.table4;
CREATE TABLE public.table4 (
    id IDENTITY NOT NULL,
    testo character varying(15) NOT NULL
);


--
-- Name: delibera; Type: TABLE; Schema: public; Owner: -; Tablespace:
--
DROP TABLE IF EXISTS public.delibera;
CREATE TABLE public.delibera (
    id IDENTITY NOT NULL,
    testo character varying(255) NOT NULL,
    regione character varying(30) NOT NULL,
    provincia character varying(30) NOT NULL,
    comune character varying(30) NOT NULL,
    catid varchar(10) not null
);



INSERT INTO public.comune VALUES ('liguria', 'genova', 'genova');
INSERT INTO public.comune VALUES ('liguria', 'genova', 'rapallo');
INSERT INTO public.comune VALUES ('liguria', 'genova', 'sant''olcese');
INSERT INTO public.comune VALUES ('liguria', 'savona', 'savona');
INSERT INTO public.comune VALUES ('liguria', 'savona', 'bergeggi');
INSERT INTO public.comune VALUES ('liguria', 'savona', 'spotorno');
INSERT INTO public.comune VALUES ('liguria', 'imperia', 'imperia');
INSERT INTO public.comune VALUES ('liguria', 'imperia', 'san remo');
INSERT INTO public.comune VALUES ('liguria', 'la spezia', 'la spezia');

--
-- Data for Name: delibera; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.delibera (testo, regione, provincia, comune, catid) VALUES ('delibera1','liguria', 'genova', 'genova', 'FISH');

--
-- Data for Name: domanda; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.domanda VALUES ('0001', 'Paolo Predonzani', 'liguria', 'genova', 'sant''olcese', '2010-09-27');
INSERT INTO public.domanda VALUES ('0002', 'Pippo', 'liguria', 'genova', 'rapallo', '2010-09-28');


--
-- Data for Name: table1; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.table1 VALUES (1, 'esempio');
INSERT INTO public.table1 VALUES (2, 'esempio');
INSERT INTO public.table1 VALUES (3, 'esempio');
INSERT INTO public.table1 VALUES (64, 'esempio');
INSERT INTO public.table1 VALUES (65, 'esempio');
INSERT INTO public.table1 VALUES (66, 'esempio');
INSERT INTO public.table1 VALUES (67, 'esempio');
INSERT INTO public.table1 VALUES (68, 'esempio');
INSERT INTO public.table1 VALUES (69, 'esempio');
INSERT INTO public.table1 VALUES (70, 'esempio');
INSERT INTO public.table1 VALUES (71, 'esempio');


--
-- Data for Name: table2; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.table2 VALUES ('AAA', 'AAA', 'Ciao');
INSERT INTO public.table2 VALUES ('AbA', 'ABB', 'Miao');


--
-- Data for Name: table3; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.table3 VALUES (2, 'AAA', 'AAA', 1, 2);
INSERT INTO public.table3 VALUES (1, 'AAA', 'AAA', 2, 2);


--
-- Data for Name: table4; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.table4 VALUES (1, 'A');
INSERT INTO public.table4 VALUES (2, 'B');
INSERT INTO public.table4 VALUES (3, 'C');

--
-- Name: domanda_regione_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  public.domanda
    ADD CONSTRAINT domanda_regione_fkey FOREIGN KEY (regione, provincia, comune) REFERENCES comune(regione, provincia, comune);

ALTER TABLE  public.domanda
    ADD CONSTRAINT domanda_comune_fkey FOREIGN KEY (comune) REFERENCES comune(comune);

--
-- Name: table3_t2_id1_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  public.table3
    ADD CONSTRAINT table3_t2_id1_fkey FOREIGN KEY (t2_id1, t2_id2) REFERENCES table2(id1, id2);


--
-- Name: table3_tb1_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  public.table3
    ADD CONSTRAINT table3_tb1_id_fkey FOREIGN KEY (tb1_id) REFERENCES table1(id);


--
-- Name: table3_tb4_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  public.table3
    ADD CONSTRAINT table3_tb4_id_fkey FOREIGN KEY (tb4_id) REFERENCES table4(id);


--
-- PostgreSQL database dump complete
--
DROP SEQUENCE IF EXISTS public.test_seq;
CREATE SEQUENCE test_seq;

DROP TABLE IF EXISTS public.test;
CREATE TABLE test
(
  id bigint NOT NULL PRIMARY KEY
);

DROP TABLE IF EXISTS public.test_no_pk;
CREATE TABLE test_no_pk(id bigint);

DROP VIEW IF EXISTS public.test_view_1;
CREATE VIEW public.test_view_1 AS SELECT * from public.table3;

DROP TABLE IF EXISTS public.test_spaces; --it looks like Liquibase doesn't find the pk if the table name is escaped. TODO we should test for the table name to contain spaces as well when Liquibase will support it.
CREATE TABLE public.test_spaces(
    "id spaces" bigint not null primary key,
    "some other column with spaces" character varying (123));

DROP TABLE IF EXISTS public.table_without_references;
CREATE TABLE public.table_without_references (
    id IDENTITY NOT NULL,
    text character varying(15) NOT NULL
);
