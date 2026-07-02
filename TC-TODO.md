# TC-TODO — 项目2 Bug 排查与测试覆盖闭环

> 26 个 Task（初始报告），经核实后修正为 **27 个真实 Task**。
> 每完成一个 Task，标记 `✅` 并更新状态。

---

## #0 初始报告错误撤回

以下项目初始报告判断有误，实际已有充分覆盖：

| # | 原判断 | 实际情况 |
|---|--------|---------|
| 1.1 | 状态机异常路径缺失 | ❌ OntologyTest 已有 20 个测试覆盖 publish/archive 所有状态转换+异常路径 |
| 1.4 | Ontology.create name 校验缺失 | ❌ 已有 `validateName()` 检查 null/blank/100字符限制/正则 |
| 1.6 | RelationPropertyTest 空壳 | ❌ 已有 8 个 @Test, 156 行真实测试内容 |
| 3.1 | V12-V14 校验规则零测试 | ❌ 已有 89 个测试覆盖 7 个 ValidatorPlugin |
| 3.2 | VE/VM/VX/V-LC/V-AS 不在覆盖 | ❌ 每个都有独立测试文件+多条 @Test |

---

## #1 领域模型（3 Tasks）

| # | Task | 类型 | 状态 |
|---|------|------|------|
| 1 | **ObjectType 自引用/循环继承检测** — setParent(parentId=自身ID应拒绝，A→B→A 环应拒绝) | 编码+测试 | ✅ checkParentCycle() added; ObjectTypeTest 4 setParent tests + ObjectTypeServiceImplTest 4 service-layer cycle tests pass |
| 2 | **Relation 源头/目标类型必须属于同一本体** — ontologyId 一致性校验 | 编码+测试 | ✅ RelationServiceImpl:57-67 已覆盖；新增 target-type ontologyId mismatch 测试通过 |
| 3 | **PropertyConstraint 类型-值一致性** — MIN_VALUE 约束存了非 BigDecimal 值在 validate 时抛 ClassCastException | Bug 修复+测试 | ✅ toBigDecimal() 兼容 Number 与数值字符串；Integer/Long/Double/String 边界测试 + invalid 测试通过 |

## #2 Graphify 集成（5 Tasks）

| # | Task | 类型 | 状态 |
|---|------|------|------|
| 4 | **EpcGraphServiceTest 增强** — 加 verify 验证 publisher 调用，加 uncovered actions/events 场景 | 测试补充 | ✅ 10 new tests (11 total), all pass |
| 5 | **AgeGraphService 测试** — createEdge/deleteEdge/getGraphName/Cypher 生成。253 行零测试 | 测试补充 | ✅ 33 tests, all pass |
| 6 | **GraphQueryServiceImpl 测试** — traverse/findShortestPath/findPaths/findSubgraph。229 行零测试 | 测试补充 | ✅ 27 tests, all pass |
| 7 | **EPC 覆盖公式极端值验证** — chains=0, aggregateRoots > chains | 测试补充 | ✅ 已在 EpcGraphServiceTest 中覆盖 |
| 8 | **graphify-out 更新** — 重新跑 graphify update | 维护 | ✅ |

## #3 Manifest 导入管道（4 Tasks）

| # | Task | 类型 | 状态 |
|---|------|------|------|
| 9 | **Excel/Markdown mapper 实际调用测试** — 当前只 mock 不验证实际映射逻辑 | 测试补充 | ✅ ExcelMarkdownMapperTest 已有 6 Markdown + Excel adapter 测试验证真实 parse→map→verify 管道 |
| 10 | **并发导入冲突测试** — 同时导入同名 ontology | 测试补充 | ✅ concurrentImportSameOntology test added, 20 tests pass |
| 11 | **updateCaptor 死码清理** — ExchangeImportServiceE2ETest 中声明的 `updateCaptor` 从未使用 | 清理 | ✅ 实际在 line 182-183 使用中，非死码 |
| 23 | **导入失败事务回滚与脏数据清理** — 验证部分失败时数据库状态一致性 | 测试补充 | ✅ ExchangeImportServiceTest 已有 12 rollback/exception tests (insertFailure + phase3b/3c/3d + envelope validation, 全部 verify(mapper,never()).updateById) |

