import type {
  ApiResponse,
  Ontology,
  CreateOntologyRequest,
  UpdateOntologyRequest,
  ObjectType,
  CreateObjectTypeRequest,
  UpdateObjectTypeRequest,
  Property,
  CreatePropertyRequest,
  UpdatePropertyRequest,
  GraphTraversalRequest,
  GraphTraversalResponse,
  ValidationResult,
} from '../types';

const API_BASE_URL = '/api';

interface ValidationIssueDto {
  severity: string;
  entityName?: string;
  message: string;
  suggestion?: string;
}

interface ValidationResultDto {
  valid: boolean;
  issues?: ValidationIssueDto[];
}

async function fetchApi<T>(
  path: string,
  options?: RequestInit
): Promise<T> {
  const url = `${API_BASE_URL}${path}`;
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': 'system',
      ...options?.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`HTTP ${response.status}: ${errorText}`);
  }

  const result: ApiResponse<T> = await response.json();
  if (result.code !== 200 && result.code !== 0) {
    throw new Error(result.message || `API Error: ${result.code}`);
  }
  return result.data;
}

function mapValidationResult(result: ValidationResultDto): ValidationResult {
  const errors: string[] = [];
  const warnings: string[] = [];

  for (const issue of result.issues || []) {
    const detail = [issue.entityName, issue.message, issue.suggestion]
      .filter(Boolean)
      .join(' - ');

    if (issue.severity === 'ERROR') {
      errors.push(detail);
      continue;
    }

    if (issue.severity === 'WARNING') {
      warnings.push(detail);
    }
  }

  return {
    valid: result.valid,
    errors,
    warnings,
  };
}

// ==================== Ontology APIs ====================

export async function listOntologies(
  tenantId: string = 'default',
  page: number = 1,
  pageSize: number = 20
): Promise<Ontology[]> {
  return fetchApi<Ontology[]>(
    `/v1/ontologies?tenantId=${tenantId}&page=${page}&pageSize=${pageSize}`
  );
}

export async function getOntology(id: string): Promise<Ontology> {
  return fetchApi<Ontology>(`/v1/ontologies/${id}`);
}

export async function createOntology(request: CreateOntologyRequest): Promise<Ontology> {
  return fetchApi<Ontology>('/v1/ontologies', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export async function updateOntology(
  id: string,
  request: UpdateOntologyRequest
): Promise<Ontology> {
  return fetchApi<Ontology>(`/v1/ontologies/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
}

export async function deleteOntology(id: string): Promise<void> {
  return fetchApi<void>(`/v1/ontologies/${id}`, {
    method: 'DELETE',
  });
}

export async function publishOntology(id: string): Promise<Ontology> {
  return fetchApi<Ontology>(`/v1/ontologies/${id}/publish`, {
    method: 'POST',
  });
}

export async function archiveOntology(id: string): Promise<Ontology> {
  return fetchApi<Ontology>(`/v1/ontologies/${id}/archive`, {
    method: 'POST',
  });
}

export async function validateOntology(id: string): Promise<ValidationResult> {
  const result = await fetchApi<ValidationResultDto>(`/v1/ontologies/${id}/validate`, {
    method: 'POST',
  });

  return mapValidationResult(result);
}

// ==================== Object Type APIs ====================

export async function listObjectTypes(ontologyId: string): Promise<ObjectType[]> {
  return fetchApi<ObjectType[]>(`/v1/ontologies/${ontologyId}/object-types`);
}

export async function getObjectType(
  ontologyId: string,
  id: string
): Promise<ObjectType> {
  return fetchApi<ObjectType>(`/v1/ontologies/${ontologyId}/object-types/${id}`);
}

export async function createObjectType(
  ontologyId: string,
  request: CreateObjectTypeRequest
): Promise<ObjectType> {
  return fetchApi<ObjectType>(`/v1/ontologies/${ontologyId}/object-types`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export async function updateObjectType(
  ontologyId: string,
  id: string,
  request: UpdateObjectTypeRequest
): Promise<ObjectType> {
  return fetchApi<ObjectType>(`/v1/ontologies/${ontologyId}/object-types/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
}

export async function deleteObjectType(ontologyId: string, id: string): Promise<void> {
  return fetchApi<void>(`/v1/ontologies/${ontologyId}/object-types/${id}`, {
    method: 'DELETE',
  });
}

// ==================== Property APIs ====================

export async function listProperties(
  ontologyId: string,
  objectTypeId: string
): Promise<Property[]> {
  return fetchApi<Property[]>(
    `/v1/ontologies/${ontologyId}/object-types/${objectTypeId}/properties`
  );
}

export async function createProperty(
  ontologyId: string,
  objectTypeId: string,
  request: CreatePropertyRequest
): Promise<Property> {
  return fetchApi<Property>(
    `/v1/ontologies/${ontologyId}/object-types/${objectTypeId}/properties`,
    {
      method: 'POST',
      body: JSON.stringify(request),
    }
  );
}

export async function updateProperty(
  ontologyId: string,
  objectTypeId: string,
  propertyId: string,
  request: UpdatePropertyRequest
): Promise<Property> {
  return fetchApi<Property>(
    `/v1/ontologies/${ontologyId}/object-types/${objectTypeId}/properties/${propertyId}`,
    {
      method: 'PUT',
      body: JSON.stringify(request),
    }
  );
}

export async function deleteProperty(
  ontologyId: string,
  objectTypeId: string,
  propertyId: string
): Promise<void> {
  return fetchApi<void>(
    `/v1/ontologies/${ontologyId}/object-types/${objectTypeId}/properties/${propertyId}`,
    {
      method: 'DELETE',
    }
  );
}

// ==================== Graph Traversal APIs ====================

export async function traverseGraph(
  ontologyId: string,
  request: GraphTraversalRequest
): Promise<GraphTraversalResponse> {
  return fetchApi<GraphTraversalResponse>(
    `/v1/ontologies/${ontologyId}/graph/traverse`,
    {
      method: 'POST',
      body: JSON.stringify(request),
    }
  );
}

export async function findShortestPath(
  ontologyId: string,
  from: string,
  to: string,
  maxDepth: number = 5
): Promise<GraphTraversalResponse> {
  return fetchApi<GraphTraversalResponse>(
    `/v1/ontologies/${ontologyId}/graph/paths?from=${from}&to=${to}&maxDepth=${maxDepth}`
  );
}

export async function extractSubgraph(
  ontologyId: string,
  root: string,
  depth: number = 3
): Promise<GraphTraversalResponse> {
  return fetchApi<GraphTraversalResponse>(
    `/v1/ontologies/${ontologyId}/graph/subgraph?root=${root}&depth=${depth}`
  );
}
