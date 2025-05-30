# awaryjna kwerenda do ubijania calej bazy danych
drop schema pzdb;

# tworzenie bazy danych
create schema pzdb;

# wybieranie schematu pzdb jako domyslnego
use pzdb;

# tabela z rolami i poziomem uprawnien
CREATE TABLE `Roles` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `name` enum('prezes','projektManager','teamLider','pracownik'),
  `privilege_level` tinyint
);
# tabela z nazwami teamow
CREATE TABLE `Teams` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(255)
);

# tabela z informacjami o uzytkownikach
CREATE TABLE `Users` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `team_id` int,
  `role_id` int,
  `first_name` varchar(20),
  `last_name` varchar(30),
  `hire_date` date,
  `login` varchar(30) UNIQUE,
  `password_hash` varchar(255),
  `created_at` timestamp,
  FOREIGN KEY (`team_id`) REFERENCES `Teams` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (`role_id`) REFERENCES `Roles` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
);

# tabela z informacjami o projekcie
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

# tabela z przypisaniem teamow do projektow
CREATE TABLE `ProjectTeams` (
  `project_id` int,
  `team_id` int,
  PRIMARY KEY (`project_id`, `team_id`),
  FOREIGN KEY (`project_id`) REFERENCES `Projects` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`team_id`) REFERENCES `Teams` (`id`) ON DELETE CASCADE
);

# tabela z kamieniami milowymi projektow
CREATE TABLE `Milestones` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `name` varchar(50),
  `progress` tinyint CHECK (progress BETWEEN 0 AND 100),
  `description` text,
  `deadline` date,
  FOREIGN KEY (`project_id`) REFERENCES `Projects` (`id`) ON DELETE CASCADE
);

# tabela z informacjami o zadaniu
CREATE TABLE  `Tasks` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `milestone_id` int NOT NULL,
  `title` varchar(50),
  `description` text,
  `priority` enum('niski','sredni','wysoki'),
  `status` enum('doZrobienia','wTrakcie','zrobione','anulowane'),
  `progress` tinyint CHECK (progress BETWEEN 0 AND 100),
  `progress_verify` boolean DEFAULT false,
  `created_at` timestamp,
  `deadline` date,
  `canceled_by` int,
  FOREIGN KEY (`milestone_id`) REFERENCES `Milestones` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`canceled_by`) REFERENCES `Users` (`id`) ON DELETE RESTRICT
);
# ALTER TABLE Tasks ADD COLUMN progress_verify boolean DEFAULT false;
# tabela z informacjami o przypisaniu zadan do uzytkownikow
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

