CREATE TABLE monitoring_machines(
	id INTEGER, 
	timestamp TIMESTAMP, 
	machine_address CHARACTER(15), 
	disk_total INTEGER, 
	disk_free INTEGER, 
	mem_total INTEGER, 
	mem_free INTEGER, 
	cpu_used INTEGER, 
	running_process INTEGER, 
	laboratory_id INTEGER
);