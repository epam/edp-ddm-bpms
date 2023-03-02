--liquibase formatted sql

--changeset author:insert process definitions runAlways:true
DELETE FROM act_re_procdef;
INSERT INTO act_re_procdef
(  id_,          key_, version_, suspension_state_, startable_) VALUES
('id1', 'procdefkey1',        1,                 1,       TRUE),
('id2', 'procdefkey2',        2,                 1,       TRUE),
('id3', 'procdefkey3',        3,                 2,       TRUE),
('id4', 'procdefkey4',        4,                 1,      FALSE),
('id5', 'procdefkey5',        5,                 1,      FALSE);