# tabela z informacjami o wygenerowanych raportach
CREATE TABLE `Reports` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `created_by` int,
  `type` varchar(255),
  `parameters` text,
  `generated_at` timestamp,
  FOREIGN KEY (`created_by`) REFERENCES `Users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
);

# tabela z informacjami do powiadomien
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
# indexy dla tabeli Tasks
CREATE INDEX idx_tasks_status ON Tasks(status);
CREATE INDEX idx_tasks_priority ON Tasks(priority);
CREATE INDEX idx_tasks_status_priority_deadline ON Tasks(status, priority, deadline);
CREATE INDEX idx_tasks_milestone ON Tasks(milestone_id);
CREATE INDEX idx_tasks_canceled_by ON Tasks(canceled_by);
CREATE INDEX idx_tasks_deadline ON Tasks(deadline);

# indexy dla tabeli Users
CREATE INDEX idx_users_team ON Users(team_id);
CREATE INDEX idx_users_role_team ON Users(role_id, team_id);
CREATE INDEX idx_users_role_id ON Users(role_id);
CREATE INDEX idx_users_team_role_hire ON Users(team_id, role_id, hire_date);
CREATE INDEX idx_user_team_role ON Users(team_id, role_id);

# indexy dla tabeli Projects
CREATE INDEX idx_projects_status ON Projects(status);
CREATE INDEX idx_projects_manager_id ON Projects(manager_id);
CREATE INDEX idx_projects_manager_status_end ON Projects(manager_id, status, end_date);

# indexy dla tabeli Milestone
CREATE INDEX idx_milestones_project_deadline_progress ON Milestones(project_id, deadline, progress);
CREATE INDEX idx_milestones_project ON Milestones(project_id);
CREATE INDEX idx_milestones_deadline ON  Milestones(deadline);

# indexy dla tabeli Notifications
CREATE INDEX idx_notifications_task_id ON Notifications(task_id);
CREATE INDEX idx_notifications_report_id ON Notifications(report_id);
CREATE INDEX idx_notifications_type ON Notifications(type);

# indexy dla tabeli Raports
CREATE INDEX idx_reports_created_by ON Reports(created_by);
CREATE INDEX idx_reports_type ON Reports(type);

# indexy dla tabeli TaskAssignments
CREATE INDEX idx_assignments_task_user_date ON TaskAssignments(task_id, user_id, assigned_at);

CREATE INDEX idx_projects_status_manager ON Projects(status, manager_id);
CREATE INDEX idx_tasks_deadline_status ON Tasks(deadline, status);
CREATE INDEX idx_milestones_project_deadline ON Milestones(project_id, deadline);

                    #============
                    # widoki
                    #============
#pelne informacje o pracowniku
CREATE VIEW vw_UserCompleteDetails AS
SELECT
    u.id AS user_id,
    u.first_name,
    u.last_name,
    r.name AS role,
    t.name AS team,
    u.hire_date,
    u.login,
    u.created_at AS user_created_at,
    MAX(CONCAT(manager.first_name, ' ', manager.last_name)) AS manager_name,
    MAX(CONCAT(team_leader.first_name, ' ', team_leader.last_name)) AS team_leader_name,
    GROUP_CONCAT(DISTINCT p.name SEPARATOR ', ') AS projects_assigned,
    GROUP_CONCAT(DISTINCT m.name SEPARATOR ', ') AS milestones_assigned,
    GROUP_CONCAT(DISTINCT tas.title SEPARATOR ', ') AS tasks_assigned,
    COUNT(DISTINCT tas.id) AS total_tasks,
    SUM(tas.status = 'doZrobienia') AS todo,
    SUM(tas.status = 'wTrakcie') AS in_progress,
    SUM(tas.status = 'zrobione') AS done,
    SUM(tas.status = 'anulowane') AS canceled
FROM Users u
JOIN Roles r ON u.role_id = r.id
LEFT JOIN Teams t ON u.team_id = t.id
LEFT JOIN TaskAssignments ta ON u.id = ta.user_id
LEFT JOIN Tasks tas ON ta.task_id = tas.id
LEFT JOIN Milestones m ON tas.milestone_id = m.id
LEFT JOIN Projects p ON m.project_id = p.id
LEFT JOIN Users manager ON p.manager_id = manager.id
LEFT JOIN Users team_leader ON t.id = team_leader.team_id AND team_leader.role_id = (SELECT id FROM Roles WHERE name = 'teamLider')
GROUP BY u.id;

# widok do wyswietlania szczegolow uzytkownikow
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

# widok do wyswietlania szczegolow projektu
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

# widok do wyswietlania szczegolow przypisywania zadan i uzytkownikow
CREATE OR REPLACE VIEW vw_TaskAssignments AS
SELECT
    t.id AS task_id,
    u.id AS user_id,
    u.team_id,
    t.title,
    t.status AS task_status,
    CONCAT(a.first_name, ' ', a.last_name) AS assigned_by,
    CONCAT(u.first_name, ' ', u.last_name) AS assigned_to,
    CONCAT(tl.first_name, ' ', tl.last_name) AS team_leader,
    CONCAT(pm.first_name, ' ', pm.last_name) AS project_manager,
    ta.assigned_at
FROM TaskAssignments ta
JOIN Tasks t ON ta.task_id = t.id
JOIN Milestones m ON t.milestone_id = m.id
JOIN Projects p ON m.project_id = p.id
JOIN Users pm ON p.manager_id = pm.id
JOIN Users u ON ta.user_id = u.id
LEFT JOIN Users tl ON tl.team_id = u.team_id AND tl.role_id = (SELECT id FROM Roles WHERE name = 'teamLider')
JOIN Users a ON ta.assigned_by = a.id;

# widok do wyswietlania szczegolow postepu kamieni milowych
CREATE VIEW vw_MilestoneProgress AS
SELECT
    m.name AS milestone,
    m.progress,
    m.deadline,
    p.name AS project,
    p.progress AS project_progress
FROM Milestones m
JOIN Projects p ON m.project_id = p.id;

# widok do wyswietlania zblizajacych sie terminow zadan
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

# widok do wyswietlania kto ile jakich ma zadan w danym statusie
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

# widok do wyswietlania podziału projektu na kamienie i zadania
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

# widok do wyswietlania uzytkownikow ich role i przypisane projekty
CREATE VIEW vw_UserPrivileges AS
SELECT
    u.login,
    r.name AS role,
    r.privilege_level,
    IF(r.name = 'prezes', (SELECT GROUP_CONCAT(DISTINCT name SEPARATOR ', ') FROM Projects),
       GROUP_CONCAT(DISTINCT p.name SEPARATOR ', ')) AS managed_projects
FROM Users u
JOIN Roles r ON u.role_id = r.id
LEFT JOIN Projects p ON u.id = p.manager_id
GROUP BY u.id;

CREATE OR REPLACE VIEW vw_TaskAssignmentDetails AS
SELECT
    u.id AS user_id,
    t.id AS task_id,
    t.title,
    t.description,
    t.priority,
    t.status AS task_status,
    t.progress AS task_progress,
    t.deadline AS task_deadline,
    GROUP_CONCAT(CONCAT(u.first_name, ' ', u.last_name) SEPARATOR ', ') AS assigned_users,
    COUNT(ta.user_id) AS assignment_count,
    MIN(ta.assigned_at) AS first_assigned_at,
    MAX(ta.assigned_at) AS last_assigned_at
FROM Tasks t
LEFT JOIN TaskAssignments ta ON t.id = ta.task_id
LEFT JOIN Users u ON ta.user_id = u.id
GROUP BY t.id, u.id;

                    #============
                    # RAPORTY JAKO WIDOKI
                    #============

# raport wydajności pracownika
CREATE OR REPLACE VIEW vw_EmployeePerformance AS
SELECT
    u.id as user_id,
    CONCAT(u.first_name, ' ', u.last_name) as employee,
    tm.name as team,
    COUNT(tk.id) as total_tasks,
    SUM(CASE WHEN tk.status = 'zrobione' THEN 1 ELSE 0 END) as completed,
    SUM(CASE WHEN tk.status = 'anulowane' THEN 1 ELSE 0 END) as canceled,
    GROUP_CONCAT(
        CASE WHEN tk.status = 'zrobione'
             THEN CONCAT(tk.title, ' (', DATE_FORMAT(tk.created_at, '%Y-%m-%d'), ')')
        END SEPARATOR '\n'
    ) as completed_tasks_titles,
    GROUP_CONCAT(
        CASE WHEN tk.status != 'zrobione'
             THEN CONCAT(tk.title, ' (', tk.status, ', ', DATE_FORMAT(tk.created_at, '%Y-%m-%d'), ')')
        END SEPARATOR '\n'
    ) as pending_tasks_titles,
    IFNULL(
        (SUM(CASE WHEN tk.status = 'zrobione' THEN 1 ELSE 0 END) * 100.0 /
        NULLIF(COUNT(tk.id), 0)),
        0
    ) as completion_rate
FROM
    Users u
    LEFT JOIN Teams tm ON u.team_id = tm.id
    LEFT JOIN TaskAssignments ta ON u.id = ta.user_id
    LEFT JOIN Tasks tk ON ta.task_id = tk.id
GROUP BY
    u.id, u.first_name, u.last_name, tm.name;

# raport postępów projektu
CREATE OR REPLACE VIEW vw_ProjectProgress AS
SELECT
    p.id                                   AS project_id,
    p.manager_id,
    p.name                                 AS project,
    CONCAT(u.first_name, ' ', u.last_name) AS manager,
    p.status,
    p.progress                             AS overall_progress,
    COUNT(DISTINCT m.id)                   AS total_milestones,
    GROUP_CONCAT(DISTINCT m.name SEPARATOR ', ') AS milestone_names,
    COUNT(t.id)                            AS total_tasks,
    GROUP_CONCAT(DISTINCT t.title SEPARATOR ', ') AS task_titles,
    SUM(IF(t.status = 'zrobione', 1, 0))   AS completed_tasks,
    SUM(IF(t.status = 'anulowane', 1, 0))  AS canceled_tasks,
    COALESCE(AVG(m.progress), 0)           AS avg_milestone_progress,
    GROUP_CONCAT(DISTINCT tm.name SEPARATOR ', ') AS involved_teams,
    GROUP_CONCAT(DISTINCT CONCAT(tml.first_name, ' ', tml.last_name, ' (', tm.name, ')') SEPARATOR '; ') AS team_leaders
FROM Projects p
JOIN Users u ON p.manager_id = u.id
LEFT JOIN ProjectTeams pt ON p.id = pt.project_id
LEFT JOIN Teams tm ON pt.team_id = tm.id
LEFT JOIN Users tml ON tml.team_id = tm.id AND tml.role_id = (SELECT id FROM Roles WHERE name = 'teamLider')
LEFT JOIN Milestones m ON p.id = m.project_id
LEFT JOIN Tasks t ON m.id = t.milestone_id
GROUP BY p.id, p.manager_id;

# raport całkowitego przeglądu projektów
CREATE OR REPLACE VIEW vw_ExecutiveOverview AS
SELECT
    p.id AS project_id,
    p.name AS project,
    p.status AS project_status,
    p.progress AS project_progress,
    CONCAT(pm.first_name, ' ', pm.last_name) AS project_manager,
    COUNT(DISTINCT pt.team_id) AS teams_involved,
    COUNT(DISTINCT u.id) AS employees_assigned,
    COUNT(DISTINCT m.id) AS milestones,
    COUNT(DISTINCT tsk.id) AS total_tasks,
    SUM(IF(tsk.status = 'zrobione', 1, 0)) AS tasks_done,
    SUM(IF(tsk.status = 'anulowane', 1, 0)) AS tasks_canceled,
    ROUND(SUM(IF(tsk.status = 'zrobione', 1, 0)) * 100.0 / NULLIF(COUNT(tsk.id), 0), 2) AS task_completion_rate,
    ROUND(AVG(m.progress), 2) AS avg_milestone_progress,
    COUNT(DISTINCT CASE WHEN m.deadline < CURDATE() AND m.progress < 100 THEN m.id END) AS overdue_milestones,
    COUNT(DISTINCT CASE WHEN tsk.deadline < CURDATE() AND tsk.status NOT IN ('zrobione', 'anulowane') THEN tsk.id END) AS overdue_tasks,
    GROUP_CONCAT(DISTINCT tsk.title SEPARATOR ', ') AS task_titles,
    GROUP_CONCAT(DISTINCT tm.name SEPARATOR ', ') AS involved_teams,
    GROUP_CONCAT(DISTINCT CONCAT(tml.first_name, ' ', tml.last_name, ' (', tm.name, ')') SEPARATOR '; ') AS team_leaders
FROM Projects p
LEFT JOIN Users pm ON p.manager_id = pm.id
LEFT JOIN ProjectTeams pt ON p.id = pt.project_id
LEFT JOIN Teams tm ON pt.team_id = tm.id
LEFT JOIN Users tml ON tml.team_id = tm.id AND tml.role_id = (SELECT id FROM Roles WHERE name = 'teamLider')
LEFT JOIN Users u ON tm.id = u.team_id
LEFT JOIN Milestones m ON p.id = m.project_id
LEFT JOIN Tasks tsk ON m.id = tsk.milestone_id
GROUP BY p.id;

CREATE OR REPLACE VIEW vw_Prezes_AllEmployees AS
SELECT
    u.id,
    u.first_name,
    u.last_name,
    r.name AS role,
    t.name AS team,
    u.hire_date,
    u.login,
    u.created_at,
    COUNT(DISTINCT ta.task_id) AS assigned_tasks_count,
    GROUP_CONCAT(DISTINCT p.name SEPARATOR ', ') AS projects_involved
FROM Users u
JOIN Roles r ON u.role_id = r.id
LEFT JOIN Teams t ON u.team_id = t.id
LEFT JOIN TaskAssignments ta ON u.id = ta.user_id
LEFT JOIN Tasks ts ON ta.task_id = ts.id
LEFT JOIN Milestones m ON ts.milestone_id = m.id
LEFT JOIN Projects p ON m.project_id = p.id
GROUP BY u.id;



CREATE OR REPLACE VIEW vw_Manager_Team AS
SELECT
    u.id,
    u.first_name,
    u.last_name,
    r.name AS role,
    t.name AS team,
    u.hire_date,
    COUNT(DISTINCT ta.task_id) AS assigned_tasks_count,
    GROUP_CONCAT(DISTINCT p.name SEPARATOR ', ') AS manager_projects,
    p_mgr.id AS manager_id
FROM Projects p_mgr
JOIN ProjectTeams pt ON p_mgr.id = pt.project_id
JOIN Teams t ON pt.team_id = t.id
JOIN Users u ON u.team_id = t.id
JOIN Roles r ON u.role_id = r.id
LEFT JOIN TaskAssignments ta ON u.id = ta.user_id
LEFT JOIN Tasks ts ON ta.task_id = ts.id
LEFT JOIN Milestones m ON ts.milestone_id = m.id
LEFT JOIN Projects p ON m.project_id = p.id
GROUP BY u.id, p_mgr.id;

CREATE OR REPLACE VIEW vw_TeamLeader_Squad AS
SELECT
    u.id,
    u.first_name,
    u.last_name,
    r.name AS role,
    u.hire_date,
    u.login,
    COUNT(DISTINCT ta.task_id) AS assigned_tasks_count,
    SUM(CASE WHEN ts.status = 'zrobione' THEN 1 ELSE 0 END) AS completed_tasks,
    SUM(CASE WHEN ts.status = 'wTrakcie' THEN 1 ELSE 0 END) AS in_progress_tasks,
    u.team_id
FROM Users u
JOIN Roles r ON u.role_id = r.id
LEFT JOIN TaskAssignments ta ON u.id = ta.user_id
LEFT JOIN Tasks ts ON ta.task_id = ts.id
WHERE u.team_id IS NOT NULL
GROUP BY u.id;
# select * from vw_TeamLeader_Squad;
# CREATE OR REPLACE VIEW vw_TeamLeader_Squad AS
# SELECT
#     u.id,
#     u.first_name,
#     u.last_name,
#     r.name AS role,
#     t.name AS team_name,  -- Dodano nazwę zespołu
#     u.hire_date,
#     u.login,
#     COUNT(DISTINCT ta.task_id) AS assigned_tasks_count,
#     SUM(CASE WHEN ts.status = 'zrobione' THEN 1 ELSE 0 END) AS completed_tasks,
#     SUM(CASE WHEN ts.status = 'wTrakcie' THEN 1 ELSE 0 END) AS in_progress_tasks,
#     u.team_id,
#     CONCAT(tl.first_name, ' ', tl.last_name) AS team_leader_name  -- Dodano lidera zespołu
# FROM Users u
# JOIN Roles r ON u.role_id = r.id
# LEFT JOIN Teams t ON u.team_id = t.id  -- Dołączanie danych zespołu
# LEFT JOIN Users tl ON tl.team_id = u.team_id
#     AND tl.role_id = (SELECT id FROM Roles WHERE name = 'teamLider')  -- Lider zespołu
# LEFT JOIN TaskAssignments ta ON u.id = ta.user_id
# LEFT JOIN Tasks ts ON ta.task_id = ts.id
# WHERE u.team_id IS NOT NULL
# GROUP BY u.id, t.name, tl.id;

CREATE OR REPLACE VIEW vw_UserCompleteDetails AS
SELECT
    u.id AS user_id,
    u.first_name,
    u.last_name,
    r.name AS role,
    t.name AS team,
    u.hire_date,
    u.login,
    u.created_at AS user_created_at,
    MAX(CONCAT(manager.first_name, ' ', manager.last_name)) AS manager_name,
    MAX(CONCAT(team_leader.first_name, ' ', team_leader.last_name)) AS team_leader_name,
    GROUP_CONCAT(DISTINCT p.name SEPARATOR ', ') AS projects_assigned,
    GROUP_CONCAT(DISTINCT m.name SEPARATOR ', ') AS milestones_assigned,
    GROUP_CONCAT(DISTINCT tas.title SEPARATOR ', ') AS tasks_assigned,
    COUNT(DISTINCT tas.id) AS total_tasks,
    SUM(tas.status = 'doZrobienia') AS todo,
    SUM(tas.status = 'wTrakcie') AS in_progress,
    SUM(tas.status = 'zrobione') AS done,
    SUM(tas.status = 'anulowane') AS canceled
FROM Users u
JOIN Roles r ON u.role_id = r.id
LEFT JOIN Teams t ON u.team_id = t.id
LEFT JOIN TaskAssignments ta ON u.id = ta.user_id
LEFT JOIN Tasks tas ON ta.task_id = tas.id
LEFT JOIN Milestones m ON tas.milestone_id = m.id
LEFT JOIN Projects p ON m.project_id = p.id
LEFT JOIN Users manager ON p.manager_id = manager.id
LEFT JOIN Users team_leader ON t.id = team_leader.team_id AND team_leader.role_id = (SELECT id FROM Roles WHERE name = 'teamLider')
GROUP BY u.id;



                    #============
                    # triggery
                    #============

# trigger do kontroli uprawnien podczas przypisywania zadan
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

# trigger do sprawdzania uprawnien podczas aktualizacji przypisywania zadan
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

# trigger do sprawdzania i zapewniania ze tylko projektmanager moze byc przypisany jak o manager
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

# trigger do automatycznego generowania powiadomien gdy zmieni sie termin wykonania zadania
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

# trigger automatycznie ustawia poziom uprawnien wedlug komorki z nazwa przy insercie
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

# trigger automatycznie ustawia poziom uprawnien wedlug komorki z nazwa przy aktualizacji
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

# trigger sprawdza i wyrzuca blad jezeli jezeli zadanie ma status anulowane a nie ma informacji anulowane przez kogo
CREATE TRIGGER Tasks_CheckCanceledBy_Insert
BEFORE INSERT ON Tasks
FOR EACH ROW
BEGIN
    IF NEW.status = 'anulowane' AND NEW.canceled_by IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Pole canceled_by musi być ustawione przy statusie anulowane.';
    END IF;
END //

# trigger do wymuszenia ustawienia pola canceled_by przy zmienie statusu
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

# trigger do automatycznej aktualizacji progresu projektu na podstawie statusu zadan
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
                       ROUND(SUM(IF(t.status = 'zrobione', 1, 0)) * 100 / NULLIF(COUNT(t.id), 0)),
            0
        )
        FROM Tasks t
        JOIN Milestones m ON t.milestone_id = m.id
        WHERE m.project_id = project_id_val
    )
    WHERE p.id = project_id_val;
END //
DELIMITER ;

# trigger do powiadomienia w okreslonym czasie przed uplywem terminu
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

# trigger do zapobiegania anulowania zadania przez uzytkownikow ze zbyt niskim poziomem uprawnienia
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

DELIMITER //
CREATE TRIGGER TaskStatusChangeNotification
AFTER UPDATE ON Tasks
FOR EACH ROW
BEGIN
    IF NEW.status <> OLD.status THEN
        INSERT INTO Notifications (task_id, user_id, type, message, created_at)
        SELECT
            NEW.id,
            ta.user_id,
            'aktualizacjaZadania',
            CONCAT('Status zadania "', NEW.title, '" zmieniono na: ', NEW.status),
            NOW()
        FROM TaskAssignments ta
        WHERE ta.task_id = NEW.id;
    END IF;
END //
DELIMITER ;
                    #============
                    #FUNKCJE
                    #============

# raport dla prezesa ze srednia wydajnoscia, zakonczonymi i anulowanymi zadaniami w okreslonym czasie, data poczatkowa i koncowa jest podawana
DELIMITER //
CREATE FUNCTION GenerateExecutiveReport(start_date DATE, end_date DATE)
RETURNS TEXT
BEGIN
    DECLARE report_text TEXT;

    SELECT CONCAT(
                   'Liczba aktywnych projektów: ', COUNT(p.id), '\n',
                   'Średnia wydajność: ', COALESCE(ROUND(AVG(p.progress)), '0'), '%\n',
                   'Zadania zakończone: ', COALESCE(SUM(IF(t.status = 'zrobione', 1, 0)), 0), '\n',
                   'Zadania anulowane: ', COALESCE(SUM(IF(t.status = 'anulowane', 1, 0)), 0)
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

DELIMITER //
CREATE TRIGGER SetTaskStatusOnInsert
BEFORE INSERT ON Tasks
FOR EACH ROW
BEGIN
    IF NEW.progress = 0 THEN
        SET NEW.status = 'doZrobienia';
    ELSEIF NEW.progress BETWEEN 1 AND 99 THEN
        SET NEW.status = 'wTrakcie';
    ELSEIF NEW.progress = 100 THEN
        SET NEW.status = 'zrobione';
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER SetTaskStatusOnUpdate
BEFORE UPDATE ON Tasks
FOR EACH ROW
BEGIN
    IF NEW.progress = 0 THEN
        SET NEW.status = 'doZrobienia';
    ELSEIF NEW.progress BETWEEN 1 AND 99 THEN
        SET NEW.status = 'wTrakcie';
    ELSEIF NEW.progress = 100 THEN
        SET NEW.status = 'zrobione';
    END IF;
END //
DELIMITER ;

                    #============
                    #PROCEDURY
                    #============
# procedura do generowania wydajnosci pracownika w tym jego statusy zadan
DELIMITER //
CREATE PROCEDURE GetEmployeePerformance(
    IN user_id INT,
    IN start_date DATE,
    IN end_date DATE
)
BEGIN
    SELECT
        CONCAT(u.first_name, ' ', u.last_name) AS employee,
        COUNT(t.id)                            AS total_tasks,
        SUM(IF(t.status = 'zrobione', 1, 0))   AS completed,
        SUM(IF(t.status = 'anulowane', 1, 0))  AS canceled,
        COALESCE(
            ROUND(
                (SUM(IF(t.status = 'zrobione', 1, 0))
                / NULLIF(COUNT(t.id), 0)) * 100, 2
            ), 0
        )                                      AS completion_rate
    FROM Users u
    LEFT JOIN TaskAssignments ta ON u.id = ta.user_id
    LEFT JOIN Tasks t ON ta.task_id = t.id
    WHERE u.id = user_id
      AND t.created_at BETWEEN start_date AND end_date
    GROUP BY u.id;
END //
DELIMITER ;

# inserty dla tabeli z rolami, przywileje uzulenia sie same
INSERT INTO Roles (name) VALUES
('prezes'),
('projektManager'),
('teamLider'),
('pracownik');

# insert dla zespolow
INSERT INTO Teams (name) VALUES
('Development'),
('Marketing'),
('Sales'),
('HR'),
('Support'),
('Design'),
('Testing'),
('Operations'),
('Finance'),
('Research');

# insert dla admina jako pierwszego uzytkownika
INSERT INTO Users (team_id, role_id, first_name, last_name, hire_date, login, password_hash, created_at)
VALUES (
    NULL,
    (SELECT id FROM Roles WHERE name = 'prezes'),
    'Admin',
    'Admin',
    '2020-01-01',
    'admin',
    'admin',
    NOW()
);

                    #===============================
                    #        inserty
                    #===============================



SELECT
    CAST(t.id AS CHAR) AS id,
    t.title,
    t.description,
    t.priority,
    t.status,
    CAST(t.progress AS CHAR) AS progress,
    DATE_FORMAT(t.created_at, '%Y-%m-%d %H:%i:%s') AS created_at,
    DATE_FORMAT(t.deadline, '%Y-%m-%d') AS deadline
FROM Tasks t;

SELECT
    CAST(t.id AS CHAR) AS id,
    t.title,
    t.description,
    t.priority,
    t.status,
    CAST(t.progress AS CHAR) AS progress,
    DATE_FORMAT(t.created_at, '%Y-%m-%d %H:%i:%s') AS created_at,
    DATE_FORMAT(t.deadline, '%Y-%m-%d') AS deadline
FROM Tasks t
JOIN Milestones m ON t.milestone_id = m.id
JOIN Projects p ON m.project_id = p.id
WHERE p.manager_id = ?;  -- ID menedżera


SELECT
    CAST(t.id AS CHAR) AS id,
    t.title,
    t.description,
    t.priority,
    t.status,
    CAST(t.progress AS CHAR) AS progress,
    DATE_FORMAT(t.created_at, '%Y-%m-%d %H:%i:%s') AS created_at,
    DATE_FORMAT(t.deadline, '%Y-%m-%d') AS deadline
FROM Tasks t
JOIN TaskAssignments ta ON t.id = ta.task_id
JOIN Users u ON ta.user_id = u.id
WHERE u.team_id = ?;  -- ID zespołu lidera

SELECT r.privilege_level
FROM Users u
JOIN Roles r ON u.role_id = r.id
WHERE u.id = ?;

SELECT
    CAST(t.id AS CHAR) AS id,
    t.title,
    t.description,
    t.priority,
    t.status,
    CAST(t.progress AS CHAR) AS progress,
    DATE_FORMAT(t.created_at, '%Y-%m-%d %H:%i:%s') AS created_at,
    DATE_FORMAT(t.deadline, '%Y-%m-%d') AS deadline,
    CONCAT(u.first_name, ' ', u.last_name) AS assigned_to
FROM Tasks t
LEFT JOIN TaskAssignments ta ON t.id = ta.task_id
LEFT JOIN Users u ON ta.user_id = u.id;



# Dodanie prezesa (admina)
INSERT INTO Users (team_id, role_id, first_name, last_name, hire_date, login, password_hash, created_at)
SELECT NULL, id, 'Jan', 'Nowak', '2020-01-01', 'prezes',
       'test', NOW()
FROM Roles WHERE name = 'prezes';

# Dodanie 5 projekt managerów
INSERT INTO Users (team_id, role_id, first_name, last_name, hire_date, login, password_hash, created_at)
SELECT NULL, id,
    CONCAT('Manager', n),
    CONCAT('Nazwisko', n),
    DATE_ADD('2020-01-01', INTERVAL n MONTH),
    CONCAT('pm', n),
    'test',
    NOW()
FROM (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) numbers
JOIN Roles WHERE name = 'projektManager';

# Dodanie 10 teamów
INSERT INTO Teams (name) VALUES
('Alpha'), ('Beta'), ('Gamma'), ('Delta'), ('Epsilon'),
('Zeta'), ('Eta'), ('Theta'), ('Iota'), ('Kappa');

# Dodanie 10 team liderów
INSERT INTO Users (team_id, role_id, first_name, last_name, hire_date, login, password_hash, created_at)
SELECT
    team_ids.id,
    (SELECT id FROM Roles WHERE name = 'teamLider'),
    CONCAT('Lider', idx),
    CONCAT('Teamowy', idx),
    DATE_ADD('2020-02-01', INTERVAL idx MONTH),
    CONCAT('tl', idx),
    'test',
    NOW()
FROM (SELECT 1 AS idx UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
      UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) indices
JOIN (SELECT id FROM Teams ORDER BY id) team_ids
ON indices.idx = team_ids.id;

# Dodanie 50 pracowników (po 5 na team)
INSERT INTO Users (team_id, role_id, first_name, last_name, hire_date, login, password_hash, created_at)
SELECT
    t.id,
    (SELECT id FROM Roles WHERE name = 'pracownik'),
    CONCAT('Pracownik', t.id, '_', nums.num),
    CONCAT('Zespołowy', t.id, '_', nums.num),
    DATE_ADD('2020-03-01', INTERVAL (t.id + nums.num) DAY),
    CONCAT('user', t.id, '_', nums.num),
    'test',
    NOW()
FROM Teams t
JOIN (SELECT 1 AS num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) nums;

# Dodanie 5 projektów
INSERT INTO Projects (manager_id, name, progress, status, start_date, end_date)
SELECT
    (SELECT id FROM Users WHERE login = CONCAT('pm', n)),
    CONCAT('Projekt ', n),
    0,
    'wTrakcie',
    DATE_ADD('2023-01-01', INTERVAL n MONTH),
    DATE_ADD('2023-07-01', INTERVAL n MONTH)
FROM (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) numbers;

# Przypisanie teamów do projektów (każdy manager ma 2 teamy)
INSERT INTO ProjectTeams (project_id, team_id)
SELECT p.id, t.id
FROM Projects p
JOIN Teams t ON (p.id = 1 AND t.id IN (1,2))
   OR (p.id = 2 AND t.id IN (3,4))
   OR (p.id = 3 AND t.id IN (5,6))
   OR (p.id = 4 AND t.id IN (7,8))
   OR (p.id = 5 AND t.id IN (9,10));

# Dodanie kamieni milowych (3 na projekt)
INSERT INTO Milestones (project_id, name, progress, description, deadline)
SELECT
    p.id,
    CONCAT('Milestone ', p.id, '.', m.n),
    ROUND(RAND() * 100),
    CONCAT('Opis kamienia ', p.id, '.', m.n),
    DATE_ADD(p.start_date, INTERVAL m.n MONTH)
FROM Projects p
JOIN (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3) m;

# Dodanie zadań
INSERT INTO Tasks (milestone_id, title, description, priority, progress, created_at, deadline)
SELECT
    m.id,
    CONCAT('Zadanie ', m.id, '.', t.n),
    CONCAT('Opis zadania ', m.id, '.', t.n),
    ELT(FLOOR(1 + RAND() * 3), 'niski', 'sredni', 'wysoki'),
    CASE
        WHEN t.n % 4 = 0 THEN 0
        WHEN t.n % 4 = 1 THEN 30
        WHEN t.n % 4 = 2 THEN 70
        ELSE 100
    END,
    DATE_SUB(m.deadline, INTERVAL FLOOR(15 + RAND() * 15) DAY),
    m.deadline
FROM Milestones m
JOIN (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) t;

# Przypisanie zadań do pracowników (każdy pracownik ma 4 zadania)
INSERT INTO TaskAssignments (task_id, assigned_by, user_id, assigned_at)
SELECT
    tasks.id,
    (SELECT id FROM Users WHERE team_id = u.team_id AND role_id = (SELECT id FROM Roles WHERE name = 'teamLider') LIMIT 1),
    u.id,
    NOW()
FROM Users u
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY RAND()) AS rn
    FROM Tasks
) tasks ON tasks.rn % (SELECT COUNT(*) FROM Users WHERE role_id = (SELECT id FROM Roles WHERE name = 'pracownik')) = u.id % (SELECT COUNT(*) FROM Users WHERE role_id = (SELECT id FROM Roles WHERE name = 'pracownik'))
WHERE u.role_id = (SELECT id FROM Roles WHERE name = 'pracownik')
LIMIT 200;

# Dodanie powiadomień (5 na pracownika, 3 nieprzeczytane i 2 przeczytane)
INSERT INTO Notifications (user_id, type, message, is_read, created_at)
SELECT
    u.id,
    CASE FLOOR(RAND() * 5)
        WHEN 0 THEN 'zadaniePrzypisane'
        WHEN 1 THEN 'aktualizacjaZadania'
        WHEN 2 THEN 'deadline'
        WHEN 3 THEN 'generowanieRaportu'
        ELSE 'inne'
    END,
    CONCAT('Treść powiadomienia ', n.n),
    CASE WHEN n.n <= 3 THEN FALSE ELSE TRUE END,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY)
FROM Users u
JOIN (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) n
WHERE u.role_id = (SELECT id FROM Roles WHERE name = 'pracownik');
