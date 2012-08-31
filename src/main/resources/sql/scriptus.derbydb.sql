create table tbl_message_correlation (
	pid varchar(36) not null,
	user_id varchar(3000),
	message_id varchar(3000),
	timestamp bigint,
	version int not null
);

create table tbl_cursors (
	transport varchar(300) not null,
	cursor_data varchar(16000) not null,
	version int not null
);

create table tbl_scheduled_actions (
	version int not null,
	pid varchar(36) not null,
	nonce bigint not null,
	action varchar(300) not null 
);

create table tbl_script (
	version int not null,
	script_name varchar(300) not null,
	script_src blob not null,
	user_id varchar(3000) not null
);

create table tbl_process (
	version int not null,
	pid varchar(36) not null,
	waiting_pid varchar(36) not null,
	user_id varchar(3000) not null,
	source blob not null,
	id_source varchar(300) not null,
	args varchar(3000),
	owner varchar(3000),
	state blob,
	compiled blob,
	root boolean not null,
	continuation blob,
	global_scope blob
)