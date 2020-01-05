create table if not exists tbl_todos (
    name varchar(32) not null,
    completed boolean not null default false,
    date_created timestamp not null default current_timestamp ,
    unique (name)
)
