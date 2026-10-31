# 场间 校园场馆预约管理系统

Web 技术实验基础项目。学生浏览场馆、选择时段并提交预约；管理员维护场馆、审核预约；审核通过后可签到或在开始前取消。包含可运行的前后端、MySQL 建表脚本及课程报告提纲。

当前应用在本机运行，已于 2026-10-31 按用户要求接入学校第 11 组 MySQL 数据库。尚未上传学校提交网站或部署到课程应用服务器。

## 技术与目录

- 后端：Java 21、Spring Boot 3.5.16、Spring Security、Spring Data JPA。
- 前端：Vue 3、TypeScript、Vite、Vue Router。
- 数据库：课程部署使用 MySQL；本地演示使用 H2 文件数据库，二者配置分离。
- 后端端口 `22222`，前端开发端口 `3000`；生产前端由同一个 JAR 提供。

```text
frontend/       Vue 源码、路由、页面和样式
backend/        Spring Boot 接口、业务、实体及集成测试
database/       MySQL 建表脚本
docs/           接口说明、系统设计、实验报告提纲
scripts/        启动、打包和源码 ZIP 脚本
```

## 本地启动

准备 JDK 21、Maven 3.6.3+、Node.js 22.12+ 和 npm。Mac 脚本会自动查找 Homebrew JDK 21，也可以自行设置 `JAVA_HOME`。建议使用 Node.js 22 或更新的 LTS 版本。

终端一，在项目根目录运行：

```bash
bash scripts/start-backend.sh
```

脚本优先读取根目录 `.env`。当前本机已配置 `mysql`，启动后读写学校数据库；未配置 `.env` 时默认启用 `demo`，使用独立 H2 文件库。H2 首次启动自动创建体验账号、6 个场馆和演示预约，数据保存在 `backend/data/`，重启后保留。两套库的数据独立，切换数据库不会迁移已有演示记录。

终端二，在项目根目录运行：

```bash
npm --prefix frontend ci
npm --prefix frontend run dev
```

打开 <http://localhost:3000>。学生与管理员分别显示自己的功能；右上角退出登录后可切换身份。

| 身份 | 账号 | 初始密码 |
| --- | --- | --- |
| 管理员 | admin | Admin123! |
| 学生甲 | student | Student123! |
| 学生乙，用于验证数据隔离 | student2 | Student123! |

账号由种子程序创建，初始化时可使用 `ADMIN_PASSWORD`、`STUDENT_PASSWORD` 环境变量覆盖；不会改动已有账号。当前提供的是体验账号，暂不包含注册、重置密码和用户管理。

## 课程 MySQL 配置

按当前团队编号 11 提供默认库名及用户名 `java_1_11`，请以课程实际分配为准。

1. 登录课程 MySQL，在分配的数据库执行 `database/schema.sql`，创建本项目三张表。
2. 复制根目录 `.env.example` 为 `.env`，填写课程数据库密码。该文件不应上传或提交。
3. 首次启动将 `APP_SEED_ENABLED=true`，创建初始账号和场馆。完成后设回 `false`。MySQL 配置不会生成历史演示预约。
4. 使用 `bash scripts/start-backend.sh` 启动，脚本会读取根目录 `.env`。

已有旧库升级两小时预约功能时，先确认缺少 `duration_hours` 列，再执行一次 `database/migrations/001_booking_duration.sql`；学校第 11 组库已完成该升级，历史预约保留为 1 小时。本地旧 H2 库由 demo 模式自动更新结构。

MySQL 配置使用 `ddl-auto: validate`，启动时检查表结构，不会自动修改课程数据库，也不会默认连接上学期的库。当前第 11 组库已建表，初始化了 3 个账号和 6 个场馆；已通过真实 MySQL 的登录、预约、审核、取消及释放时段验证。初始化开关已在 `.env` 中设为 `false`。详见 `docs/课程要求核对.md`。

### 打包与课程上传

```bash
bash scripts/package.sh
```

脚本先构建前端，再复制到 `backend/src/main/resources/public`，运行后端测试，以 Maven `course` profile 打包并生成：

