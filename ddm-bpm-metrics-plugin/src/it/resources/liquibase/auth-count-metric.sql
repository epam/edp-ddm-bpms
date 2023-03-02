--liquibase formatted sql

--changeset author:insert authorizations runAlways:true
DELETE FROM act_ru_authorization;
INSERT INTO act_ru_authorization VALUES
-- id_, rev_, type_,       group_id_,   user_id_, resource_type_, resource_id_,     perms_, removal_time_, root_proc_inst_id_
('id1',    1,     1, 'camunda-admin',       NULL,              0,          '*', 2147483647,          NULL,               NULL),
('id2',    2,     2,            NULL, 'user_id2',              1,  'resource2',          1,          NULL,               NULL),
('id3',    3,     3,       'officer',       NULL,              2,  'resource3',          2,          NULL,               NULL),
('id4',    4,     4,            NULL, 'user_id4',              3,  'resource4',          3,          NULL,               NULL);