--liquibase formatted sql

--changeset author:insert incidents runAlways:true
DELETE FROM act_ru_job;
INSERT INTO act_ru_job
(  id_,     type_, retries_) VALUES
('id1', 'message',        1),
('id2',   'timer',        0),
('id3',   'timer',        1),
('id4', 'message',        1),
('id5', 'message',        1);
