-- 在课程分配的 java_1_11 数据库执行。只创建项目表，不创建或删除数据库。
-- 不包含示例账号；首次初始化账号见 README 的 APP_SEED_ENABLED 说明。
CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(40) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  display_name VARCHAR(40) NOT NULL,
  role VARCHAR(16) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS venue (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(60) NOT NULL,
  category VARCHAR(20) NOT NULL,
  location VARCHAR(100) NOT NULL,
  description VARCHAR(500) NOT NULL,
  capacity INT NOT NULL,
  open_hour INT NOT NULL,
  close_hour INT NOT NULL,
  enabled BIT NOT NULL,
  theme VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS booking (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  venue_id BIGINT NOT NULL,
  booking_date DATE NOT NULL,
  start_hour INT NOT NULL,
  duration_hours INT NOT NULL DEFAULT 1,
  participants INT NOT NULL,
  purpose VARCHAR(200) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  checked_in_at DATETIME(6) NULL,
  CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES app_user(id),
  CONSTRAINT fk_booking_venue FOREIGN KEY (venue_id) REFERENCES venue(id),
  INDEX idx_booking_slot (venue_id, booking_date, start_hour)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
