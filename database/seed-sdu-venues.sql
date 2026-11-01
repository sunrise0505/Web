-- 课程演示预约单元。容量和时间不是学校真实运营参数。
-- 校区字段迁移后执行；按校区和名称跳过已存在项，不覆盖现有配置。
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '中心校区体育馆 · 羽毛球区','羽毛球','中心校区','中心校区 · 体育馆','室内羽毛球预约单元，请自备球拍、穿着运动鞋。',4,8,22,1,'court' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='中心校区' AND name='中心校区体育馆 · 羽毛球区');
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '中心校区风雨操场 · 篮球区','篮球','中心校区','中心校区 · 风雨操场','室内篮球预约单元，适合小组训练与班级活动。',20,8,22,1,'court' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='中心校区' AND name='中心校区风雨操场 · 篮球区');
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '洪家楼校区体育馆 · 羽毛球区','羽毛球','洪家楼校区','洪家楼校区 · 体育馆','羽毛球训练与双打活动预约单元。',4,8,22,1,'court' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='洪家楼校区' AND name='洪家楼校区体育馆 · 羽毛球区');
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '趵突泉校区体育馆 · 篮球区','篮球','趵突泉校区','趵突泉校区 · 体育馆','篮球训练预约单元，请遵守场馆使用秩序。',20,8,22,1,'court' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='趵突泉校区' AND name='趵突泉校区体育馆 · 篮球区');
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '千佛山校区风雨操场 · 篮球区','篮球','千佛山校区','千佛山校区 · 风雨操场','室内篮球预约单元，供训练和班级体育活动使用。',20,8,22,1,'court' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='千佛山校区' AND name='千佛山校区风雨操场 · 篮球区');
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '兴隆山校区风雨操场 · 篮球区','篮球','兴隆山校区','兴隆山校区 · 风雨操场','篮球训练预约单元，请按预约时段入场。',20,8,22,1,'court' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='兴隆山校区' AND name='兴隆山校区风雨操场 · 篮球区');
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '兴隆山校区风雨操场 · 羽毛球区','羽毛球','兴隆山校区','兴隆山校区 · 风雨操场','羽毛球预约单元，请自备球拍与球。',4,8,22,1,'court' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='兴隆山校区' AND name='兴隆山校区风雨操场 · 羽毛球区');
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '青岛校区综合体育馆 · 篮球区','篮球','青岛校区','青岛校区 · 综合体育馆篮排球馆','篮球训练预约单元，适合小组训练与友谊赛。',20,8,22,1,'court' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='青岛校区' AND name='青岛校区综合体育馆 · 篮球区');
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '青岛校区综合体育馆 · 游泳馆','游泳','青岛校区','青岛校区 · 综合体育馆游泳馆','游泳预约单元，请携带泳帽和泳镜。',30,9,21,1,'pool' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='青岛校区' AND name='青岛校区综合体育馆 · 游泳馆');
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '青岛校区综合体育馆 · 健身大厅','健身','青岛校区','青岛校区 · 综合体育馆健身大厅','有氧与力量训练预约单元，器械使用后请归位。',12,8,22,1,'studio' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='青岛校区' AND name='青岛校区综合体育馆 · 健身大厅');

-- 补充软件园和威海校区；演示参数，非官方预约配置。
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '软件园校区羽毛球场','羽毛球','软件园校区','软件园校区 · 羽毛球场','室内羽毛球预约单元，请自备球拍、穿着运动鞋。',4,8,22,1,'court' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='软件园校区' AND name='软件园校区羽毛球场');
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '威海校区体育馆 · 羽毛球区','羽毛球','威海校区','威海校区 · 体育馆','羽毛球训练与双打活动，请自备球拍与球。',4,8,22,1,'court' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='威海校区' AND name='威海校区体育馆 · 羽毛球区');
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '威海校区篮球场','篮球','威海校区','威海校区 · 篮球场','适合小组训练与班级篮球活动，请按预约时间入场。',20,8,22,1,'court' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='威海校区' AND name='威海校区篮球场');
INSERT INTO venue (name,category,campus,location,description,capacity,open_hour,close_hour,enabled,theme)
SELECT '威海校区体育中心 · 游泳馆','游泳','威海校区','威海校区 · 体育中心游泳馆','游泳预约区域，请携带泳帽和泳镜。',30,9,21,1,'pool' WHERE NOT EXISTS (SELECT 1 FROM venue WHERE campus='威海校区' AND name='威海校区体育中心 · 游泳馆');
