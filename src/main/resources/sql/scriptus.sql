create schema scriptus;

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
	script_src blob not null,
	user_id varchar(3000) not null,
	primary key (script_name, user_id)
);

create table scriptus.tbl_process (
	version int not null,
	pid varchar(36) not null,
	waiting_pid varchar(36),
	user_id varchar(3000) not null,
	source blob not null,
	id_source varchar(300) not null,
	args varchar(3000),
	owner varchar(3000),
	state blob,
	state_label varchar(3000),
	root boolean not null,
	alive boolean not null,
	transport varchar(100) not null,
	script_state blob,
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
	access_token blob,
	access_secret blob,	
	version int not null
);

create table scriptus.tbl_log (
 id varchar(36) not null,
	user_id varchar(3000) not null,
	pid varchar(36) not null,
	created bigint not null,
	message clob not null
);

create table scriptus.tbl_personal_msg (
 id varchar(36) not null,
 parent varchar(36),
 message clob not null,
 msg_from varchar(300),
 userId varchar(3000) not null,
 created long not null
);
