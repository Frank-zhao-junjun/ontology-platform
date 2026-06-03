import type { Plugin } from 'vite';

// ==================== Mock Data ====================

const ontologies = [
  {
    id: 'ont-001',
    name: 'enterprise-model',
    displayName: '企业架构本体',
    description: '描述企业组织架构、业务流程和IT系统的本体模型',
    version: '1.2.0',
    status: 'PUBLISHED',
    tenantId: 'default',
    createdAt: '2025-01-15T08:30:00Z',
    updatedAt: '2025-03-20T14:22:00Z',
  },
  {
    id: 'ont-002',
    name: 'product-ontology',
    displayName: '产品知识本体',
    description: '产品分类、属性和关系的知识图谱本体',
    version: '0.8.0',
    status: 'DRAFT',
    tenantId: 'default',
    createdAt: '2025-02-10T10:00:00Z',
    updatedAt: '2025-04-01T09:15:00Z',
  },
  {
    id: 'ont-003',
    name: 'iot-domain',
    displayName: '物联网领域本体',
    description: '定义物联网设备、传感器、网关及数据流关系的本体',
    version: '2.0.0',
    status: 'PUBLISHED',
    tenantId: 'default',
    createdAt: '2024-11-05T16:45:00Z',
    updatedAt: '2025-02-28T11:30:00Z',
  },
  {
    id: 'ont-004',
    name: 'medical-knowledge',
    displayName: '医学知识本体',
    description: '疾病、症状、药物和治疗方案的知识体系',
    version: '1.0.0',
    status: 'ARCHIVED',
    tenantId: 'default',
    createdAt: '2024-06-20T09:00:00Z',
    updatedAt: '2024-12-15T17:00:00Z',
  },
  {
    id: 'ont-005',
    name: 'supply-chain',
    displayName: '供应链本体',
    description: '供应商、物流、仓储和订单关系的本体模型',
    version: '0.5.0',
    status: 'DRAFT',
    tenantId: 'default',
    createdAt: '2025-03-01T13:20:00Z',
    updatedAt: '2025-04-10T08:45:00Z',
  },
];

const objectTypes = [
  {
    id: 'ot-001',
    ontologyId: 'ont-001',
    name: 'Department',
    displayName: '部门',
    description: '组织架构中的部门单元',
    isAbstract: false,
    parentTypeId: null,
    properties: [
      { id: 'p-001', name: 'name', displayName: '部门名称', dataType: 'STRING', required: true, defaultValue: null },
      { id: 'p-002', name: 'code', displayName: '部门编码', dataType: 'STRING', required: true, defaultValue: null },
      { id: 'p-003', name: 'level', displayName: '层级', dataType: 'INTEGER', required: false, defaultValue: '1' },
    ],
    createdAt: '2025-01-16T10:00:00Z',
    updatedAt: '2025-03-20T14:22:00Z',
  },
  {
    id: 'ot-002',
    ontologyId: 'ont-001',
    name: 'Employee',
    displayName: '员工',
    description: '组织中的员工实体',
    isAbstract: false,
    parentTypeId: null,
    properties: [
      { id: 'p-004', name: 'name', displayName: '姓名', dataType: 'STRING', required: true, defaultValue: null },
      { id: 'p-005', name: 'employeeId', displayName: '工号', dataType: 'STRING', required: true, defaultValue: null },
      { id: 'p-006', name: 'email', displayName: '邮箱', dataType: 'STRING', required: false, defaultValue: null },
      { id: 'p-007', name: 'hireDate', displayName: '入职日期', dataType: 'DATE', required: false, defaultValue: null },
    ],
    createdAt: '2025-01-16T10:30:00Z',
    updatedAt: '2025-03-18T09:00:00Z',
  },
  {
    id: 'ot-003',
    ontologyId: 'ont-001',
    name: 'BusinessProcess',
    displayName: '业务流程',
    description: '企业业务流程定义',
    isAbstract: false,
    parentTypeId: null,
    properties: [
      { id: 'p-008', name: 'processName', displayName: '流程名称', dataType: 'STRING', required: true, defaultValue: null },
      { id: 'p-009', name: 'status', displayName: '状态', dataType: 'ENUM', required: true, defaultValue: 'ACTIVE' },
    ],
    createdAt: '2025-02-01T11:00:00Z',
    updatedAt: '2025-03-15T16:30:00Z',
  },
  {
    id: 'ot-004',
    ontologyId: 'ont-002',
    name: 'Product',
    displayName: '产品',
    description: '产品实体定义',
    isAbstract: false,
    parentTypeId: null,
    properties: [
      { id: 'p-010', name: 'productName', displayName: '产品名称', dataType: 'STRING', required: true, defaultValue: null },
      { id: 'p-011', name: 'sku', displayName: 'SKU', dataType: 'STRING', required: true, defaultValue: null },
      { id: 'p-012', name: 'price', displayName: '价格', dataType: 'DECIMAL', required: false, defaultValue: '0' },
    ],
    createdAt: '2025-02-11T08:00:00Z',
    updatedAt: '2025-04-01T09:15:00Z',
  },
];

