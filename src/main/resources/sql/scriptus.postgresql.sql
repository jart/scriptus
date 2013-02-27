create database scriptus owner scriptus_adm;
connect scriptus;
create schema scriptus;
alter schema scriptus owner to scriptus_adm;
create user scriptus_client;



create table scriptus.tbl_message_correlation (
	pid varchar(36) not null,
	from_id varchar(3000),
	user_id varchar(3000),
	message_id varchar(3000),
	timestamp bigint,
	version int not null,
	transport varchar (300) not null,
	primary key (pid)
);

create table scriptus.tbl_cursors (
	transport varchar(300) not null,
	cursor_data varchar(16000) not null,
	version int not null,
	primary key (transport)
);

create table scriptus.tbl_scheduled_actions (
	version int not null,
	pid varchar(36) not null,
	nonce bigint not null,
	action varchar(300) not null,
	action_time bigint not null,
	primary key (pid)
);

/*BLOB doesn't exist in posgresql*/
create table scriptus.tbl_script (
	version int not null,
	script_name varchar(300) not null,
	script_src bytea not null,
	user_id varchar(3000) not null,
	primary key (script_name, user_id)
);

create table scriptus.tbl_process (
	version int not null,
	pid varchar(36) not null,
	waiting_pid varchar(36),
	user_id varchar(3000) not null,
	source bytea not null,
	id_source varchar(300) not null,
	args varchar(3000),
	owner varchar(3000),
	state bytea,
	state_label varchar(3000),
	root boolean not null,
	alive boolean not null,
	script_state bytea,
	created bigint not null,
	lastmod bigint not null,
	primary key (pid)
);

create view scriptus.v_proclist as (
	select 
	 pid, 
	 user_id,
	 alive,
	 version, 
	 state_label, 
	 id_source,
	 length(script_state)+length(source)+length(state) size,
	 lastmod,
	 created
	from scriptus.tbl_process
);

create table scriptus.tbl_process_child (
	parent varchar(36) not null,
	child varchar(36) not null,
	seq int not null
);

create table scriptus.tbl_transport_access_tokens (
	user_id varchar(3000) not null,
	transport varchar(100) not null,
	key_id varchar(100) not null,
	access_token bytea,
	access_secret bytea,	
	version int not null
);

create table scriptus.tbl_log (
 id varchar(36) not null,
 user_id varchar(3000) not null,
 pid varchar(36) not null,
 created bigint not null,
 message text not null
);



grant usage on schema scriptus to scriptus_client;
grant select, insert, update, delete on scriptus.tbl_message_correlation to scriptus_client;
grant select, insert, update, delete on scriptus.tbl_cursors to scriptus_client;
grant select, insert, update, delete on scriptus.tbl_scheduled_actions to scriptus_client;
grant select, insert, update, delete on scriptus.tbl_script to scriptus_client;
grant select, insert, update, delete on scriptus.tbl_process to scriptus_client;
grant select, insert, update, delete on scriptus.tbl_process_child to scriptus_client;
grant select, insert, update, delete on scriptus.v_proclist to scriptus_client;
grant select, insert, update, delete on scriptus.tbl_transport_access_tokens to scriptus_client;
grant select, insert, update, delete on scriptus.tbl_log to scriptus_client;

