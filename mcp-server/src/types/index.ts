// =============================================
// MCP Server Type Definitions
// =============================================

export enum IntentCategory {
  QUERY = 'QUERY',
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  ANALYZE = 'ANALYZE',
  NAVIGATE = 'NAVIGATE',
  EXECUTE = 'EXECUTE',
  UNKNOWN = 'UNKNOWN',
}

export interface ToolDefinition {
  name: string;
  description: string;
  inputSchema: {
    type: 'object';
    properties: Record<string, unknown>;
    required?: string[];
  };
  domain: string;
  riskLevel: 'READ' | 'WRITE' | 'DELETE' | 'APPROVAL';
  /** Tool version (1, 2, ...). Registered as {name}_v{version}. Phase 2c / F04 */
  version?: number;
  /** Whether this tool version is deprecated. Deprecated tools show a warning. */
  deprecated?: boolean;
  /** ISO timestamp when this deprecated version will be removed. */
  sunsetAt?: string;
  handler: (args: Record<string, unknown>, ctx: AgentContext) => Promise<unknown>;
}

export interface AgentContext {
  agentId: string;
  tenantId: string;
  domains: string[];
  roles: Map<string, string>; // domain -> role
  tokenId: string;
}

export interface ToolCallRequest {
  name: string;
  arguments: Record<string, unknown>;
}

export interface JsonRpcRequest {
  jsonrpc: '2.0';
  id: string | number;
  method: string;
  params?: unknown;
}

export interface JsonRpcResponse {
  jsonrpc: '2.0';
  id: string | number;
  result?: unknown;
  error?: {
    code: number;
    message: string;
    data?: unknown;
  };
}

export interface StructuredContent {
  status: 'success' | 'error' | 'pending_approval';
  data: unknown;
  metadata: {
    version: string;
    generated_at: string;
    trace_id: string;
    confidence?: number;
    derivation_chain?: string[];
  };
  error?: {
    code: string;
    message: string;
    details?: unknown;
  };
}

export interface JwtPayload {
  agentId: string;
  tenantId: string;
  domains: string[];
  roles: Record<string, string>;
  tokenId: string;
  iat: number;
  exp: number;
}

export interface ResolveIntentInput {
  query: string;
}

export interface ResolveIntentOutput {
  category: IntentCategory;
  confidence: number;
  entities: string[];
  suggestedTool?: string;
}

export interface ValidateInstructionInput {
  actionName: string;
  entityId: string;
  params: Record<string, unknown>;
}

export interface ValidateInstructionOutput {
  valid: boolean;
  message: string;
  missingFields?: string[];
  ruleResults?: Array<{
    ruleId: string;
    passed: boolean;
    reason?: string;
  }>;
}

export interface QueryOntologyInput {
  ontologyId: string;
  entities?: string[];
  includeActions?: boolean;
  includeEvents?: boolean;
  includeEpc?: boolean;
}

export interface TraverseGraphInput {
  ontologyId: string;
  startEntityId: string;
  relationTypes?: string[];
  maxDepth?: number;
}
