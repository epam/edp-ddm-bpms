--liquibase formatted sql

--changeset author:insert process-instances runAlways:true
DELETE FROM act_hi_procinst;
INSERT INTO act_hi_procinst
(   id_,   proc_inst_id_,   proc_def_id_,           start_time_,   state_) VALUES
( 'id1',  'proc_inst_id1', 'proc_def_id', '2023-03-02 18:29:11', 'ACTIVE'),
( 'id2',  'proc_inst_id2', 'proc_def_id', '2023-03-02 18:29:13', 'SUSPENDED'),
( 'id3',  'proc_inst_id3', 'proc_def_id', '2023-03-02 18:29:14', 'INTERNALLY_TERMINATED'),
( 'id4',  'proc_inst_id4', 'proc_def_id', '2023-03-02 18:29:15', 'COMPLETED'),
( 'id5',  'proc_inst_id5', 'proc_def_id', '2023-03-02 18:29:17', 'EXTERNALLY_TERMINATED'),
( 'id6',  'proc_inst_id6', 'proc_def_id', '2023-03-02 18:29:18', 'COMPLETED'),
( 'id7',  'proc_inst_id7', 'proc_def_id', '2023-03-02 18:29:27', 'EXTERNALLY_TERMINATED'),
( 'id8',  'proc_inst_id8', 'proc_def_id', '2023-03-02 18:29:37', 'COMPLETED'),
( 'id9',  'proc_inst_id9', 'proc_def_id', '2023-03-02 18:29:53', 'SUSPENDED');