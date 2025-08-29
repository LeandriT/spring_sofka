DROP TABLE IF EXISTS "clients";

CREATE TABLE "clients" (
    "id" BIGINT PRIMARY KEY AUTO_INCREMENT,
    "name" VARCHAR(255),
    "gender" VARCHAR(255),
    "age" INT,
    "dni" VARCHAR(255),
    "address" VARCHAR(255),
    "phone" VARCHAR(255),
    "password" VARCHAR(255),
    "is_active" BOOLEAN,
    "created_at" TIMESTAMP NULL,
    "updated_at" TIMESTAMP NULL,
    "deleted" BOOLEAN NOT NULL DEFAULT FALSE,
    "deleted_at" TIMESTAMP NULL
);
