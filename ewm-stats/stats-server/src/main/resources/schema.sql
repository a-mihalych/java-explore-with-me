DROP TABLE IF EXISTS HITS;

CREATE TABLE IF NOT EXISTS HITS (
    HIT_ID    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    APP       VARCHAR(64) NOT NULL,
    URI       VARCHAR(256) NOT NULL,
    IP        VARCHAR(20) NOT NULL,
    TIMESTAMP TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT PK_HITS PRIMARY KEY (HIT_ID)
);
