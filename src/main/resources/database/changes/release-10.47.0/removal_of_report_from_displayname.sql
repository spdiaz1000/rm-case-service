SET schema 'casesvc';


UPDATE reporttype
SET displayname = 'Case Events' WHERE reporttypepk = 'CASE_EVENTS';


UPDATE reporttype
SET displayname = 'Response Chasing' WHERE reporttypepk = 'RESPONSE_CHASING';
