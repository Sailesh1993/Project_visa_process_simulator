TRUNCATE TABLE application_log;

TRUNCATE TABLE distribution_config;

TRUNCATE TABLE servicepoint_result;

DELETE FROM simulation_run;


-- Search query in order
SELECT
    ServicePoint_Name,
    Distribution_Type,
    Parameter1,
    Parameter2
FROM
    distribution_config;