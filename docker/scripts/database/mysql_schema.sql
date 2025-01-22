CREATE DATABASE IF NOT EXISTS propertymanagement;
USE propertymanagement;

create table association_meeting (meeting_id bigint not null auto_increment, approval_date_time varchar(255), approver_id bigint, scheduled_date varchar(255), scheduled_time varchar(255), tracker_id varchar(255), community_id bigint not null, primary key (meeting_id));
create table community (community_id bigint not null auto_increment, address varchar(255), cif varchar(255), president_id bigint, vice_president_id bigint, primary key (community_id));
create table meeting_participant (participant_id bigint not null, participant_role varchar(255), meeting_id bigint not null, primary key (participant_id, meeting_id));
create table meeting_tracker (id bigint not null auto_increment, community_id bigint not null, date varchar(255) not null, time varchar(255) not null, tracker_id varchar(255) not null, meeting_id bigint, primary key (id));
create table neighbour (neighbourg_id bigint not null auto_increment, email varchar(255), fullname varchar(255), phonenumber varchar(255), president bit, vicepresident bit, community_id bigint not null, primary key (neighbourg_id));
alter table association_meeting add constraint UK27elur84xtt7cu1xj1xk5dqey unique (tracker_id);
alter table meeting_tracker add constraint UKp24e4nfuch8yfcpmnvey6w99x unique (tracker_id);
alter table meeting_tracker add constraint UKd2qbju921bl13nix7g8scyiiy unique (meeting_id);
alter table association_meeting add constraint FKp6nqy19r3x1shivbhgx3buxya foreign key (community_id) references community (community_id);
alter table meeting_participant add constraint FKfqt5419bxkyknj7byq8jt3nan foreign key (meeting_id) references association_meeting (meeting_id);
alter table meeting_tracker add constraint FKcldr3ncw21qf1qp4bati8kr3e foreign key (meeting_id) references association_meeting (meeting_id);
alter table neighbour add constraint FKsh7f0kjq2tbn0ywfv10ad3gti foreign key (community_id) references community (community_id);
