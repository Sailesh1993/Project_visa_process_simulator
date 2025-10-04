CREATE DATABASE simulationproject;

CREATE USER 'dbuser'@'localhost' IDENTIFIED BY 'group7';

GRANT SELECT,INSERT,UPDATE,DELETE ON simulationproject.* TO 'dbuser'@'localhost';

GRANT CREATE, DROP, ALTER ON simulationproject.* TO 'dbuser'@'localhost';

