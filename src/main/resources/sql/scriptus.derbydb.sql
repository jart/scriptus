
create table tbl_message_correlation (
	pid varchar not null,
	user varchar null,
	message_id varchar null,
	timestamp long null,
	version int not null
);

create table tbl_cursors (
	transport varchar not null,
	cursor varchar not null,
	version int not null
)

create table tbl_scheduled_actions (
	version int not null,
	pid varchar not null,
	nonce long not null,
	action varchar not null 
);

create table tbl_script (
	version int not null,
	script_name varchar not null,
	script_src varchar not null,
	user_id varchar not null
);

create table tbl_process (
	version int not null,
	pid varchar not null,
	waiting_pid varchar not null,
	user_id varchar not null,
	source varchar not null,
	id_source varchar not null,
	args varchar null,
	owner varchar null,
	state blob null,
	compiled blob null,
	root boolean not null,
	continuation blob null,
	global_scope blob null
)