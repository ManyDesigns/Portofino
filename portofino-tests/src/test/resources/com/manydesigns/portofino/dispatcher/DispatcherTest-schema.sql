DROP TABLE IF EXISTS "projects";
CREATE TABLE "projects" (
    "id" integer PRIMARY KEY,
    "name" character varying(30) NOT NULL,
    "description" character varying(30) NOT NULL
);

DROP TABLE IF EXISTS "tickets";
CREATE TABLE "tickets" (
    "id" integer PRIMARY KEY,
    "subject" character varying(30) NOT NULL,
    "description" character varying(30) NOT NULL,
    "project_id" integer NOT NULL REFERENCES "projects"("id")
);