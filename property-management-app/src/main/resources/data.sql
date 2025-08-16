-- H2 database
INSERT INTO community (community_id, president_id, vice_president_id, address, cif)
VALUES (1, 1, 2, '99 Santander Street', '0384858C');
INSERT INTO community (community_id, president_id, vice_president_id, address, cif)
VALUES (2, 1, 2, '1 Canarias Street', '999999A');
INSERT INTO community (community_id, president_id, vice_president_id, address, cif)
VALUES (3, 5, 6, '112 Delicias Avenue', '111111A');
INSERT INTO community (community_id, president_id, vice_president_id, address, cif)
VALUES (4, 5, 6, '112 Delicias Avenue', '222222A');
INSERT INTO neighbour (neighbourg_id, phonenumber, email, fullname, president, vicepresident, community_id)
VALUES(1, '+34676544887', 'president@propertymanagement.org', 'Sprinter', 1, 0, 1);
INSERT INTO neighbour (neighbourg_id, phonenumber, email, fullname, president, vicepresident, community_id)
VALUES(2, '+34699999999', 'vicepresident@propertymanagement.org', 'John', 0, 1, 1);
INSERT INTO neighbour (neighbourg_id, phonenumber, email, fullname, president, vicepresident, community_id)
VALUES(3, '+34888888888', 'mark@propertymanagement.org', 'Mark', null, null, 1);
INSERT INTO neighbour (neighbourg_id, phonenumber, email, fullname, president, vicepresident, community_id)
VALUES(4, '+34888888888', 'louis@propertymanagement.org', 'Louis', null, null, 1);
INSERT INTO neighbour (neighbourg_id, phonenumber, email, fullname, president, vicepresident, community_id)
VALUES(5, '+34112233445', 'presidentTwo@propertymanagement.org', 'Sophie', 1, 0, 3);
INSERT INTO neighbour (neighbourg_id, phonenumber, email, fullname, president, vicepresident, community_id)
VALUES(6, '+34556677889', 'vicePresidentTwo@propertymanagement.org', 'Ellen', 0, 1, 3);
INSERT INTO neighbour (neighbourg_id, phonenumber, email, fullname, president, vicepresident, community_id)
VALUES(7, '+34111222333', 'sandra@propertymanagement.org', 'Sandra', 0, 0, 3);




