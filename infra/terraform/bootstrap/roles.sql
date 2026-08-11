-- Run once against the Aurora cluster (as the master user) to create the per-service
-- roles and schemas. Passwords come from the Secrets Manager secrets Terraform created:
--   aws secretsmanager get-secret-value --secret-id petclinic-<env>/<service>/database
--
-- Each service can only touch its own schema, so a compromised service cannot read
-- another service's tables even though they share one Aurora cluster.

\set customers_password `echo $CUSTOMERS_PASSWORD`
\set vets_password `echo $VETS_PASSWORD`
\set visits_password `echo $VISITS_PASSWORD`

CREATE ROLE customers LOGIN PASSWORD :'customers_password';
CREATE ROLE vets LOGIN PASSWORD :'vets_password';
CREATE ROLE visits LOGIN PASSWORD :'visits_password';

CREATE SCHEMA IF NOT EXISTS customers AUTHORIZATION customers;
CREATE SCHEMA IF NOT EXISTS vets AUTHORIZATION vets;
CREATE SCHEMA IF NOT EXISTS visits AUTHORIZATION visits;

REVOKE ALL ON SCHEMA public FROM PUBLIC;

GRANT CONNECT ON DATABASE petclinic TO customers, vets, visits;
