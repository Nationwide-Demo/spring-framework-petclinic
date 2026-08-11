-- One database per service: services never read each other's tables.
CREATE DATABASE customers;
CREATE DATABASE vets;
CREATE DATABASE visits;
