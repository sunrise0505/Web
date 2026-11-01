# 山东大学场馆预约管理系统

面向山东大学不同校区的课程实验项目。学生按校区和运动类型查询场馆，提交 1 或 2 小时的预约，审核通过后由本人签到；管理员维护场馆和校区归属，处理预约审核与取消。

这是课程演示系统。场馆名称参考学校公开资料；运动区域拆分、人数上限、开放时间和预约规则均为实验配置，不代表学校真实运营安排，也没有对接学校统一身份认证或正式场馆预约平台。

## 当前运行状态

- 本轮修改仅在本地实施，未修改学校数据库，未上传学校提交网站，也未自动更新 GitHub。
- 本机 `.env` 已切换为 `demo`，预览使用独立本地 H2 数据库。之前学校第 11 组 MySQL 的数据保留。
- 新增 `venue.campus` 校区字段。学校库尚未执行本轮迁移，不能直接用新版代码连接旧结构；准备好后再按下文升级。
- 校区目录包含 14 个演示预约单元，覆盖中心、洪家楼、趵突泉、千佛山、兴隆山、软件园、威海、青岛八个校区。软件园羽毛球场沿用中心校区羽毛球区的介绍、容量、时间和配色。
- 本地旧库会补充缺少的目录场馆，保留原有场馆配置及预约。因此升级后的总数可能超过 14，不会清空旧数据。

## 主要功能

- 学生、管理员登录及退出；学生仅查看自己的预约，管理员查看全体预约。
- 校区、运动类型、开放状态组合筛选，支持场馆名称、位置和校区搜索。
- 整点开始，可预约今天至未来第 14 天，连续 1 或 2 小时；用途从下拉菜单选择。
- 完整预约区间必须空闲，且不能超过场馆关闭时间；校验参与人数和过去时段。
- 待审核与已通过的预约占用时段，取消或拒绝后释放整个区间。
- 管理员审核预约，不能提交个人预约，也不能代学生签到。
- 仅预约学生本人可在开始前 15 分钟至预约结束前签到。
- 首页最近预约可直接处理，操作后更新统计；另有完整预约记录、状态筛选与搜索。
- 管理员新增、编辑场馆，设置校区、类型、位置、容量、时间、配色与开放状态。
- 暂停场馆或调整时间不会自动取消已有预约，需另行处理。

当前一个预约单元同一时段只允许一份团体预约。多块独立球场应分别建档。暂未实现注册、账号维护、通知、支付、导出、图片上传或过期预约自动处理。

## 技术与目录

Java 21、Spring Boot 3.5.16、Spring Security、Spring Data JPA；Vue 3、TypeScript、Vite、Vue Router。课程部署使用 MySQL，本地演示与自动化测试使用 H2。

```text
frontend/src/views/       登录、工作台、场馆、预约记录、场馆管理页面
frontend/src/components/ 共用布局、预约弹窗、预约表格和运动图标
backend/src/main/java/   接口、实体、权限、业务事务及场馆初始化目录
backend/src/main/resources/ 配置与打包后的网页
backend/src/test/        独立测试库中的集成测试
database/              建表、迁移与多校区场馆补充脚本
docs/                  系统设计、接口、课程核对、报告提纲与场馆资料依据
scripts/               启动、打包与源码压缩脚本
```

## 本地启动

准备 JDK 21、Maven 3.6.3+、Node.js 22.12+ 与 npm。Mac 脚本会优先选择 Homebrew JDK 21，其他系统请自行设置 `JAVA_HOME`。

项目根目录运行：

```bash
bash scripts/start-backend.sh
```

另一个终端运行：

```bash
npm --prefix frontend ci
npm --prefix frontend run dev
```

打开 <http://localhost:3000>。后端端口为 `22222`。启动脚本读取根目录 `.env`；本轮建议使用 `SPRING_PROFILES_ACTIVE=demo`。未配置 `.env` 时开发默认也使用 demo。H2 数据保存在 `backend/data/`，与学校库独立。

