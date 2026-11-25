-- Departments table
CREATE TABLE IF NOT EXISTS departments (
                                           id VARCHAR(36) PRIMARY KEY,
                                           version BIGINT NOT NULL DEFAULT 0,
                                           code VARCHAR(50) NOT NULL UNIQUE,
                                           name VARCHAR(255) NOT NULL,
                                           description TEXT,
                                           parent_department_id VARCHAR(36),
                                           manager_employee_id VARCHAR(36),
                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           active BOOLEAN DEFAULT TRUE
);

-- Employees table
CREATE TABLE IF NOT EXISTS employees (
                                         id VARCHAR(36) PRIMARY KEY,
                                         version BIGINT NOT NULL DEFAULT 0,
                                         employee_number VARCHAR(50) NOT NULL UNIQUE,
                                         first_name VARCHAR(100) NOT NULL,
                                         last_name VARCHAR(100) NOT NULL,
                                         middle_name VARCHAR(100),
                                         email VARCHAR(255) UNIQUE,
                                         phone VARCHAR(20),
                                         position VARCHAR(255),
                                         department_id VARCHAR(36),
                                         role VARCHAR(20) DEFAULT 'EMPLOYEE',
                                         manager_employee_id VARCHAR(36),
                                         telegram_chat_id VARCHAR(100),
                                         calendar_integration_enabled BOOLEAN DEFAULT TRUE,
                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         active BOOLEAN DEFAULT TRUE,
                                         FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- Employee skills
CREATE TABLE IF NOT EXISTS employee_skills (
                                               employee_id VARCHAR(36),
                                               skill VARCHAR(100),
                                               PRIMARY KEY (employee_id, skill),
                                               FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Offices table
CREATE TABLE IF NOT EXISTS offices (
                                       id VARCHAR(36) PRIMARY KEY,
                                       version BIGINT NOT NULL DEFAULT 0,
                                       code VARCHAR(50) NOT NULL UNIQUE,
                                       name VARCHAR(255) NOT NULL,
                                       address TEXT,
                                       city VARCHAR(100),
                                       country VARCHAR(100),
                                       timezone VARCHAR(50),
                                       work_start_time TIME,
                                       work_end_time TIME,
                                       max_booking_duration_hours INTEGER,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       active BOOLEAN DEFAULT TRUE
);

-- Equipment table
CREATE TABLE IF NOT EXISTS equipment (
                                         id VARCHAR(36) PRIMARY KEY,
                                         version BIGINT NOT NULL DEFAULT 0,
                                         inventory_number VARCHAR(50) NOT NULL UNIQUE,
                                         name VARCHAR(255) NOT NULL,
                                         model VARCHAR(100),
                                         manufacturer VARCHAR(100),
                                         type VARCHAR(50),
                                         description TEXT,
                                         quantity INTEGER DEFAULT 1,
                                         is_portable BOOLEAN DEFAULT FALSE,
                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         active BOOLEAN DEFAULT TRUE
);

-- Rooms table
CREATE TABLE IF NOT EXISTS rooms (
                                     id VARCHAR(36) PRIMARY KEY,
                                     version BIGINT NOT NULL DEFAULT 0,
                                     room_number VARCHAR(50) NOT NULL,
                                     name VARCHAR(255) NOT NULL,
                                     floor INTEGER,
                                     capacity INTEGER,
                                     office_id VARCHAR(36) NOT NULL,
                                     type VARCHAR(50),
                                     has_projector BOOLEAN DEFAULT FALSE,
                                     has_whiteboard BOOLEAN DEFAULT FALSE,
                                     has_tv_screen BOOLEAN DEFAULT FALSE,
                                     has_video_conference BOOLEAN DEFAULT FALSE,
                                     description TEXT,
                                     is_accessible BOOLEAN DEFAULT TRUE,
                                     requires_approval BOOLEAN DEFAULT FALSE,
                                     approver_employee_id VARCHAR(36),
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     active BOOLEAN DEFAULT TRUE,
                                     FOREIGN KEY (office_id) REFERENCES offices(id)
);

-- Room equipment mapping
CREATE TABLE IF NOT EXISTS room_equipment (
                                              room_id VARCHAR(36),
                                              equipment_id VARCHAR(36),
                                              PRIMARY KEY (room_id, equipment_id),
                                              FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
                                              FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_employees_department ON employees(department_id);
CREATE INDEX idx_employees_email ON employees(email);
CREATE INDEX idx_employees_employee_number ON employees(employee_number);
CREATE INDEX idx_rooms_office ON rooms(office_id);
CREATE INDEX idx_offices_city ON offices(city);