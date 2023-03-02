--liquibase formatted sql

--changeset author:insert subscriptions runAlways:true
DELETE FROM act_ru_event_subscr;
INSERT INTO act_ru_event_subscr
(  id_,   event_type_,              created_) VALUES
( 'id1',      'signal', '2023-03-02 18:29:11'),
( 'id2', 'conditional', '2023-03-02 18:29:22'),
( 'id3', 'conditional', '2023-03-02 18:29:33'),
( 'id4',  'compensate', '2023-03-02 18:29:44'),
( 'id5',  'compensate', '2023-03-02 18:29:55'),
( 'id6',  'compensate', '2023-03-02 18:30:06'),
( 'id7',     'message', '2023-03-02 18:30:17'),
( 'id8',     'message', '2023-03-02 18:30:28'),
( 'id9',     'message', '2023-03-02 18:30:39'),
('id10',     'message', '2023-03-02 18:30:50');
