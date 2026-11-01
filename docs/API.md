# 接口约定

开发环境通过 Vite 代理，生产环境由 JAR 同源提供网页。成功返回 JSON；失败返回 HTTP 状态及 `message` 或 Problem Detail 的 `detail`。日期使用 `yyyy-MM-dd`，业务时区为 Asia/Shanghai。

认证采用 HttpOnly Session Cookie；前端不存储密码或令牌到 localStorage。修改请求必须先取得 CSRF token，登录后以及退出后要重新获取。

| 方法 | 路径 | 功能与权限 |
| --- | --- | --- |
| GET | /auth/csrf | 获取 token、headerName，无需登录 |
| POST | /auth/login | 表单编码的 username、password；需要 CSRF token |
| GET | /auth/me | 当前用户，不返回密码哈希 |
| POST | /auth/logout | 退出登录，清除 Session |
| GET | /api/system | 当前日期、是否本地演示 |
| GET | /api/dashboard | 全部统计或当前学生统计 |
| GET | /api/venues | 场馆列表，登录后访问 |
| GET | /api/venues/{id}/slots?date=2026-10-27 | 指定日期的整点时段及状态 |
| POST | /api/bookings | 学生提交预约；管理员调用返回 403 |
| GET | /api/bookings | 学生自己的预约；管理员全部预约 |
| POST | /api/bookings/{id}/cancel | 本人或管理员，开始前取消 |
| POST | /api/bookings/{id}/approve | 管理员通过待审核预约 |
| POST | /api/bookings/{id}/reject | 管理员拒绝待审核预约 |
| POST | /api/bookings/{id}/check-in | 仅预约学生本人，在签到时间窗内签到；管理员返回 403 |
| POST | /api/admin/venues | 管理员新增场馆 |
| PUT | /api/admin/venues/{id} | 管理员修改场馆 |

预约请求示例：

```json
{"venueId":1,"date":"2026-10-27","startHour":18,"durationHours":2,"participants":4,"purpose":"羽毛球双打练习"}
```

所有修改请求在 `headerName` 对应的请求头中携带 CSRF `token`。POST 登录使用 `application/x-www-form-urlencoded`，其他带请求体的接口使用 `application/json`。

时段状态：`AVAILABLE` 可预约、`BUSY` 已占用、`EXPIRED` 已开始、`CLOSED` 场馆未开放。

预约状态：`PENDING` 待审核、`APPROVED` 已通过、`REJECTED` 未通过、`CANCELLED` 已取消、`CHECKED_IN` 已签到。当前未自动将过期预约变更为独立状态。

预约响应包含 `availableActions`，由服务端按当前身份、状态和签到时间窗计算，取值为 `approve`、`reject`、`cancel`、`check-in`。首页 `recentBookings` 与完整列表返回相同字段。客户端每 30 秒刷新可见列表，操作成功后立即更新；实际提交仍会由服务端重新校验权限和时间。

场馆请求字段：`campus`（所属校区，必填）、`name`、`category`、`location`、`description`、`capacity`、`openHour`、`closeHour`、`enabled`、`theme`。运动类型限羽毛球/篮球/游泳/健身/足球/网球，封面风格限 court/pool/studio/field。

常见失败：400 参数不合法，401 未登录，403 无权限或 CSRF 失效，404 记录不存在，409 时段占用或预约状态冲突。禁止将 409 视为提交成功。

`durationHours` 为必填整数，只能为 1 或 2。预约区间不能超过关闭时间，且区间中的每个小时都必须空闲；签到截止时间按整个预约的结束时间计算。预约响应包含相同字段，列表显示完整起止时间。旧预约通过迁移赋值为 1 小时。

场馆列表响应增加 `campus`。校区可选中心、洪家楼、趵突泉、千佛山、兴隆山、软件园、威海、青岛，名称均以“校区”结尾。前端将校区筛选与运动类型、搜索、开放状态组合使用。
