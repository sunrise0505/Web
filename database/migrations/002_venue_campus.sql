-- 仅在旧库缺少 campus 列时执行一次；本轮未在学校数据库执行。
ALTER TABLE venue ADD COLUMN campus VARCHAR(20) NOT NULL DEFAULT '中心校区' AFTER category;
-- 旧数据的位置以校区名称开头时，保留原有归属。
UPDATE venue SET campus = CONCAT(SUBSTRING_INDEX(location, '校区', 1), '校区') WHERE location REGEXP '^(中心|洪家楼|趵突泉|千佛山|兴隆山|软件园|威海|青岛)校区';
