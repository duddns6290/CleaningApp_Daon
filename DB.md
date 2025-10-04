create table users (
    user_id varchar(30) primary key,
    signup_type enum('OAUTH','NORMAL') NOT NULL,
    oauth_provider enum('GOOGLE','KAKAO','NAVER'),
    email varchar(255) unique,
    name varchar(30) not null,
    address varchar(100),
    phone varchar(30) unique,
    created_at timestamp default current_timestamp
    );
