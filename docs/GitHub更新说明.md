# 手动更新 GitHub 上的项目

本轮只修改本地，没有自动更新 https://github.com/sunrise0505/Web 。

## 本轮网页上传

1. 打开项目的 `artifacts/github-update` 文件夹。这是与上次源码上传副本比较后整理出的本轮变化，只有需要新增或覆盖的文件；没有 `.env`、依赖、JAR、本地数据库或真实密码。
2. 打开 GitHub 仓库，在 Code 页确认分支为 `main`，选择 Add file → Upload files。
3. 选中 `github-update` 里面的全部内容，拖到网页上传区域。拖入的是内部的 `frontend`、`backend`、`database`、`docs` 等目录及 `README.md`，不要把最外层 `github-update` 文件夹拖进去。
4. 保留目录结构。上传清单应显示如 `frontend/src/views/VenuesView.vue`、`database/migrations/002_venue_campus.sql`，并直接覆盖仓库原路径。不要上传 ZIP。
5. 填写说明“更名为山东大学场馆预约系统，增加多校区目录与筛选”，点击 Commit changes，并确认提交。
6. 返回仓库首页，确认 README 标题已变化，且能找到新的校区迁移文件和场馆目录类。

更新文件夹会继续包含自最初上传版本以来的累计变化，直到你确认某一版本已经上传后再更新比较基准。如果上一次校区版本尚未上传，直接使用当前文件夹即可；若已上传，相同路径更新不会多出副本。此次也补充了软件园、威海场馆及老师演示包的生成脚本。

本轮没有修改上次补齐的 `.gitignore` 和 `.env.example`，不需要再用文本副本改名。网页没有这些变更之前，队友下载到的仍是上一个版本。

## 本地验证与数据库

队友拉取新版后，可用 demo 模式在本机验证。学校库仍是旧结构，不能直接让新版使用 mysql；需要先备份、执行缺失的校区迁移，再补充场馆数据，详见 README。

GitHub 保存源码，不会自动运行 Spring Boot 或自动升级学校数据库。后续多人协作建议各自使用 Git 分支并通过 Pull Request 合并；网页上传适用于此次少量文件更新。上传前先确认队友没有改动相同文件，避免覆盖对方更新。