| 身份 | 账号 | 初始密码 |
| --- | --- | --- |
| 管理员 | admin | Admin123! |
| 学生甲 | student | Student123! |
| 学生乙 | student2 | Student123! |

初始化程序不会覆盖已有账号密码。登录页可选择身份自动填入测试账号。

## MySQL 配置与旧库升级

学校第 11 组连接参数模板在 `.env.example`，实际密码单独交接，不放入 GitHub。数据库名、用户名均为 `java_1_11`，服务器 `202.194.14.120:3306`。

新空库执行 `database/schema.sql`，再配置 `SPRING_PROFILES_ACTIVE=mysql` 和数据库密码。首次可临时设置 `APP_SEED_ENABLED=true` 初始化账号和校区场馆，完成后改回 `false`。

已有旧库不能重新初始化或删表，应先备份、检查列结构，再按需升级：

1. 缺少 `booking.duration_hours` 才执行 `database/migrations/001_booking_duration.sql`。学校第 11 组此前已执行这一项，不要重复执行。
2. 缺少 `venue.campus` 才执行 `database/migrations/002_venue_campus.sql`。本轮未在学校库执行。
3. 执行 `database/seed-sdu-venues.sql` 补充多校区演示预约单元，按校区与名称跳过已有项，不覆盖原配置。
4. 将本地 `.env` 切换为 `mysql` 后启动并验证。MySQL 模式使用 `ddl-auto: validate`，不会自动修改表结构。

学校数据库接入不等于将应用部署到学校服务器；目前仍未提交学校网站。

## 测试与打包

```bash
npm --prefix frontend run build
cd backend
mvn test
```

测试使用独立内存库，不连接学校库，覆盖权限、校区维护、时长、冲突、签到、取消与并发预约等。

项目根目录运行：

```bash
bash scripts/package.sh
```

脚本构建前端，将网页复制到后端 `resources/public`，运行测试，生成 `backend/target/java-server-1.0-SNAPSHOT.jar`。课程 JAR 默认选择 mysql；仅在本地演示时显式运行：

```bash
java -jar backend/target/java-server-1.0-SNAPSHOT.jar --spring.profiles.active=demo
```

打开 <http://localhost:22222>。直接运行 JAR 不会自动读取项目根目录 `.env`；连接 MySQL 时需先加载环境变量。前端生产请求使用 `/api`、`/auth` 同源地址，Vite 代理只在开发时使用。

生成课程源码包：

```bash
bash scripts/source-zip.sh
```

输出 `artifacts/campus-venue-source.zip`，排除 node_modules、dist、JAR、本地数据库、截图及真实密码配置。报告 11 项、成员贡献及不超过 5 分钟的 H.264 视频仍需按实际完成情况补齐。

## 组员协作

仓库地址：<https://github.com/sunrise0505/Web>。先克隆仓库，在各自分支开发；修改前拉取最新版本，完成后提交并通过 Pull Request 合并。

本地代码修改不会自动同步到 GitHub。本轮手动网页更新步骤见 `docs/GitHub更新说明.md`。场馆资料依据及演示设置边界见 `docs/场馆资料说明.md`。

## 给老师的本地运行包

运行 `bash scripts/package-demo.sh` 会重新构建并生成 `artifacts/sdu-venue-demo.zip`。包中包含集成前端的 JAR、Windows 与 Mac/Linux 启动文件和运行说明，不包含真实数据库密码或已有用户数据。

老师需安装 Java 21，解压到可写目录后，Windows 双击 `start-windows.bat`；Mac 可双击 `start-mac.command`，或在终端运行 `bash start-mac.command`；Linux 运行 `bash start-linux.sh`。保持窗口开启，在浏览器打开 <http://127.0.0.1:22222>。无需单独安装 Node、Maven 或 MySQL。首次启动自动创建本地演示库、三个账号、14 个场馆及演示预约，后续重启保留数据。

演示包与课程源码包分别提供。课程要求源码包不包含 JAR；在线验收仍需指定 JAR、学校 MySQL、结构迁移与连接密码，不能用 H2 演示包替代。给老师运行的详细步骤见 `docs/老师运行说明.md`。
