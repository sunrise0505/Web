# 手动更新 GitHub 上的项目

仓库：https://github.com/sunrise0505/Web 。用户已确认此前的 29 个多校区更新文件上传完成。本轮学校数据库接入后，只需补充 7 个发生变化的文件。

## 本次上传

`artifacts/github-update` 已精简，仅包含：

- `README.md`
- `database/migrations/002_venue_campus.sql`
- `docs/系统设计.md`
- `docs/课程要求核对.md`
- `docs/GitHub更新说明.md`
- `docs/场馆资料说明.md`
- `docs/老师运行说明.md`

打开仓库 Code 页，选择 Add file → Upload files，把 `github-update` 里面的内容拖入上传区域，保持原有目录结构，不要拖入最外层 `github-update` 文件夹。填写说明“更新学校数据库接入状态与运行说明”，点击 Commit changes。

相同路径会更新原文件并保留提交历史，不会生成第二份代码。若上传文件夹的最外层目录，才可能产生不需要的额外目录。GitHub 页面允许查看提交差异，上传前可检查这些路径。

## 数据库与协作

学校第 11 组库已完成校区字段迁移并补充场馆，当前本机使用 mysql。队友连接学校库需单独配置实际密码；选择 demo 则使用独立本地库。已经执行过的数据库迁移不要重复执行。

不要上传 `.env`、数据库备份、JAR、本地 data、node_modules 或 dist。GitHub 上的源码更新不会自动修改学校数据库，也不代表已向学校网站提交作业。

后续多人开发建议使用 Git 分支和 Pull Request 合并；每次上传前与上一次已提交版本比较，只上传有变化的文件。