const graphTraversalResult = {
  nodes: [
    { id: 'n1', label: '技术部', type: 'Department', properties: { name: '技术部', level: 1 } },
    { id: 'n2', label: '张三', type: 'Employee', properties: { name: '张三', employeeId: 'E001' } },
    { id: 'n3', label: '李四', type: 'Employee', properties: { name: '李四', employeeId: 'E002' } },
    { id: 'n4', label: '研发流程', type: 'BusinessProcess', properties: { processName: '敏捷开发流程', status: 'ACTIVE' } },
  ],
  edges: [
    { id: 'e1', source: 'n1', target: 'n2', label: 'contains', properties: { role: '部门经理' } },
    { id: 'e2', source: 'n1', target: 'n3', label: 'contains', properties: { role: '高级工程师' } },
    { id: 'e3', source: 'n1', target: 'n4', label: 'owns', properties: {} },
    { id: 'e4', source: 'n2', target: 'n4', label: 'participates', properties: {} },
  ],
};

// ==================== Route Handlers ====================

type Handler = (url: URL, body?: unknown) => unknown;

const routes: Record<string, Handler> = {
  // Ontology CRUD
  'GET /v1/ontologies': (url) => {
    const tenantId = url.searchParams.get('tenantId') || 'default';
    const filtered = ontologies.filter((o) => o.tenantId === tenantId);
    return { code: 200, message: 'success', data: filtered };
  },
  'GET /v1/ontologies/:id': (url) => {
    const id = url.pathname.split('/v1/ontologies/')[1]?.split('/')[0];
    const ont = ontologies.find((o) => o.id === id);
    if (!ont) return { code: 404, message: 'Ontology not found', data: null };
    return { code: 200, message: 'success', data: ont };
  },
  'POST /v1/ontologies': (_url, body) => {
    const req = body as Record<string, string>;
    const newOnt = {
      id: `ont-${Date.now()}`,
      name: req.name || '',
      displayName: req.displayName || '',
      description: req.description || '',
      version: '0.1.0',
      status: 'DRAFT',
      tenantId: 'default',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    ontologies.push(newOnt);
    return { code: 200, message: 'success', data: newOnt };
  },
  'PUT /v1/ontologies/:id': (url, body) => {
    const id = url.pathname.split('/v1/ontologies/')[1]?.split('/')[0];
    const idx = ontologies.findIndex((o) => o.id === id);
    if (idx === -1) return { code: 404, message: 'Ontology not found', data: null };
    const req = body as Record<string, string>;
    ontologies[idx] = { ...ontologies[idx], ...req, updatedAt: new Date().toISOString() };
    return { code: 200, message: 'success', data: ontologies[idx] };
  },
  'DELETE /v1/ontologies/:id': (url) => {
    const id = url.pathname.split('/v1/ontologies/')[1]?.split('/')[0];
    const idx = ontologies.findIndex((o) => o.id === id);
    if (idx === -1) return { code: 404, message: 'Ontology not found', data: null };
    ontologies.splice(idx, 1);
    // Also remove related object types
    for (let i = objectTypes.length - 1; i >= 0; i--) {
      if (objectTypes[i].ontologyId === id) objectTypes.splice(i, 1);
    }
    return { code: 200, message: 'success', data: null };
  },
  'POST /v1/ontologies/:id/publish': (url) => {
    const id = url.pathname.split('/v1/ontologies/')[1]?.split('/')[0];
    const ont = ontologies.find((o) => o.id === id);
    if (!ont) return { code: 404, message: 'Ontology not found', data: null };
    ont.status = 'PUBLISHED';
    ont.updatedAt = new Date().toISOString();
    return { code: 200, message: 'success', data: ont };
  },
  'POST /v1/ontologies/:id/archive': (url) => {
    const id = url.pathname.split('/v1/ontologies/')[1]?.split('/')[0];
    const ont = ontologies.find((o) => o.id === id);
    if (!ont) return { code: 404, message: 'Ontology not found', data: null };
    ont.status = 'ARCHIVED';
    ont.updatedAt = new Date().toISOString();
    return { code: 200, message: 'success', data: ont };
  },
  'POST /v1/ontologies/:id/validate': (url) => {
    const id = url.pathname.split('/v1/ontologies/')[1]?.split('/')[0];
    const ont = ontologies.find((o) => o.id === id);
    if (!ont) return { code: 404, message: 'Ontology not found', data: null };
    const types = objectTypes.filter((ot) => ot.ontologyId === id);
    const issues: { severity: string; entityName: string; message: string }[] = [];
    if (types.length === 0) {
      issues.push({ severity: 'WARNING', entityName: ont.displayName, message: '本体中没有定义任何对象类型' });
    }
    if (!ont.description) {
      issues.push({ severity: 'WARNING', entityName: ont.displayName, message: '缺少描述信息' });
    }
    return {
      code: 200,
      message: 'success',
      data: { valid: issues.filter((i) => i.severity === 'ERROR').length === 0, issues },
    };
  },

  // Object Type CRUD
  'GET /v1/ontologies/:id/object-types': (url) => {
    const ontId = url.pathname.split('/v1/ontologies/')[1]?.split('/')[0];
    const types = objectTypes.filter((ot) => ot.ontologyId === ontId);
    return { code: 200, message: 'success', data: types };
  },
  'GET /v1/ontologies/:ontId/object-types/:otId': (url) => {
    const parts = url.pathname.split('/');
    const ontId = parts[4];
    const otId = parts[6];
    const ot = objectTypes.find((o) => o.id === otId && o.ontologyId === ontId);
    if (!ot) return { code: 404, message: 'Object type not found', data: null };
    return { code: 200, message: 'success', data: ot };
  },
  'POST /v1/ontologies/:id/object-types': (url, body) => {
    const ontId = url.pathname.split('/v1/ontologies/')[1]?.split('/')[0];
    const req = body as Record<string, unknown>;
    const newOt = {
      id: `ot-${Date.now()}`,
      ontologyId: ontId,
      name: (req.name as string) || '',
      displayName: (req.displayName as string) || '',
      description: (req.description as string) || '',
      isAbstract: (req.isAbstract as boolean) || false,
      parentTypeId: (req.parentTypeId as string) || null,
      properties: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    objectTypes.push(newOt);
    return { code: 200, message: 'success', data: newOt };
  },
  'PUT /v1/ontologies/:ontId/object-types/:otId': (url, body) => {
    const parts = url.pathname.split('/');
    const ontId = parts[4];
    const otId = parts[6];
    const idx = objectTypes.findIndex((o) => o.id === otId && o.ontologyId === ontId);
    if (idx === -1) return { code: 404, message: 'Object type not found', data: null };
    const req = body as Record<string, unknown>;
    objectTypes[idx] = { ...objectTypes[idx], ...req, updatedAt: new Date().toISOString() };
    return { code: 200, message: 'success', data: objectTypes[idx] };
  },
  'DELETE /v1/ontologies/:ontId/object-types/:otId': (url) => {
    const parts = url.pathname.split('/');
    const ontId = parts[4];
    const otId = parts[6];
    const idx = objectTypes.findIndex((o) => o.id === otId && o.ontologyId === ontId);
    if (idx === -1) return { code: 404, message: 'Object type not found', data: null };
    objectTypes.splice(idx, 1);
    return { code: 200, message: 'success', data: null };
  },

  // Properties
  'POST /v1/ontologies/:ontId/object-types/:otId/properties': (url, body) => {
    const parts = url.pathname.split('/');
    const ontId = parts[4];
    const otId = parts[6];
    const ot = objectTypes.find((o) => o.id === otId && o.ontologyId === ontId);
    if (!ot) return { code: 404, message: 'Object type not found', data: null };
    const req = body as Record<string, unknown>;
    const newProp = {
      id: `p-${Date.now()}`,
      name: (req.name as string) || '',
      displayName: (req.displayName as string) || '',
      dataType: (req.dataType as string) || 'STRING',
      required: (req.required as boolean) || false,
      defaultValue: (req.defaultValue as string) || null,
    };
    ot.properties.push(newProp);
    return { code: 200, message: 'success', data: newProp };
  },
  'DELETE /v1/ontologies/:ontId/object-types/:otId/properties/:propId': (url) => {
    const parts = url.pathname.split('/');
    const ontId = parts[4];
    const otId = parts[6];
    const propId = parts[8];
    const ot = objectTypes.find((o) => o.id === otId && o.ontologyId === ontId);
    if (!ot) return { code: 404, message: 'Object type not found', data: null };
    const pidx = ot.properties.findIndex((p: { id: string }) => p.id === propId);
    if (pidx === -1) return { code: 404, message: 'Property not found', data: null };
    ot.properties.splice(pidx, 1);
    return { code: 200, message: 'success', data: null };
  },

  // Graph Traversal
  'POST /v1/graphs/traverse': () => {
    return { code: 200, message: 'success', data: graphTraversalResult };
  },
  'POST /v1/graphs/shortest-path': () => {
    return {
      code: 200,
      message: 'success',
      data: {
        path: {
          nodes: graphTraversalResult.nodes.slice(0, 3),
          edges: graphTraversalResult.edges.slice(0, 2),
        },
        totalWeight: 2,
      },
    };
  },
  'POST /v1/graphs/subgraph': () => {
    return { code: 200, message: 'success', data: graphTraversalResult };
  },
};

// ==================== Match Route ====================

function matchRoute(method: string, pathname: string): Handler | null {
  const key = `${method} ${pathname}`;

  // Exact match first
  if (routes[key]) return routes[key];

  // Pattern match for :param routes
  for (const routeKey of Object.keys(routes)) {
    const [routeMethod, ...routePathParts] = routeKey.split(' ');
    const routePath = routePathParts.join(' ');
    if (routeMethod !== method) continue;

    const routeParts = routePath.split('/');
    const pathParts = pathname.split('/');
    if (routeParts.length !== pathParts.length) continue;

    let match = true;
    for (let i = 0; i < routeParts.length; i++) {
      if (routeParts[i].startsWith(':')) continue;
      if (routeParts[i] !== pathParts[i]) {
        match = false;
        break;
      }
    }
    if (match) return routes[routeKey];
  }

  return null;
}

// ==================== Vite Plugin ====================

export function mockServer(): Plugin {
  return {
    name: 'mock-server',
    configureServer(server) {
      server.middlewares.use(async (req, res, next) => {
        if (!req.url?.startsWith('/api/')) {
          return next();
        }

        const url = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
        const pathname = url.pathname.replace('/api', '');
        const method = req.method || 'GET';

        const handler = matchRoute(method, pathname);
        if (!handler) {
          res.statusCode = 404;
          res.setHeader('Content-Type', 'application/json');
          res.end(JSON.stringify({ code: 404, message: 'Not found', data: null }));
          return;
        }

        let body: unknown;
        if (method !== 'GET' && method !== 'HEAD') {
          const chunks: Buffer[] = [];
          for await (const chunk of req) chunks.push(chunk);
          const raw = Buffer.concat(chunks).toString();
          try {
            body = JSON.parse(raw);
          } catch {
            body = {};
          }
        }

        try {
          const result = handler(url, body);
          res.statusCode = 200;
          res.setHeader('Content-Type', 'application/json');
          res.setHeader('Access-Control-Allow-Origin', '*');
          res.end(JSON.stringify(result));
        } catch (err) {
          res.statusCode = 500;
          res.setHeader('Content-Type', 'application/json');
          res.end(JSON.stringify({ code: 500, message: String(err), data: null }));
        }
      });
    },
  };
}
