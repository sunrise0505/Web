-- 仅适用于旧库：先确认 booking 尚无 duration_hours 列，再执行一次。
-- 保留已有预约，将历史预约按原规则设置为 1 小时。
ALTER TABLE booking ADD COLUMN duration_hours INT NOT NULL DEFAULT 1 AFTER start_hour;
