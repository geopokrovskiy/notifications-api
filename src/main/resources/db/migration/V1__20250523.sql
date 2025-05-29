-- This is a migration file for creation of schemas and tables

CREATE TABLE IF NOT EXISTS notifications
(
    id                        serial    primary key,
    created_at                timestamp default now() not null,
    modified_at               timestamp,
    message                   text                    not null,
    user_uid                  varchar(36),
    notification_status       varchar(32),
    subject                   varchar(128),
    notification_type         varchar(16),
    notification_dest         varchar(128)   	      not null,
    created_by                varchar(255)
    );



