CREATE DATABASE simulationproject;

CREATE USER 'dbuser'@'localhost' IDENTIFIED BY 'group7';

GRANT SELECT,INSERT,UPDATE,DELETE ON simulationproject.* TO 'dbuser'@'localhost';

GRANT CREATE, DROP, ALTER ON simulationproject.* TO 'dbuser'@'localhost';

-- export a table to CSV
SELECT * FROM your_table
    INTO OUTFILE '/var/lib/mysql-files/your_table.csv'
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n';


-- Or from terminal:
mysqldump -u root -p your_database > backup.sql

