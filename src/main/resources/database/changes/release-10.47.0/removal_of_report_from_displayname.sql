SET schema 'casesvc';


UPDATE reporttype
SET displayname = 'Case Events' WHERE reporttypepk = 'CASE_EVENTS';


UPDATE reporttype
SET displayname = 'Response Chasing' WHERE reporttypepk = 'RESPONSE_CHASING';


CREATE SEQUENCE casenotificationseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;


CREATE TABLE casenotification (
    casenotificationPK bigint NOT NULL,
    caseid uuid NOT NULL,
    actionplanid uuid NOT NULL,
    notificationtype character varying(20) NOT NULL
);


ALTER TABLE ONLY casenotification
    ADD CONSTRAINT casenotification_pkey PRIMARY KEY (casenotificationPK);
