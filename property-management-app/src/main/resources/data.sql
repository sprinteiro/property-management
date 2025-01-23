-- H2 database
INSERT INTO community (community_id, president_id, vice_president_id, address, cif)
VALUES (1, 1, 2, '1 Canarias Street', '0384858C');
INSERT INTO neighbour (neighbourg_id, phonenumber, email, fullname, president, vicepresident, community_id)
VALUES(1, '+34676544887', 'president@propertymanagement.org', 'Sprinter', 1, 0, 1);
INSERT INTO neighbour (neighbourg_id, phonenumber, email, fullname, president, vicepresident, community_id)
VALUES(2, '+34699999999', 'vicepresident@propertymanagement.org', 'John', 0, 1, 1);
INSERT INTO neighbour (neighbourg_id, phonenumber, email, fullname, president, vicepresident, community_id)
VALUES(3, '+34888888888', 'mark@propertymanagement.org', 'Mark', null, null, 1);
INSERT INTO neighbour (neighbourg_id, phonenumber, email, fullname, president, vicepresident, community_id)
VALUES(4, '+34888888888', 'louis@propertymanagement.org', 'Louis', null, null, 1);




