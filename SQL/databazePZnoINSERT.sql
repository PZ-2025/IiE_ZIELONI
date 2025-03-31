drop schema pzdb;

create schema pzdb;
use pzdb;

CREATE TABLE `Roles` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `name` enum('prezes','projektManager','teamLider','pracownik'),
  `privilege_level` tinyint
);

CREATE TABLE `Teams` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(255)
);

CREATE TABLE `Users` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `team_id` int,
  `role_id` int,
  `first_name` varchar(20),
  `last_name` varchar(30),
  `hire_date` date,
  `login` varchar(30) UNIQUE,
  `password_hash` varchar(60),
  `created_at` timestamp,
  FOREIGN KEY (`team_id`) REFERENCES `Teams` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (`role_id`) REFERENCES `Roles` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE `Projects` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `manager_id` int NOT NULL ,
  `name` varchar(255),
  `progress` tinyint CHECK (progress BETWEEN 0 AND 100),
  `status` enum('planowany','wTrakcie','zakonczony','anulowany'),
  `start_date` date NOT NULL,
  `end_date` date,
  FOREIGN KEY (`manager_id`) REFERENCES `Users` (`id`) ON DELETE CASCADE,
  CHECK (end_date >= start_date)
);

CREATE TABLE `ProjectTeams` (
  `project_id` int,
  `team_id` int,
  PRIMARY KEY (`project_id`, `team_id`),
  FOREIGN KEY (`project_id`) REFERENCES `Projects` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`team_id`) REFERENCES `Teams` (`id`) ON DELETE CASCADE
);

CREATE TABLE `Milestones` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `name` varchar(50),
  `progress` tinyint CHECK (progress BETWEEN 0 AND 100),
  `description` text,
  `deadline` date,
  FOREIGN KEY (`project_id`) REFERENCES `Projects` (`id`) ON DELETE CASCADE
);

CREATE TABLE `Tasks` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `milestone_id` int NOT NULL,
  `title` varchar(50),
  `description` text,
  `priority` enum('niski','sredni','wysoki'),
  `status` enum('doZrobienia','wTrakcie','zrobione','anulowane'),
  `progress` tinyint CHECK (progress BETWEEN 0 AND 100),
  `created_at` timestamp,
  `deadline` date,
  `canceled_by` int,
  FOREIGN KEY (`milestone_id`) REFERENCES `Milestones` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`canceled_by`) REFERENCES `Users` (`id`) ON DELETE RESTRICT
);

CREATE TABLE `TaskAssignments` (
  `task_id` int,
  `assigned_by` int,
  `user_id` int,
  `assigned_at` timestamp,
  PRIMARY KEY (`task_id`, `user_id`),
  FOREIGN KEY (`task_id`) REFERENCES `Tasks` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`assigned_by`) REFERENCES `Users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`) ON DELETE CASCADE
);

