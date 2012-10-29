create schema scriptus;

create table scriptus.tbl_message_correlation (
	pid varchar(36) not null,
	user_id varchar(3000),
	message_id varchar(3000),
	timestamp bigint,
	version int not null
);

create table scriptus.tbl_cursors (
	transport varchar(300) not null,
	cursor_data varchar(16000) not null,
	version int not null
);

create table scriptus.tbl_scheduled_actions (
	version int not null,
	pid varchar(36) not null,
	nonce bigint not null,
	action varchar(300) not null,
	when bigint not null
);

create table scriptus.tbl_script (
	version int not null,
	script_name varchar(300) not null,
	script_src blob not null,
	user_id varchar(3000) not null
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
	script_state blob,
	created bigint not null,
	lastmod bigint not null
);

create view scriptus.v_proclist as (
	select 
		pid, 
		user_id,
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
