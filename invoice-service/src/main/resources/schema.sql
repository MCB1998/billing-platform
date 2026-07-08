-- Sequence backing the human-readable invoiceNumber (INV-00001, INV-00002, ...).
-- Used by the H2 dev/test profile; for PostgreSQL it will move into a Flyway migration.
CREATE SEQUENCE IF NOT EXISTS invoice_number_seq START WITH 1 INCREMENT BY 1;
