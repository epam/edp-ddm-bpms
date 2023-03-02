--liquibase formatted sql

--changeset author:insert metrics runAlways:true
DELETE FROM act_ru_meter_log;
INSERT INTO act_ru_meter_log
(  id_,                                       name_, value_) VALUES
('id1', 'history-cleanup-removed-process-instances',    578),
('id2', 'history-cleanup-removed-process-instances',    124),
('id3',      'history-cleanup-removed-task-metrics',   2576),
('id4',      'history-cleanup-removed-task-metrics',    216),
('id5',                            'garbage-metric',  12574);