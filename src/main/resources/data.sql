-- ============================================================
-- SEED DATA: 10 Customers + 10 Accounts (manually inserted)
-- IDs 1–10 are explicit. JPA will auto-generate from 11 onwards.
-- Mix: 8 ACTIVE | 1 FROZEN | 1 INACTIVE
-- ============================================================

-- Customers
INSERT INTO customer (id, first_name, last_name, email, phone_number) VALUES (1,  'Alice',    'Johnson',  'alice@example.com',    '555-0100');
INSERT INTO customer (id, first_name, last_name, email, phone_number) VALUES (2,  'Bob',      'Smith',    'bob@example.com',      '555-0101');
INSERT INTO customer (id, first_name, last_name, email, phone_number) VALUES (3,  'Carol',    'White',    'carol@example.com',    '555-0102');
INSERT INTO customer (id, first_name, last_name, email, phone_number) VALUES (4,  'David',    'Brown',    'david@example.com',    '555-0103');
INSERT INTO customer (id, first_name, last_name, email, phone_number) VALUES (5,  'Eva',      'Martinez', 'eva@example.com',      '555-0104');
INSERT INTO customer (id, first_name, last_name, email, phone_number) VALUES (6,  'Frank',    'Wilson',   'frank@example.com',    '555-0105');
INSERT INTO customer (id, first_name, last_name, email, phone_number) VALUES (7,  'Grace',    'Lee',      'grace@example.com',    '555-0106');
INSERT INTO customer (id, first_name, last_name, email, phone_number) VALUES (8,  'Henry',    'Taylor',   'henry@example.com',    '555-0107');
INSERT INTO customer (id, first_name, last_name, email, phone_number) VALUES (9,  'Isabella', 'Anderson', 'isabella@example.com', '555-0108');
INSERT INTO customer (id, first_name, last_name, email, phone_number) VALUES (10, 'James',    'Thomas',   'james@example.com',    '555-0109');

-- Accounts
INSERT INTO account (id, account_number, customer_id, balance, status, version) VALUES (1,  'ACC-0001-UUID', 1,  50000.00,  'ACTIVE',   0);
INSERT INTO account (id, account_number, customer_id, balance, status, version) VALUES (2,  'ACC-0002-UUID', 2,  25000.00,  'ACTIVE',   0);
INSERT INTO account (id, account_number, customer_id, balance, status, version) VALUES (3,  'ACC-0003-UUID', 3,  10000.00,  'FROZEN',   0);
INSERT INTO account (id, account_number, customer_id, balance, status, version) VALUES (4,  'ACC-0004-UUID', 4,  75000.00,  'ACTIVE',   0);
INSERT INTO account (id, account_number, customer_id, balance, status, version) VALUES (5,  'ACC-0005-UUID', 5,  30000.00,  'ACTIVE',   0);
INSERT INTO account (id, account_number, customer_id, balance, status, version) VALUES (6,  'ACC-0006-UUID', 6,  12000.00,  'ACTIVE',   0);
INSERT INTO account (id, account_number, customer_id, balance, status, version) VALUES (7,  'ACC-0007-UUID', 7,  98000.00,  'ACTIVE',   0);
INSERT INTO account (id, account_number, customer_id, balance, status, version) VALUES (8,  'ACC-0008-UUID', 8,  5000.00,   'INACTIVE', 0);
INSERT INTO account (id, account_number, customer_id, balance, status, version) VALUES (9,  'ACC-0009-UUID', 9,  200000.00, 'ACTIVE',   0);
INSERT INTO account (id, account_number, customer_id, balance, status, version) VALUES (10, 'ACC-0010-UUID', 10, 8500.00,   'ACTIVE',   0);

COMMIT;
