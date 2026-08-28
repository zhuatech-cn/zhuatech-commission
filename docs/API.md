# 企业佣金与销售激励管理系统 API

所有业务接口默认位于 `/api`，除 `/public/**` 和健康检查外均需要 HTTP Basic 身份认证。生产环境应接入企业 IAM 或统一身份平台。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/public/about` | 产品、公司、官网和许可元数据 |
| GET | `/catalog` | 业务模块、字段标签和状态动作 |
| GET | `/dashboard` | 业务规模、金额、状态和模块统计 |
| GET/POST | `/records` | 业务台账查询与创建 |
| GET/PUT/DELETE | `/records/{id}` | 详情、草稿修改与删除 |
| POST | `/records/{id}/actions` | 执行服务端状态迁移 |
| POST | `/records/{id}/comments` | 增加协作记录 |
| GET | `/records/{id}/timeline` | 查询完整操作时间线 |
| GET | `/records/search` | 组合检索、分页和逾期筛选 |
| GET | `/records/export.csv` | 导出 UTF-8 CSV |
| GET | `/sla-summary` | SLA、逾期、风险和人员工作量 |
| POST | `/domain/decision` | 执行企业佣金与销售激励管理系统专属领域规则 |
| GET/POST | `/commission/runs` | 佣金批次查询与创建 |
| POST | `/commission/runs/{id}/calculate` | 执行阶梯、加速和封顶计算 |
| GET / PUT | `/commission/runs/{id}/allocations` | 查询或调整多人佣金归属 |
| POST | `/commission/runs/{id}/submit` | 通过治理门禁后提交复核 |
| POST | `/admin/commission/runs/{id}/approve` | 管理员批准佣金结果 |
| POST | `/commission/runs/{id}/payout` | 登记佣金发放回执 |
| POST | `/commission/runs/{id}/clawback` | 执行受余额限制的佣金追索 |
| GET/POST | `/enterprise/controls` | 企业控制项查询与幂等创建 |
| GET | `/enterprise/workbench` | 按组织与账期查询治理驾驶舱 |
| GET | `/enterprise/period-status` | 查询组织账期锁定状态 |
| POST | `/enterprise/controls/bulk-submit` | 原子批量提交控制项 |
| POST | `/enterprise/controls/{id}/submit` | 提交复核 |
| POST | `/admin/enterprise/controls/bulk-review` | 管理员原子批量审批或驳回 |
| POST | `/admin/enterprise/controls/{id}/review` | 管理员审批或驳回 |
| PUT | `/admin/enterprise/period-lock` | 管理员锁定或解锁组织账期 |
| POST | `/enterprise/controls/{id}/documents` | 登记附件哈希及存储元数据 |
| POST | `/enterprise/controls/{id}/complete` | 凭证完整后办结 |
| POST | `/admin/enterprise/controls/{id}/sync` | 登记外部系统回执 |

## 领域决策字段

| 字段 | 类型 | 含义 |
| --- | --- | --- |
| `planNo` | String | 激励方案编号 |
| `eligibleRevenue` | double | 有效业绩金额 |
| `calculatedCommission` | double | 计算佣金 |
| `capAmount` | double | 方案封顶金额 |
| `disputedItems` | int | 争议事项数 |
| `planApproved` | boolean | 方案已审批 |
| `dataLocked` | boolean | 业绩数据已锁定 |
| `complianceChecked` | boolean | 合规检查通过 |
| `clawbackPending` | boolean | 存在待追索事项 |

接口统一返回 `ApiResponse`；业务冲突使用 HTTP 409，参数错误使用 400，未认证使用 401，无权限使用 403。
