alter table appointments add column fullname varchar(255) not null default '';
alter table appointments add column email varchar(255) not null default '';

alter table appointments alter column fullname drop default;
alter table appointments alter column email drop default;
