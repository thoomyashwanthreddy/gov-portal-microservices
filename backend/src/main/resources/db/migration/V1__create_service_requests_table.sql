CREATE TABLE service_requests (
    id                       VARCHAR(36)   PRIMARY KEY,
    citizen_sub              VARCHAR(255)  NOT NULL,
    citizen_username         VARCHAR(255)  NOT NULL,
    category                 VARCHAR(255)  NOT NULL,
    description              VARCHAR(2000) NOT NULL,
    location                 VARCHAR(500),
    status                   VARCHAR(50)   NOT NULL,
    created_at               TIMESTAMP     NOT NULL,
    updated_at                TIMESTAMP    NOT NULL,
    last_updated_by_username VARCHAR(255)
);

CREATE INDEX idx_service_requests_citizen_sub ON service_requests (citizen_sub);
CREATE INDEX idx_service_requests_status ON service_requests (status);
