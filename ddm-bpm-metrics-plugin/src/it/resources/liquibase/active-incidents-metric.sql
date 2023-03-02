--liquibase formatted sql

--changeset author:insert incidents runAlways:true
DELETE FROM act_ru_incident;
INSERT INTO act_ru_incident
(  id_, rev_,   incident_timestamp_,   incident_type_) VALUES
('id1',    1, '2023-03-02 18:29:11', 'incident_type1'),
('id2',    2, '2023-03-02 18:29:22', 'incident_type2'),
('id3',    3, '2023-03-02 18:29:33', 'incident_type3'),
('id4',    4, '2023-03-02 18:29:44', 'incident_type4'),
('id5',    5, '2023-03-02 18:29:55', 'incident_type5'),
('id6',    6, '2023-03-02 18:30:06', 'incident_type6');