CREATE TABLE `Reports` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `created_by` int,
  `type` varchar(255),
  `parameters` text,
  `generated_at` timestamp,
  FOREIGN KEY (`created_by`) REFERENCES `Users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE `Notifications` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `task_id` int,
  `user_id` int NOT NULL ,
  `report_id` int,
  `type` enum('zadaniePrzypisane','aktualizacjaZadania','deadline','generowanieRaportu','inne'),
  `message` text,
  `is_read` boolean DEFAULT false,
  `created_at` timestamp,
  FOREIGN KEY (`task_id`) REFERENCES `Tasks` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`report_id`) REFERENCES `Reports` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
);


                    #============
                    # indexy
                    #============
CREATE INDEX idx_tasks_status ON Tasks(status);
CREATE INDEX idx_milestones_deadline ON  Milestones(deadline);
CREATE INDEX idx_user_team_role ON Users(team_id, role_id);
CREATE INDEX idx_tasks_priority ON Tasks(priority);
CREATE INDEX idx_projects_status ON Projects(status);
CREATE INDEX idx_projects_manager_id ON Projects(manager_id);
CREATE INDEX idx_tasks_status_priority_deadline ON Tasks(status, priority, deadline);
CREATE INDEX idx_users_team_role_hire ON Users(team_id, role_id, hire_date);
CREATE INDEX idx_milestones_project_deadline_progress ON Milestones(project_id, deadline, progress);
CREATE INDEX idx_assignments_task_user_date ON TaskAssignments(task_id, user_id, assigned_at);
CREATE INDEX idx_projects_manager_status_end ON Projects(manager_id, status, end_date);
CREATE INDEX idx_milestones_project ON Milestones(project_id);
CREATE INDEX idx_tasks_milestone ON Tasks(milestone_id);
CREATE INDEX idx_users_role_id ON Users(role_id);
CREATE INDEX idx_tasks_canceled_by ON Tasks(canceled_by);
CREATE INDEX idx_notifications_task_id ON Notifications(task_id);
CREATE INDEX idx_notifications_report_id ON Notifications(report_id);
CREATE INDEX idx_reports_created_by ON Reports(created_by);
CREATE INDEX idx_tasks_deadline ON Tasks(deadline);
CREATE INDEX idx_reports_type ON Reports(type);
CREATE INDEX idx_notifications_type ON Notifications(type);
CREATE INDEX idx_users_role_team ON Users(role_id, team_id);
CREATE INDEX idx_users_team ON Users(team_id);
                    #============
                    # widoki
                    #============
CREATE VIEW vw_UserDetails AS
SELECT
    u.id,
    u.first_name,
    u.last_name,
    r.name AS role,
    t.name AS team,
    u.hire_date,
    u.login,
    u.created_at
FROM Users u
JOIN Roles r ON u.role_id = r.id
LEFT JOIN Teams t ON u.team_id = t.id;

CREATE OR REPLACE VIEW vw_ProjectDetails AS
SELECT
    p.id,
    p.name AS project_name,
    CONCAT(u.first_name, ' ', u.last_name) AS manager,
    MAX(p.progress) AS progress,
    MAX(p.status) AS status,
    MAX(p.start_date) AS start_date,
    MAX(p.end_date) AS end_date,
    GROUP_CONCAT(t.name SEPARATOR ', ') AS teams
FROM Projects p
JOIN Users u ON p.manager_id = u.id
LEFT JOIN ProjectTeams pt ON p.id = pt.project_id
LEFT JOIN Teams t ON pt.team_id = t.id
GROUP BY p.id;


CREATE VIEW vw_TaskAssignments AS
SELECT
    t.title,
    t.status AS task_status,
    CONCAT(a.first_name, ' ', a.last_name) AS assigned_by,
    CONCAT(u.first_name, ' ', u.last_name) AS assigned_to,
    ta.assigned_at
FROM TaskAssignments ta
JOIN Tasks t ON ta.task_id = t.id
JOIN Users u ON ta.user_id = u.id
JOIN Users a ON ta.assigned_by = a.id;

CREATE VIEW vw_MilestoneProgress AS
SELECT
    m.name AS milestone,
    m.progress,
    m.deadline,
    p.name AS project,
    p.progress AS project_progress
FROM Milestones m
JOIN Projects p ON m.project_id = p.id;

CREATE VIEW vw_UpcomingDeadlines AS
SELECT
    'Task' AS type,
    t.title,
    t.deadline,
    CONCAT(u.first_name, ' ', u.last_name) AS assigned_to
FROM Tasks t
JOIN TaskAssignments ta ON t.id = ta.task_id
JOIN Users u ON ta.user_id = u.id
WHERE t.deadline BETWEEN CURDATE() AND CURDATE() + INTERVAL 7 DAY
UNION ALL
SELECT
    'Milestone' AS type,
    m.name,
    m.deadline,
    NULL
FROM Milestones m
WHERE m.deadline BETWEEN CURDATE() AND CURDATE() + INTERVAL 7 DAY;

CREATE VIEW vw_UserTaskLoad AS
SELECT
    CONCAT(u.first_name, ' ', u.last_name) AS user,
    COUNT(t.id) AS total_tasks,
    SUM(t.status = 'doZrobienia') AS todo,
    SUM(t.status = 'wTrakcie') AS in_progress,
    SUM(t.status = 'zrobione') AS done
FROM Users u
LEFT JOIN TaskAssignments ta ON u.id = ta.user_id
LEFT JOIN Tasks t ON ta.task_id = t.id
GROUP BY u.id;

CREATE VIEW vw_ProjectSummary AS
SELECT
    p.name AS project,
    p.status,
    p.progress,
    COUNT(DISTINCT m.id) AS milestones,
    COUNT(DISTINCT t.id) AS tasks,
    MIN(t.deadline) AS next_deadline
FROM Projects p
LEFT JOIN Milestones m ON p.id = m.project_id
LEFT JOIN Tasks t ON m.id = t.milestone_id
GROUP BY p.id, p.name, p.status, p.progress;

CREATE VIEW vw_UserPrivileges AS
SELECT
    u.login,
    r.name AS role,
    r.privilege_level,
    CASE
        WHEN r.name = 'prezes' THEN (SELECT GROUP_CONCAT(DISTINCT name SEPARATOR ', ') FROM Projects)
        ELSE GROUP_CONCAT(DISTINCT p.name SEPARATOR ', ')
    END AS managed_projects
FROM Users u
JOIN Roles r ON u.role_id = r.id
LEFT JOIN Projects p ON u.id = p.manager_id
GROUP BY u.id;

                    #============
                    # RAPORTY JAKO WIDOKI
                    #============
#Raport wydajności pracownika
CREATE VIEW vw_EmployeePerformance AS
SELECT
    u.id AS user_id,
    CONCAT(u.first_name, ' ', u.last_name) AS employee,
    COUNT(t.id) AS total_tasks,
    SUM(CASE WHEN t.status = 'zrobione' THEN 1 ELSE 0 END) AS completed,
    SUM(CASE WHEN t.status = 'anulowane' THEN 1 ELSE 0 END) AS canceled,
    (SUM(CASE WHEN t.status = 'zrobione' THEN 1 ELSE 0 END) / COUNT(t.id)) * 100 AS completion_rate
FROM Users u
LEFT JOIN TaskAssignments ta ON u.id = ta.user_id
LEFT JOIN Tasks t ON ta.task_id = t.id
WHERE u.role_id = (SELECT id FROM Roles WHERE name = 'pracownik')
GROUP BY u.id;
# Raport postępów projektu
CREATE VIEW vw_ProjectProgress AS
SELECT
    p.name AS project,
    p.status,
    p.progress AS overall_progress,
    COUNT(DISTINCT m.id) AS total_milestones,
    COUNT(t.id) AS total_tasks,
    SUM(CASE WHEN t.status = 'zrobione' THEN 1 ELSE 0 END) AS completed_tasks,
    SUM(CASE WHEN t.status = 'anulowane' THEN 1 ELSE 0 END) AS canceled_tasks,
    AVG(m.progress) AS avg_milestone_progress
FROM Projects p
LEFT JOIN Milestones m ON p.id = m.project_id
LEFT JOIN Tasks t ON m.id = t.milestone_id
GROUP BY p.id;

CREATE VIEW vw_ExecutiveOverview AS
SELECT
    p.name AS project,
    p.status AS project_status,
    p.progress AS project_progress,
    COUNT(DISTINCT pt.team_id) AS teams_involved,
    COUNT(DISTINCT u.id) AS employees_assigned,
    COUNT(DISTINCT m.id) AS milestones,
    SUM(CASE WHEN tsk.status = 'zrobione' THEN 1 ELSE 0 END) AS tasks_done,
    SUM(CASE WHEN tsk.status = 'anulowane' THEN 1 ELSE 0 END) AS tasks_canceled
FROM Projects p
LEFT JOIN ProjectTeams pt ON p.id = pt.project_id
LEFT JOIN Teams tm ON pt.team_id = tm.id
LEFT JOIN Users u ON tm.id = u.team_id
LEFT JOIN Milestones m ON p.id = m.project_id
LEFT JOIN Tasks tsk ON m.id = tsk.milestone_id
GROUP BY p.id;


                    #============
                    # triggery
                    #============
DELIMITER //

CREATE TRIGGER CheckAssignmentPrivileges
BEFORE INSERT ON TaskAssignments
FOR EACH ROW
BEGIN
    DECLARE assigner_privilege TINYINT;
    DECLARE assigner_role VARCHAR(20);
    DECLARE target_user_team INT;
    DECLARE assigner_team INT;


    SELECT r.privilege_level, u.team_id, r.name
    INTO assigner_privilege, assigner_team, assigner_role
    FROM Users u
    JOIN Roles r ON u.role_id = r.id
    WHERE u.id = NEW.assigned_by;


    SELECT team_id INTO target_user_team
    FROM Users
    WHERE id = NEW.user_id;


    IF assigner_privilege < 2 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Brak uprawnień do przypisywania zadań!';
    END IF;


    IF assigner_role = 'teamLider' AND assigner_team != target_user_team THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Team Lider może przypisywać zadania tylko członkom swojego zespołu!';
    END IF;


    IF assigner_role = 'pracownik' AND NEW.assigned_by != NEW.user_id THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Pracownicy mogą przypisywać zadania tylko sobie!';
    END IF;
END //


CREATE TRIGGER CheckAssignmentPrivilegesOnUpdate
BEFORE UPDATE ON TaskAssignments
FOR EACH ROW
BEGIN
    DECLARE assigner_privilege TINYINT;
    DECLARE assigner_role VARCHAR(20);
    DECLARE target_user_team INT;
    DECLARE assigner_team INT;

    SELECT r.privilege_level, u.team_id, r.name
    INTO assigner_privilege, assigner_team, assigner_role
    FROM Users u
    JOIN Roles r ON u.role_id = r.id
    WHERE u.id = NEW.assigned_by;

    SELECT team_id INTO target_user_team
    FROM Users
    WHERE id = NEW.user_id;

    IF assigner_privilege < 2 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Brak uprawnień do przypisywania zadań!';
    END IF;

    IF assigner_role = 'teamLider' AND assigner_team != target_user_team THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Team Lider może przypisywać zadania tylko członkom swojego zespołu!';
    END IF;

    IF assigner_role = 'pracownik' AND NEW.assigned_by != NEW.user_id THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Pracownicy mogą przypisywać zadania tylko sobie!';
    END IF;
END //

DELIMITER ;

DELIMITER //
CREATE TRIGGER CheckProjectManagerRole
BEFORE INSERT ON Projects
FOR EACH ROW
BEGIN
    DECLARE manager_role VARCHAR(20);

    SELECT r.name INTO manager_role
    FROM Users u
    JOIN Roles r ON u.role_id = r.id
    WHERE u.id = NEW.manager_id;

    IF manager_role != 'projektManager' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tylko użytkownicy z rolą projektManager mogą być menedżerami projektów';
    END IF;
END //


CREATE TRIGGER CheckProjectManagerRoleOnUpdate
BEFORE UPDATE ON Projects
FOR EACH ROW
BEGIN
    DECLARE manager_role VARCHAR(20);

    SELECT r.name INTO manager_role
    FROM Users u
    JOIN Roles r ON u.role_id = r.id
    WHERE u.id = NEW.manager_id;

    IF manager_role != 'projektManager' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tylko użytkownicy z rolą projektManager mogą być menedżerami projektów';
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER UpdateDeadlineNotification
AFTER UPDATE ON Tasks
FOR EACH ROW
BEGIN
    IF NEW.deadline <> OLD.deadline THEN
        INSERT INTO Notifications (task_id, user_id, type, message, created_at)
        SELECT
            NEW.id,
            ta.user_id,
            'deadline',
            CONCAT('Zmieniono deadline zadania "', NEW.title, '" na ', NEW.deadline),
            NOW()
        FROM TaskAssignments ta
        WHERE ta.task_id = NEW.id;
    END IF;
END //
DELIMITER ;

DELIMITER //

CREATE TRIGGER SetPrivilegeLevelOnInsert
BEFORE INSERT ON Roles
FOR EACH ROW
BEGIN
    SET NEW.privilege_level =
        CASE NEW.name
            WHEN 'prezes' THEN 4
            WHEN 'projektManager' THEN 3
            WHEN 'teamLider' THEN 2
            WHEN 'pracownik' THEN 1
        END;
END //

CREATE TRIGGER SetPrivilegeLevelOnUpdate
BEFORE UPDATE ON Roles
FOR EACH ROW
BEGIN
    SET NEW.privilege_level =
        CASE NEW.name
            WHEN 'prezes' THEN 4
            WHEN 'projektManager' THEN 3
            WHEN 'teamLider' THEN 2
            WHEN 'pracownik' THEN 1
        END;
END //
DELIMITER //

CREATE TRIGGER Tasks_CheckCanceledBy_Insert
BEFORE INSERT ON Tasks
FOR EACH ROW
BEGIN
    IF NEW.status = 'anulowane' AND NEW.canceled_by IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Pole canceled_by musi być ustawione przy statusie anulowane.';
    END IF;
END //

CREATE TRIGGER Tasks_CheckCanceledBy_Update
BEFORE UPDATE ON Tasks
FOR EACH ROW
BEGIN
    IF NEW.status = 'anulowane' AND (NEW.canceled_by IS NULL OR NEW.canceled_by = '') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Pole canceled_by musi być ustawione przy statusie anulowane.';
    END IF;
END //
DELIMITER ;


DELIMITER //
CREATE TRIGGER UpdateProjectProgress
AFTER UPDATE ON Tasks
FOR EACH ROW
BEGIN
    DECLARE project_id_val INT;
    SELECT m.project_id INTO project_id_val FROM Milestones m WHERE m.id = NEW.milestone_id;

    UPDATE Projects p
    SET p.progress = (
        SELECT COALESCE(
            ROUND(SUM(CASE WHEN t.status = 'zrobione' THEN 1 ELSE 0 END) * 100 / NULLIF(COUNT(t.id), 0)),
            0
        )
        FROM Tasks t
        JOIN Milestones m ON t.milestone_id = m.id
        WHERE m.project_id = project_id_val
    )
    WHERE p.id = project_id_val;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER DeadlineNotification
AFTER INSERT ON TaskAssignments
FOR EACH ROW
BEGIN
    DECLARE task_deadline DATE;

    SELECT deadline INTO task_deadline
    FROM Tasks
    WHERE id = NEW.task_id;

    IF task_deadline BETWEEN CURDATE() AND CURDATE() + INTERVAL 3 DAY THEN
        INSERT INTO Notifications (task_id, user_id, type, message, created_at)
        VALUES (
            NEW.task_id,
            NEW.user_id,
            'deadline',
            CONCAT('Zadanie "', (SELECT title FROM Tasks WHERE id = NEW.task_id), '" ma deadline za ',
                   DATEDIFF(task_deadline, CURDATE()), ' dni!'),
            NOW()
        );
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER PreventUnauthorizedCancel
BEFORE UPDATE ON Tasks
FOR EACH ROW
BEGIN
    DECLARE user_role VARCHAR(20);

    IF NEW.status = 'anulowane' THEN

        SELECT name INTO user_role
        FROM Roles
        WHERE id = (SELECT role_id FROM Users WHERE id = NEW.canceled_by LIMIT 1);

        IF user_role NOT IN ('teamLider', 'prezes') THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Tylko Team Lider lub Prezes może anulować zadanie!';
        END IF;
    END IF;
END //
DELIMITER ;
                    #============
                    #FUNKCJE
                    #============
# Raport miesięczny dla Prezesa
DELIMITER //

CREATE FUNCTION GenerateExecutiveReport(start_date DATE, end_date DATE)
RETURNS TEXT
BEGIN
    DECLARE report_text TEXT;


    SELECT CONCAT(
        'Liczba aktywnych projektów: ', COUNT(p.id), '\n',
        'Średnia wydajność: ', COALESCE(ROUND(AVG(p.progress)), '0'), '%\n',
        'Zadania zakończone: ', COALESCE(SUM(CASE WHEN t.status = 'zrobione' THEN 1 ELSE 0 END), 0), '\n',
        'Zadania anulowane: ', COALESCE(SUM(CASE WHEN t.status = 'anulowane' THEN 1 ELSE 0 END), 0)
    ) INTO report_text
    FROM Projects p
    LEFT JOIN Milestones m ON p.id = m.project_id
    LEFT JOIN Tasks t ON m.id = t.milestone_id
    WHERE
        p.start_date <= end_date AND
        (p.end_date >= start_date OR p.end_date IS NULL);


    RETURN report_text;
END //

DELIMITER ;

                    #============
                    #PROCEDURY
                    #============
#Raport zespołu

DELIMITER //

DELIMITER //

CREATE PROCEDURE GetEmployeePerformance(
    IN user_id INT,
    IN start_date DATE,
    IN end_date DATE
)
BEGIN
    SELECT
        CONCAT(u.first_name, ' ', u.last_name) AS employee,
        COUNT(t.id) AS total_tasks,
        SUM(CASE WHEN t.status = 'zrobione' THEN 1 ELSE 0 END) AS completed,
        SUM(CASE WHEN t.status = 'anulowane' THEN 1 ELSE 0 END) AS canceled,
        COALESCE(
            ROUND(
                (SUM(CASE WHEN t.status = 'zrobione' THEN 1 ELSE 0 END)
                / NULLIF(COUNT(t.id), 0)) * 100, 2
            ), 0
        ) AS completion_rate
    FROM Users u
    LEFT JOIN TaskAssignments ta ON u.id = ta.user_id
    LEFT JOIN Tasks t ON ta.task_id = t.id
    WHERE u.id = user_id
      AND t.created_at BETWEEN start_date AND end_date
    GROUP BY u.id;
END //

DELIMITER ;
