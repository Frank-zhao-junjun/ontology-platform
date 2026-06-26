# Phase 4 Sprint Plan — ObjectType 补齐 + VM 校验 + EPC profiles

> 启动: 2026-06-26

---

## Phase A — UpdateObjectType 补齐字段

| # | 任务 | 说明 |
|:-:|------|------|
| A1 | `UpdateObjectTypeRequest` 添加新字段 | 补齐 entity 已定义但 request 缺失的字段 |
| A2 | `ObjectTypeServiceImpl.updateObjectType()` 处理新字段 | 补全 setter 调用 |
| A3 | `OntologyServiceImpl.createObjectType()` 改用 7 参数 create | 传入 entityRole + businessScenarioId |
| A4 | `OntologyServiceImpl.toObjectTypeResponse()` 补齐字段 | 映射所有 entity 字段到 response |
| A5 | `OntologyServiceImpl.toObjectTypeDetailResponse()` 补齐字段 | 同上 |
| A6 | 创建 V19 Flyway 迁移 | 新增/变更表结构 |
| A7 | 补充单元测试 | createObjectType / updateObjectType |
| A8 | `OntologyServiceImpl.updateObjectType` 补齐 | 与 ObjectTypeServiceImpl 同步 |

## Phase B — buildEpcModelFromV1 profiles 补齐

创建 `buildEpcModelFromV1` 方法，补齐 EPC profile 映射，含测试。

## Phase C — StructuralValidator + VM 规则

创建 `StructuralValidator` 校验器，实现 VM (Validation Metrics) 规则，含测试。

## Final

`mvn compile` + `mvn test` 全量回归。