```text
backend/target/java-server-1.0-SNAPSHOT.jar
```

本地验证整个 JAR：

```bash
cd backend
java -jar target/java-server-1.0-SNAPSHOT.jar --spring.profiles.active=demo
```

打开 <http://localhost:22222>。这里显式选择独立本地演示库。课程打包的 JAR 默认使用 MySQL，未提供课程密码时将拒绝启动，避免误上传 H2 演示配置。

课程服务器运行时必须启用 `mysql` 配置并提供数据库密码。若服务器允许设置启动参数：

```bash
SPRING_PROFILES_ACTIVE=mysql DB_PASSWORD='实际密码' java -jar java-server-1.0-SNAPSHOT.jar
```

若课程平台只支持上传 JAR、不能设置环境变量，则在上传前自行将 `application-mysql.yml` 的密码占位符替换为课程密码，重新运行课程打包脚本。在本地用同样配置验证后再上传；含密码的配置不应进入源码 ZIP。也可通过 Maven 命令行 `-Dspring-boot.run.profiles=mysql` 在本地联调。

前端所有请求均使用 `/api` 和 `/auth` 同源路径，生产访问地址由实际部署页面决定。`vite.config.ts` 的代理只用于开发，不会写入生产包；调整该代理不能改变生产包的后端地址。这样将前端集成到 JAR 后，无需在浏览器请求中写死 `localhost` 或服务器 IP。[Vite 官方代理说明](https://vite.dev/config/server-options.html#server-proxy)

### 提交源码

```bash
bash scripts/source-zip.sh
```

输出 `artifacts/campus-venue-source.zip`，含前后端、数据库脚本和文档，排除依赖、构建产物、JAR、本地数据及 `.env`。如果为课程打包临时在 YAML 写入了密码，先还原为占位符再生成源码 ZIP。

每组还需提交不超过 5 分钟的 H.264 演示视频、完整报告；组员个人填写贡献。报告框架在 `docs/实验报告提纲.md`，最终内容、团队分工、进度和截图需按实际项目补齐。

## 已实现的基础流程

- 登录、退出、身份切换入口；基于 Session Cookie 的服务端鉴权和 CSRF 防护。
- 角色导航：学生提交和查看自己的预约；管理员查看全体预约、审核和维护场馆，不能以管理员身份提交个人预约。
- 场馆名称/位置搜索、运动分类、开放状态筛选、场馆新增与编辑。
- 查询今天起 14 天内的时段；整点开始，可选连续 1 或 2 小时，校验完整区间的开放时间、占用情况、人数与过去时段；预约用途使用下拉选择。
- 待审核和已通过预约均占用时段；取消、拒绝后释放时段。
- 管理员审核，预约开始前 15 分钟至结束前仅允许预约学生本人签到，管理员不能代签。
- 基于真实数据库记录的统计卡片、近 7 天预约柱图与最近预约。
- 首页最近预约和完整记录共用操作组件，可直接审核、签到和取消；操作后更新状态及首页统计。签到按钮根据服务端返回的可用操作启用。
- 场馆使用按运动类型区分的 SVG 图标，场馆管理可调整配色。
- 同场馆写操作使用数据库行锁与 READ_COMMITTED 隔离级别，避免同一时段并发重复预约。

## 验证

```bash
npm --prefix frontend run build
cd backend
mvn test
```

集成测试覆盖未登录访问、CSRF、登录会话、角色权限、预约记录隔离、日期/人数/时段校验、取消与拒绝释放时段、签到时间窗、并发冲突。测试使用独立内存库，不连接课程数据库。

## 后续开发

基础框架尚未包含用户注册与账号维护、节假日开放策略、自动处理过期预约、通知、导出、图片上传及细分球场。当前每条场馆记录在一个时段只允许一份团体预约，多块独立球场应分别建档。没有预约费用与支付功能。

项目设计及实体关系见 `docs/系统设计.md`，接口约定见 `docs/API.md`。技术版本与 Java 21 兼容性可参考 [Spring Boot 官方要求](https://docs.spring.io/spring-boot/3.5/system-requirements.html)。
