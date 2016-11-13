CREATE TABLE monitoring_machines(
	id INTEGER, 
	timestamp TIMESTAMP, 
	machine_address CHARACTER(15), 
	disk_total VARCHAR, 
	disk_free VARCHAR, 
	mem_total VARCHAR, 
	mem_free VARCHAR, 
	cpu_used VARCHAR, 
	running_process INTEGER, 
	laboratory_id INTEGER
);