-- Sequence backing the human-readable customerNumber (C-00001, C-00002, ...).
-- Declared explicitly here for now; it will move into a Flyway migration when we
-- switch to PostgreSQL. A DB sequence guarantees unique, monotonic values even
-- under concurrent inserts and across multiple service instances.
CREATE SEQUENCE IF NOT EXISTS customer_number_seq START WITH 1 INCREMENT BY 1;
