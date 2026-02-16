INSERT INTO groups (name, color) VALUES
('Products', '#D39CB5'),
('Issues', '#D4987E'),
('Campaign', '#8FB896');

INSERT INTO users (username, password, role, email) VALUES
('admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5LNE1P3e/9dZYkTfLZAlJ4Kq5P9z.', 'ROLE_ADMIN', 'admin@example.com'),
('User_A', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5LNE1P3e/9dZYkTfLZAlJ4Kq5P9z.', 'ROLE_USER', 'user_a@example.com'),
('User_B', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5LNE1P3e/9dZYkTfLZAlJ4Kq5P9z.', 'ROLE_USER', 'user_b@example.com'),
('User_C', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5LNE1P3e/9dZYkTfLZAlJ4Kq5P9z.', 'ROLE_USER', 'user_c@example.com');

INSERT INTO todos (author, assignee, title, detail, created_at, updated_at, status, priority, start_date, due_date, group_id, user_id) VALUES
('Admin', 'Admin', 'Task 01', 'Sample task 01.', '2026-02-02 09:00:00', '2026-02-02 09:00:00', 'NOT_STARTED', 'MEDIUM', '2026-02-01', '2026-02-03', 1, 1),
('Admin', 'User_A', 'Task 02', 'Sample task 02.', '2026-02-02 09:10:00', '2026-02-02 09:10:00', 'IN_PROGRESS', 'LOW', '2026-02-02', '2026-02-04', 2, 2),
('Admin', 'Admin', 'Task 03', 'Sample task 03.', '2026-02-02 09:20:00', '2026-02-02 09:20:00', 'COMPLETED', 'HIGH', '2026-02-02', '2026-02-05', 3, 1),
('User_C', 'User_A', 'Task 04', 'Sample task 04.', '2026-02-02 09:30:00', '2026-02-02 09:30:00', 'NOT_STARTED', 'MEDIUM', '2026-02-03', '2026-02-06', 1, 4),
('User_C', 'User_C', 'Task 05', 'Sample task 05.', '2026-02-02 09:40:00', '2026-02-02 09:40:00', 'NOT_STARTED', 'LOW', '2026-02-04', '2026-02-07', 3, 4),
('User_C', 'User_C', 'Task 06', 'Sample task 06.', '2026-02-02 09:50:00', '2026-02-02 09:50:00', 'COMPLETED', 'HIGH', '2026-02-05', '2026-02-08', 3, 4),
('User_C', 'User_C', 'Task 07', 'Sample task 07.', '2026-02-02 10:00:00', '2026-02-02 10:00:00', 'IN_PROGRESS', 'MEDIUM', '2026-02-06', '2026-03-09', 1, 4),
('User_C', 'User_B', 'Task 08', 'Sample task 08.', '2026-02-02 10:10:00', '2026-02-02 10:10:00', 'NOT_STARTED', 'LOW', '2026-02-03', '2026-02-10', 3, 3),
('User_C', 'User_B', 'Task 09', 'Sample task 09.', '2026-02-02 10:20:00', '2026-02-02 10:20:00', 'COMPLETED', 'HIGH', '2026-02-08', '2026-02-11', 3, 3),
('User_C', 'User_B', 'Task 10', 'Sample task 10.', '2026-02-02 10:30:00', '2026-02-02 10:30:00', 'NOT_STARTED', 'MEDIUM', '2026-02-09', '2026-02-12', 1, 3),
('User_C', 'User_A', 'Task 11', 'Sample task 11.', '2026-02-02 10:40:00', '2026-02-02 10:40:00', 'NOT_STARTED', 'LOW', '2026-02-10', '2026-02-13', 2, 2),
('User_C', 'User_C', 'Task 12', 'Sample task 12.', '2026-02-02 10:50:00', '2026-02-02 10:50:00', 'COMPLETED', 'HIGH', '2026-02-15', '2026-03-14', 3, 4),
('User_B', 'User_B', 'Task 13', 'Sample task 13.', '2026-02-02 11:00:00', '2026-02-02 11:00:00', 'IN_PROGRESS', 'MEDIUM', '2026-02-12', '2026-02-15', 1, 3),
('User_B', 'User_B', 'Task 14', 'Sample task 14.', '2026-02-02 11:10:00', '2026-02-02 11:10:00', 'NOT_STARTED', 'LOW', '2026-02-13', '2026-02-20', 2, 3),
('User_C', 'User_B', 'Task 15', 'Sample task 15.', '2026-02-02 11:20:00', '2026-02-02 11:20:00', 'COMPLETED', 'HIGH', '2026-02-14', '2026-02-17', 3, 3),
('User_C', 'User_C', 'Task 16', 'Sample task 16.', '2026-02-02 11:30:00', '2026-02-02 11:30:00', 'NOT_STARTED', 'MEDIUM', '2026-02-15', '2026-03-10', 1, 4),
('User_C', 'User_C', 'Task 17', 'Sample task 17.', '2026-02-02 11:40:00', '2026-02-02 11:40:00', 'NOT_STARTED', 'LOW', '2026-02-12', '2026-02-19', 2, 4),
('User_A', 'User_A', 'Task 18', 'Sample task 18.', '2026-02-02 11:50:00', '2026-02-02 11:50:00', 'COMPLETED', 'HIGH', '2026-02-17', '2026-02-20', 3, 2),
('User_A', 'User_B', 'Task 19', 'Sample task 19.', '2026-02-02 12:00:00', '2026-02-02 12:00:00', 'NOT_STARTED', 'MEDIUM', '2026-02-18', '2026-02-21', 1, 2),
('User_B', 'User_B', 'Task 20', 'Sample task 20.', '2026-02-02 12:10:00', '2026-02-02 12:10:00', 'NOT_STARTED', 'LOW', '2026-02-22', '2026-03-02', 2, 3),
('User_B', 'User_B', 'Task 21', 'Sample task 21.', '2026-02-02 12:20:00', '2026-02-02 12:20:00', 'COMPLETED', 'HIGH', '2026-02-20', '2026-02-23', 3, 3),
('User_A', 'User_A', 'Task 22', 'Sample task 22.', '2026-02-02 12:30:00', '2026-02-02 12:30:00', 'NOT_STARTED', 'MEDIUM', '2026-02-21', '2026-03-10', 1, 2),
('Admin', 'User_A', 'Task 23', 'Sample task 23.', '2026-02-02 12:40:00', '2026-02-02 12:40:00', 'NOT_STARTED', 'LOW', '2026-02-22', '2026-03-10', 2, 1),
('User_A', 'User_A', 'Task 24', 'Sample task 24.', '2026-02-02 12:50:00', '2026-02-02 12:50:00', 'COMPLETED', 'HIGH', '2026-02-22', '2026-03-10', 3, 2),
('Admin', 'Admin', 'Task 25', 'Sample task 25.', '2026-02-02 13:00:00', '2026-02-02 13:00:00', 'NOT_STARTED', 'MEDIUM', '2026-02-22', '2026-03-15', 1, 1);
