#!/usr/bin/env python3
"""
项目1 → 项目2 导入适配器
==========================
将 Ontology 设计工具 (项目1) 的 JSON/YAML 导出，
转换为 Ontology-platform (项目2) 的 REST API 调用。

用法:
  python import_from_project1.py --url http://localhost:8080/api \\
      --file export.json --token xxxxx

  # 从 manifest JSON 直接导入
  python import_from_project1.py --manifest manifest.json --url ...

  # 从 XLSX 导入
  python import_from_project1.py --xlsx export.xlsx --url ...
"""

import argparse, json, sys, os, uuid
from datetime import datetime, timezone
import urllib.request
import re

# ============================================================
# 类型映射
# ============================================================
TYPE_MAP = {
    'string': 'STRING', 'text': 'STRING', 'varchar': 'STRING',
    'number': 'FLOAT', 'integer': 'INTEGER', 'int': 'INTEGER',
    'float': 'FLOAT', 'double': 'FLOAT',
    'boolean': 'BOOLEAN', 'bool': 'BOOLEAN',
    'date': 'DATE', 'datetime': 'DATE', 'time': 'DATE',
    'object': 'JSON', 'array': 'JSON', 'json': 'JSON',
    'enum': 'ENUM', 'ref': 'REFERENCE',
}


def map_data_type(t: str) -> str:
    return TYPE_MAP.get((t or 'string').lower().strip(), 'STRING')


def clean_url(url: str) -> str:
    """Strip tracking params from CSDN/article URLs."""
    if url and '?' in url:
        return url.split('?')[0]
    return url or ''


# ============================================================
# 解析器: 从项目1的 JSON content 提取结构化数据
# ============================================================
def parse_project1_export(data: dict) -> dict:
    """解析项目1的导出JSON，返回结构化字典。"""
    result = {
        'version': data.get('version', '0.0.0'),
        'exported_at': data.get('exported_at'),
    }

    # 各维度
    result['structural'] = data.get('structural') or data.get('data', {}).get('structural', {})
    result['behavioral'] = data.get('behavioral') or data.get('data', {}).get('behavioral', {})
    result['rules'] = data.get('rules') or data.get('data', {}).get('rules', {})
    result['events'] = data.get('events') or data.get('data', {}).get('events', {})
    result['interfaces'] = data.get('interfaces') or data.get('data', {}).get('interfaces', {})
    result['epc'] = data.get('epc') or data.get('data', {}).get('epc', {})
    result['domains'] = data.get('domains', [])

    return result


