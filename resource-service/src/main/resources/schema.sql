create table PERSON(
    ID int not null AUTO_INCREMENT,
    FIRSTNAME varchar(50) not null,
    LASTNAME varchar(50) not null,
    PRIMARY KEY ( ID )
);

create table BOOK(
    ID int not null AUTO_INCREMENT,
    TITLE varchar(100) not null,
    AUTHOR varchar(50) not null,
    PUBLICATION_YEAR int not null,
    PRIMARY KEY ( ID )
);