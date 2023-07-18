DROP TABLE IF EXISTS REQUESTS;
DROP TABLE IF EXISTS COMPILATION_EVENTS;
DROP TABLE IF EXISTS EVENTS;
DROP TABLE IF EXISTS COMPILATIONS;
DROP TABLE IF EXISTS LOCATIONS;
DROP TABLE IF EXISTS CATEGORIES;
DROP TABLE IF EXISTS USERS;

CREATE TABLE IF NOT EXISTS USERS (
    USER_ID   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    USER_NAME VARCHAR(250) NOT NULL,
    EMAIL     VARCHAR(254) NOT NULL,
    CONSTRAINT PK_USER PRIMARY KEY (USER_ID),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (EMAIL)
);

CREATE TABLE IF NOT EXISTS CATEGORIES (
    CATEGORY_ID   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    CATEGORY_NAME VARCHAR(50) NOT NULL,
    CONSTRAINT PK_CATEGORY PRIMARY KEY (CATEGORY_ID),
    CONSTRAINT UQ_CATEGORY_NAME UNIQUE (CATEGORY_NAME)
);

CREATE TABLE IF NOT EXISTS LOCATIONS (
    LOCATE_ID BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    LAT       FLOAT NOT NULL,
    LON       FLOAT NOT NULL,
    CONSTRAINT PK_LOCATE PRIMARY KEY (LOCATE_ID)
);

CREATE TABLE IF NOT EXISTS COMPILATIONS (
    COMPILATION_ID BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    PINNED         BOOLEAN DEFAULT FALSE,
    TITLE          VARCHAR(50) NOT NULL,
    CONSTRAINT PK_COMPILATION PRIMARY KEY (COMPILATION_ID),
    CONSTRAINT UQ_COMPILATION_TITLE UNIQUE (TITLE)
);

CREATE TABLE IF NOT EXISTS EVENTS (
    EVENT_ID           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    ANNOTATION         VARCHAR(2000) NOT NULL,
    CATEGORY_ID        BIGINT NOT NULL,
    CREATED_ON         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    DESCRIPTION        VARCHAR(7000) NOT NULL,
    EVENT_DATE         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    INITIATOR_ID       BIGINT NOT NULL,
    LOCATE_ID          BIGINT NOT NULL,
    PAID               BOOLEAN DEFAULT FALSE,
    PARTICIPANT_LIMIT  INT DEFAULT 0,
    PUBLISHED_ON       TIMESTAMP WITHOUT TIME ZONE,
    REQUEST_MODERATION BOOLEAN DEFAULT TRUE,
    EVENT_STATE        VARCHAR(20) NOT NULL,
    TITLE              VARCHAR(120) NOT NULL,
    CONSTRAINT PK_EVENT PRIMARY KEY (EVENT_ID),
    CONSTRAINT FK_EVENTS_CATEGORY FOREIGN KEY (CATEGORY_ID)
        REFERENCES CATEGORIES (CATEGORY_ID),
    CONSTRAINT FK_EVENTS_USER FOREIGN KEY (INITIATOR_ID)
        REFERENCES USERS (USER_ID),
    CONSTRAINT FK_EVENTS_LOCATE FOREIGN KEY (LOCATE_ID)
        REFERENCES LOCATIONS (LOCATE_ID)
);

CREATE TABLE IF NOT EXISTS COMPILATION_EVENTS (
    COMPILATION_ID BIGINT NOT NULL,
    EVENT_ID       BIGINT NOT NULL,
    CONSTRAINT PK_COMPILATION_EVENT PRIMARY KEY (COMPILATION_ID, EVENT_ID),
    CONSTRAINT FK_COMPILATION_EVENTS_COMPILATION FOREIGN KEY (COMPILATION_ID)
        REFERENCES COMPILATIONS (COMPILATION_ID) ON DELETE CASCADE,
    CONSTRAINT FK_COMPILATION_EVENTS_EVENT FOREIGN KEY (EVENT_ID)
        REFERENCES EVENTS (EVENT_ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS REQUESTS (
    REQUEST_ID   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    CREATED      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    EVENT_ID     BIGINT NOT NULL,
    REQUESTER_ID BIGINT NOT NULL,
    STATUS       VARCHAR(20) NOT NULL,
    CONSTRAINT PK_REQUEST PRIMARY KEY (REQUEST_ID),
    CONSTRAINT FK_REQUESTS_EVENT FOREIGN KEY (EVENT_ID)
        REFERENCES EVENTS (EVENT_ID) ON DELETE CASCADE,
    CONSTRAINT FK_REQUESTS_USER FOREIGN KEY (REQUESTER_ID)
        REFERENCES USERS (USER_ID) ON DELETE CASCADE
);