# ============================================================
# 导入器: 调用 Ontology-platform REST API
# ============================================================
class PlatformImporter:
    """调用 Ontology-platform 的 REST API 导入数据。"""

    def __init__(self, base_url: str, token: str = ""):
        self.base_url = base_url.rstrip('/')
        self.token = token
        self.headers = {
            'Content-Type': 'application/json',
        }
        if token:
            self.headers['Authorization'] = f'Bearer {token}'

    def _api_call(self, method: str, path: str, data: dict = None) -> dict:
        """执行 REST API 调用。"""
        url = f"{self.base_url}{path}"
        body = json.dumps(data).encode('utf-8') if data else None
        req = urllib.request.Request(url, data=body, headers=self.headers, method=method)
        try:
            with urllib.request.urlopen(req, timeout=30) as resp:
                return json.loads(resp.read().decode('utf-8'))
        except urllib.error.HTTPError as e:
            err_body = e.read().decode('utf-8', errors='replace')
            print(f"  [ERROR] HTTP {e.code} {path}: {err_body[:200]}")
            return {'code': e.code, 'message': str(e)}
        except Exception as e:
            print(f"  [ERROR] {path}: {e}")
            return {'code': 0, 'message': str(e)}

    def _get(self, path: str) -> dict:
        return self._api_call('GET', path)

    def _post(self, path: str, data: dict) -> dict:
        return self._api_call('POST', path, data)

    # ---- 本体 ----

    def create_ontology(self, name: str, display_name: str = "",
                        description: str = "") -> dict:
        payload = {
            'name': re.sub(r'[^a-zA-Z0-9_-]', '_', name.lower()),
            'displayName': display_name or name,
            'description': description or '',
        }
        result = self._post('/api/v1/ontologies', payload)
        if result.get('code') == 200 or result.get('data'):
            oid = result['data'].get('id') or result['data'].get('name')
            print(f"  [本体] 创建成功: {name} (id={oid})")
            return result['data']
        # 可能已存在
        return {'name': payload['name'], 'id': payload['name']}

    # ---- 对象类型 ----

    def create_object_type(self, ontology_id: str, name: str,
                           description: str = "", parent_id: str = None) -> dict:
        payload = {
            'ontologyId': ontology_id,
            'name': name,
            'displayName': name,
            'description': description or '',
        }
        if parent_id:
            payload['parentId'] = parent_id
        result = self._post(f'/api/v1/ontologies/{ontology_id}/object-types', payload)
        if result.get('data'):
            return result['data']
        return {'name': name, 'id': name}

    # ---- 属性 ----

    def create_property(self, ontology_id: str, object_type_id: str,
                        name: str, data_type: str = "STRING",
                        required: bool = False, unique: bool = False) -> dict:
        payload = {
            'name': name.replace(' ', '_').lower(),
            'displayName': name,
            'dataType': map_data_type(data_type),
            'isRequired': required,
            'isUnique': unique,
        }
        result = self._post(
            f'/api/v1/ontologies/{ontology_id}/object-types/{object_type_id}/properties',
            payload
        )
        return result.get('data') or {}

    # ---- 关系 ----

    def create_relation(self, ontology_id: str, source: str, target: str,
                        rel_type: str, inverse_of: str = "") -> dict:
        # source/target 是 entity name，需转为 object_type_id
        payload = {
            'sourceTypeName': source,
            'targetTypeName': target,
            'name': rel_type,
            'displayName': rel_type,
            'cardinality': '1:N',
            'reverseName': inverse_of or '',
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/relations', payload)
        return result.get('data') or {}

    # ---- 行为 ----

    def create_action(self, ontology_id: str, name: str,
                      input_schema: str = "", output_schema: str = "",
                      domain: str = "") -> dict:
        payload = {
            'name': name,
            'displayName': name,
            'actionType': 'CUSTOM',
            'inputSchema': json.loads(input_schema) if input_schema else {},
            'outputSchema': json.loads(output_schema) if output_schema else {},
            'domain': domain or '',
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/actions', payload)
        return result.get('data') or {}

    # ---- 事件 ----

    def create_event(self, ontology_id: str, name: str,
                     severity: str = "INFO", source: str = "") -> dict:
        payload = {
            'name': name,
            'displayName': name,
            'eventType': 'DOMAIN',
            'severity': (severity or 'INFO').upper(),
            'source': source or '',
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/events', payload)
        return result.get('data') or {}

    # ---- EPC ----

    def create_epc_step(self, ontology_id: str, flow_name: str,
                        step_order: int, trigger_event: str = "",
                        action: str = "", conditions: list = None,
                        guards: list = None) -> dict:
        payload = {
            'flowName': flow_name,
            'stepOrder': step_order,
            'eventTrigger': trigger_event,
            'actionName': action,
            'conditions': conditions or [],
            'guards': guards or [],
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/epc-steps', payload)
        return result.get('data') or {}

    # ---- 校验规则 (ValidationRule) ----

    def create_validation(self, ontology_id: str, name: str,
                          rule_type: str = "CUSTOM", entity: str = "",
                          field: str = "", expression: str = "") -> dict:
        payload = {
            'ruleName': name,
            'ruleType': rule_type.upper(),
            'entityId': entity,
            'fieldName': field,
            'expression': expression or '',
            'enabled': True,
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/validations', payload)
        return result.get('data') or {}

    # ---- API定义 (ApiDefinition) ----

    def create_api_def(self, ontology_id: str, name: str,
                       url: str = "", method: str = "GET") -> dict:
        payload = {
            'apiName': name,
            'url': clean_url(url),
            'httpMethod': method.upper(),
            'enabled': True,
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/apis', payload)
        return result.get('data') or {}

    # ---- 护栏规则 (GuardrailRule) ----

    def create_guardrail(self, ontology_id: str, name: str,
                         condition: str = "", action: str = "BLOCK") -> dict:
        payload = {
            'ruleName': name,
            'conditionExpr': condition or '',
            'actionType': action.upper(),
            'enabled': True,
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/guardrails', payload)
        return result.get('data') or {}

    # ---- 策略 (PolicyRule) ----

    def create_policy(self, ontology_id: str, name: str,
                      policy_type: str = "ACCESS", effect: str = "ALLOW") -> dict:
        payload = {
            'policyName': name,
            'policyType': policy_type.upper(),
            'effect': effect.upper(),
            'enabled': True,
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/policies', payload)
        return result.get('data') or {}

    # ---- 探针 (ProbeDefinition) ----

    def create_probe(self, ontology_id: str, name: str,
                     target: str = "", probe_type: str = "HTTP") -> dict:
        payload = {
            'probeName': name,
            'target': target or '',
            'probeType': probe_type.upper(),
            'enabled': True,
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/probes', payload)
        return result.get('data') or {}

    # ---- 查询 (QueryDefinition) ----

    def create_query(self, ontology_id: str, name: str,
                     template: str = "", query_type: str = "CUSTOM") -> dict:
        payload = {
            'queryName': name,
            'queryTemplate': template or '',
            'queryType': query_type.upper(),
            'enabled': True,
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/queries', payload)
        return result.get('data') or {}

    # ---- 计算 (ComputeDefinition) ----

    def create_compute(self, ontology_id: str, name: str,
                       formula: str = "", output_type: str = "NUMBER") -> dict:
        payload = {
            'computeName': name,
            'formula': formula or '',
            'outputType': output_type.upper(),
            'enabled': True,
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/compute', payload)
        return result.get('data') or {}

    # ---- 通知 (NotificationDefinition) ----

    def create_notification(self, ontology_id: str, name: str,
                            channel: str = "EMAIL", template: str = "") -> dict:
        payload = {
            'notifName': name,
            'channel': channel.upper(),
            'template': template or '',
            'enabled': True,
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/notifications', payload)
        return result.get('data') or {}

    # ---- 报表 (ReportDefinition) ----

    def create_report(self, ontology_id: str, name: str,
                      report_format: str = "TABLE") -> dict:
        payload = {
            'reportName': name,
            'reportFormat': report_format.upper(),
            'enabled': True,
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/reports', payload)
        return result.get('data') or {}

    # ---- 指标 (IndicatorDefinition) ----

    def create_indicator(self, ontology_id: str, name: str,
                         formula: str = "", agg_type: str = "COUNT") -> dict:
        payload = {
            'indicatorName': name,
            'formula': formula or '',
            'aggregationType': agg_type.upper(),
            'enabled': True,
        }
        result = self._post(f'/api/v1/ontologies/{ontology_id}/indicators', payload)
        return result.get('data') or {}


# ============================================================
# 导入主流程
# ============================================================
def import_export(importer: PlatformImporter, parsed: dict, domain_name: str = "default"):
    """将解析后的项目1数据导入 Ontology-platform。"""

    # 1. 创建本体
    meta = parsed.get('domains', [{}])[0] if parsed.get('domains') else {}
    onto = importer.create_ontology(
        name=domain_name,
        display_name=meta.get('name', domain_name),
        description=meta.get('description', ''),
    )
    onto_id = onto.get('id', domain_name)
    print(f"\n=== 导入到本体: {domain_name} (id={onto_id}) ===\n")

    # 2. 导入静态结构 (实体 + 属性 + 关系)
    structural = parsed.get('structural', {})
    entities = structural.get('entities', [])

    # 2a. 创建对象类型
    type_map = {}  # entity.name -> object_type_id
    for ent in entities:
        ot = importer.create_object_type(
            onto_id, ent.get('name', 'unknown'),
            description=ent.get('description', ''),
        )
        type_map[ent.get('name')] = ot.get('id', ent.get('name'))

    # 2b. 创建属性
    for ent in entities:
        ot_id = type_map.get(ent.get('name'))
        for attr in ent.get('attributes', []):
            importer.create_property(
                onto_id, ot_id,
                name=attr.get('name', attr.get('id', 'unknown')),
                data_type=attr.get('type', 'string'),
                required=attr.get('required', False),
                unique=attr.get('unique', False),
            )

    # 2c. 创建关系
    for rel in structural.get('relations', []):
        importer.create_relation(
            onto_id,
            source=rel.get('source', ''),
            target=rel.get('target', ''),
            rel_type=rel.get('type', 'relates_to'),
            inverse_of=rel.get('inverseOf', ''),
        )

    # 3. 导入动态行为
    behavioral = parsed.get('behavioral', {})
    for action in behavioral.get('actions', []):
        importer.create_action(
            onto_id,
            name=action.get('name', action.get('id', 'unknown')),
            input_schema=action.get('input', '{}'),
            output_schema=action.get('output', '{}'),
            domain=action.get('domain', ''),
        )

    # 4. 导入事件
    events_data = parsed.get('events', {})
    for evt in events_data.get('eventTypes', []):
        importer.create_event(
            onto_id,
            name=evt.get('name', evt.get('id', 'unknown')),
            severity=evt.get('severity', 'INFO'),
            source=evt.get('source', ''),
        )

    # 5. 导入 EPC
    epc_data = parsed.get('epc', {})
    for i, step in enumerate(epc_data.get('steps', []), 1):
        importer.create_epc_step(
            onto_id,
            flow_name=step.get('flow_name', epc_data.get('flow_name', 'default_flow')),
            step_order=step.get('step_order', i),
            trigger_event=step.get('event_trigger', ''),
            action=step.get('action', ''),
            conditions=step.get('conditions', []),
            guards=step.get('guards', []),
        )

    # 6. 导入规则约束
    rules = parsed.get('rules', {})
    for v in rules.get('validations', []):
        importer.create_validation(onto_id, name=v.get('name', v.get('id', 'v_unknown')),
            rule_type=v.get('type', 'CUSTOM'), entity=v.get('entity', ''),
            field=v.get('field', ''), expression=v.get('expression', ''))
    for g in rules.get('guardrails', []):
        importer.create_guardrail(onto_id, name=g.get('name', g.get('id', 'g_unknown')),
            condition=g.get('condition', ''), action=g.get('action', 'BLOCK'))
    for p in rules.get('policies', []):
        importer.create_policy(onto_id, name=p.get('name', p.get('id', 'p_unknown')),
            policy_type=p.get('type', 'ACCESS'), effect=p.get('effect', 'ALLOW'))
    for pr in rules.get('probes', []):
        importer.create_probe(onto_id, name=pr.get('name', pr.get('id', 'probe_unknown')),
            target=pr.get('target', ''), probe_type=pr.get('probeType', 'HTTP'))

    # 7. 导入外部接口
    interfaces = parsed.get('interfaces', {})
    for api in interfaces.get('apis', []):
        importer.create_api_def(onto_id, name=api.get('name', api.get('id', 'api_unknown')),
            url=api.get('url', ''), method=api.get('method', 'GET'))
    for q in interfaces.get('queries', []):
        importer.create_query(onto_id, name=q.get('name', q.get('id', 'q_unknown')),
            template=q.get('template', ''), query_type=q.get('type', 'CUSTOM'))
    for c in interfaces.get('compute', []):
        importer.create_compute(onto_id, name=c.get('name', c.get('id', 'c_unknown')),
            formula=c.get('formula', ''), output_type=c.get('outputType', 'NUMBER'))
    for n in interfaces.get('notifications', []):
        importer.create_notification(onto_id, name=n.get('name', n.get('id', 'n_unknown')),
            channel=n.get('channel', 'EMAIL'), template=n.get('template', ''))
    for r in interfaces.get('reports', []):
        importer.create_report(onto_id, name=r.get('name', r.get('id', 'r_unknown')),
            report_format=r.get('format', 'TABLE'))

    # 8. 导入指标 (来自 behavior 维度)
    behavioral = parsed.get('behavioral', {})
    for ind in behavioral.get('indicators', []):
        importer.create_indicator(onto_id, name=ind.get('name', ind.get('id', 'ind_unknown')),
            formula=ind.get('formula', ''), agg_type=ind.get('aggregationType', 'COUNT'))

    print(f"\n=== 导入完成 ===")


# ============================================================
# CLI
# ============================================================
def main():
    parser = argparse.ArgumentParser(description='项目1→项目2 导入适配器')
    parser.add_argument('--url', default='http://localhost:8080/api',
                        help='项目2 API 基础 URL')
    parser.add_argument('--token', default='', help='API Token')
    parser.add_argument('--file', help='项目1 导出 JSON/YAML 文件')
    parser.add_argument('--manifest', help='Manifest JSON 文件')
    parser.add_argument('--xlsx', help='XLSX 文件 (暂不支持)')
    parser.add_argument('--domain', default='default', help='本体名称')
    args = parser.parse_args()

    # 读取数据
    data = None
    if args.file:
        with open(args.file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        print(f"读取项目1 导出文件: {args.file}")
    elif args.manifest:
        with open(args.manifest, 'r', encoding='utf-8') as f:
            data = f.read()
        # 尝试解析 JSON/YAML
        try:
            data = json.loads(data)
        except json.JSONDecodeError:
            import yaml
            data = yaml.safe_load(data)
        print(f"读取 Manifest 文件: {args.manifest}")
    elif args.xlsx:
        print("XLSX 导入暂不支持，请先用 JSON 导出")
        return
    else:
        # 从 stdin 读取
        data = json.load(sys.stdin)

    if not data:
        print("错误: 无法解析输入数据")
        return

    parsed = parse_project1_export(data)
    importer = PlatformImporter(args.url, args.token)
    import_export(importer, parsed, domain_name=args.domain)


if __name__ == '__main__':
    main()
