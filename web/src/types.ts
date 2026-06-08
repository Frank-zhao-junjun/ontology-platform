export interface Ontology {
  id: string;
  name: string;
  displayName: string;
  description: string;
  version: string;
  status: OntologyStatus;
  publishedAt: string;
  objectTypeCount: number;
  actionTypeCount: number;
  createdAt: string;
  updatedAt: string;
}

export type OntologyStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface CreateOntologyRequest {
  name: string;
  displayName: string;
  description?: string;
}

export interface UpdateOntologyRequest {
  displayName?: string;
  description?: string;
}

export interface ObjectType {
  id: string;
  ontologyId: string;
  name: string;
  displayName: string;
  description: string;
  primaryKey: string;
  parentId?: string;
  interfaceNames: string[];
  instanceCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateObjectTypeRequest {
  ontologyId: string;
  name: string;
  displayName: string;
  description?: string;
  primaryKey?: string;
  parentId?: string;
  interfaceNames?: string[];
}

export interface UpdateObjectTypeRequest {
  displayName?: string;
  description?: string;
  primaryKey?: string;
  parentId?: string;
  interfaceNames?: string[];
}

export interface Property {
  id: string;
  objectTypeId: string;
  name: string;
  displayName: string;
  description?: string;
  dataType: string;
  required: boolean;
  defaultValue?: string;
  constraints?: PropertyConstraint[];
  createdAt: string;
  updatedAt: string;
}

export interface PropertyConstraint {
  type: string;
  value: string;
}

export interface CreatePropertyRequest {
  objectTypeId: string;
  name: string;
  displayName: string;
  description?: string;
  dataType: string;
  required?: boolean;
  defaultValue?: string;
  constraints?: PropertyConstraint[];
}

export interface UpdatePropertyRequest {
  displayName?: string;
  description?: string;
  dataType?: string;
  required?: boolean;
  defaultValue?: string;
  constraints?: PropertyConstraint[];
}

export interface GraphTraversalRequest {
  startObjectType: string;
  startObjectId: string;
  path?: TraversalPath[];
  maxDepth?: number;
  direction?: 'OUTGOING' | 'INCOMING' | 'BOTH';
  limit?: number;
  filters?: TraversalFilter[];
  returnFormat?: 'GRAPH' | 'TREE' | 'FLAT';
  includeProperties?: string[];
  excludeProperties?: string[];
}

export interface TraversalPath {
  relationType?: string;
  targetObjectType?: string;
  depth?: number;
}

export interface TraversalFilter {
  depth?: number;
  targetType?: string;
  conditions?: FilterCondition[];
  logic?: 'AND' | 'OR';
}

export interface FilterCondition {
  field: string;
  operator: string;
  value: string;
}

export interface GraphTraversalResponse {
  success: boolean;
  totalCount: number;
  paths: PathInfo[];
  nodes: NodeInfo[];
  edges: EdgeInfo[];
  executionTimeMs: number;
  errorMessage?: string;
}

export interface PathInfo {
  pathId: string;
  nodeIds: string[];
  edgeIds: string[];
  depth: number;
}

export interface NodeInfo {
  id: string;
  objectType: string;
  objectId: string;
  properties: Record<string, unknown>;
  depth: number;
}

export interface EdgeInfo {
  id: string;
  relationType: string;
  sourceId: string;
  targetId: string;
  properties: Record<string, unknown>;
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  meta?: {
    page?: number;
    pageSize?: number;
    total?: number;
  };
}

export interface ValidationResult {
  valid: boolean;
  errors: string[];
  warnings: string[];
}
