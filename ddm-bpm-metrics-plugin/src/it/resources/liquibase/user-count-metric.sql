--liquibase formatted sql

--changeset author:insert users
DELETE FROM act_id_user;
INSERT INTO act_id_user VALUES
-- id_, rev_,  first_,       last_,                        email_,    pwd_,   salt_, lock_exp_time_, attempts_, picture_id_
('id1',    1, 'user1', 'lastname1', 'user1_lastname1@camunda.com', 'pass1', 'salt1',           NULL,      NULL,        NULL),
('id2',    2, 'user2', 'lastname2', 'user2_lastname2@camunda.com', 'pass2', 'salt2',           NULL,      NULL,        NULL),
('id3',    3, 'user3', 'lastname3', 'user3_lastname3@camunda.com', 'pass3', 'salt3',           NULL,      NULL,        NULL);