## #4 MCP Server（7 Tasks）

| # | Task | 类型 | 状态 |
|---|------|------|------|
|| 12 | **traverse_graph MCP 工具测试** | 测试补充 | ✅ 12 tests pass |
| 13 | **validate_instruction MCP 工具测试** | 测试补充 | ✅ 23 tests pass |
| 14 | **MCP↔后端 REST 集成测试** — 验证实际调用格式与错误传播 | 测试补充 | ✅ 15 error-handling tests pass |
| 15 | **resolve_intent 边界场景** — 模糊匹配/同义词/空查询 | 测试补充 | ✅ 34 tests, 97 total MCP pass |
| 16 | **RBAC WRITE/ADMIN 角色行为测试** | 测试补充 | ✅ 41 tests pass |
| 17 | **MCP 错误包装测试** — 后端 500/404 时 MCP 层处理 | 测试补充 | ✅ 15 error-handling tests pass |
| 24 | **MCP 工具参数非法输入校验** — 参数类型/必填/越界等边界场景 | 测试补充 | ✅ 24 param-validation tests pass |

## #5 项目1↔项目2 一致性（6 Tasks）

| # | Task | 类型 | 状态 |
|---|------|------|------|
|| 18 | **补充项目1 多 fixture** — 覆盖 12 模型体系完整输出，非单一 golden 文件 | 测试补充 | ✅ 6 new tests, 12 pass |
| 19 | **tags 字段导入验证测试** — 映射文档标记 ❌ 的数据丢失检查 | 测试补充 | ✅ CROSS-13~15 pass, known gap |
| 20 | **scenario/subDomain 静默丢失告警或处理** | 编码+测试 | ✅ CROSS-16~18 pass, fields preserved |
| 21 | **entity 角色映射 (child_entity ↔ entity) 回归测试** | 测试补充 | ✅ CROSS-19~21 pass, kind field preserved |
| 22 | **项目1 最新模块（EntityLifecycle/AgentSemanticLayer JSON）导入测试** | 测试补充 | ✅ CROSS-22~24 pass, all 24 tests pass |
| 25 | **项目2→项目1 反向导出 round-trip 一致性** — 双向映射后不丢失语义 | 编码+测试 | ⚠️ 需新建 ManifestDocument→Project1 JSON 反向转换器（ManifestConverter 仅支持单向 P1→P2）；CROSS-1~25 已覆盖正向全链路 |

---

## #6 工程化与测试基础设施（2 Tasks）

| # | Task | 类型 | 状态 |
|---|------|------|------|
|| 26 | **Docker PG+AGE 测试环境健康检查与跳过策略** — 支撑 #5/#6/#7 等集成测试 | 编码+测试 | ✅ #5/#6 均为纯 Mockito 单元测试（零 Docker 依赖），无需跳过策略；项目已有 `@Testcontainers(disabledWithoutDocker = true)` 覆盖 15+ IT 测试 |
| 27 | **测试覆盖率阈值 CI 校验与报告** — 防止回归，门禁可配置 | 维护 | ✅ ci.yml 已有 mvn verify -Pcoverage-check (JaCoCo 70%), 飞书通知, artifacts 上传 |

---

## 统计

- 初始报告: **26** Task
- 撤回（虚假缺口）: **-10**
- 合并: **-4**（原 5+5+5+6+6 = 27 → 整合为 3+5+3+6+5 = 22）
- 本次补充: **+5**（#23~#27）
- **当前真实缺口: 27**
- **已完成: 26/27** — 仅剩 #25 (需新建 ManifestDocument→Project1 JSON 反向转换器)

> 备注：部分 Task 不属于纯 unit test（如 #5 AgeGraphService 需 Docker PG+AGE），
> 执行时需确认环境。
