CREATE TABLE IF NOT EXISTS employee_dataset (
    id         INT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    age        INT NOT NULL,
    department VARCHAR(100) NOT NULL
);

INSERT IGNORE INTO employee_dataset (id, name, age, department) VALUES
(1,'John Doe',30,'Engineering'),(2,'Jane Smith',25,'Engineering'),
(3,'Alice Brown',28,'Marketing'),(4,'Bob Wilson',35,'Sales'),
(5,'Carol White',41,'Engineering'),(6,'David Lee',29,'Marketing'),
(7,'Emma Davis',33,'HR'),(8,'Frank Miller',45,'Sales'),
(9,'Grace Taylor',27,'HR'),(10,'Henry Clark',38,'Engineering');