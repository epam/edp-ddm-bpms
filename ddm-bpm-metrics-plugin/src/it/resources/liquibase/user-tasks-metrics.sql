--liquibase formatted sql

--changeset author:insert tasks runAlways:true
DELETE FROM act_ru_task;
INSERT INTO act_ru_task
(   id_,   assignee_, suspension_state_) VALUES
( 'id1', 'assignee1',                 1),
( 'id2',        NULL,                 1),
( 'id3', 'assignee3',                 1),
( 'id4',        NULL,                 1),
( 'id5', 'assignee5',                 1),
( 'id6',        NULL,                 1),
( 'id7', 'assignee7',                 1),
( 'id8',        NULL,                 1),
( 'id9', 'assignee9',                 1),
('id10',        NULL,                 2);