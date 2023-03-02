--liquibase formatted sql

--changeset author:insert deployments runAlways:true
DELETE FROM act_re_deployment;
INSERT INTO act_re_deployment VALUES
-- id_,         name_,  deploy_time_, source_, tenant_id_
('id1', 'deployment1',          NULL,    NULL,       NULL),
('id2', 'deployment2',          NULL,    NULL,       NULL),
('id3', 'deployment3',          NULL,    NULL,       NULL),
('id4', 'deployment4',          NULL,    NULL,       NULL),
('id5', 'deployment5',          NULL,    NULL,       NULL);