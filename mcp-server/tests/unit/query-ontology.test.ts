import { describe, it, expect, vi, beforeEach } from 'vitest';
import { queryOntologyTool } from '../../src/mcp/tools/query-ontology.js';

const mockCtx = {
  agentId: 'test-agent',
  tenantId: 'default',
  domains: ['platform'],
  roles: new Map([['platform', 'READER']]),
  tokenId: 'test-token',
};

vi.mock('../../src/client/platform-client.js', () => ({
  platformClient: {
    queryActions: vi.fn().mockResolvedValue([]),
    queryEvents: vi.fn().mockResolvedValue([]),
    queryEpc: vi.fn().mockResolvedValue([]),
    queryEpcCoverage: vi.fn().mockResolvedValue({
      ontologyId: 'onto-1',
      chainCount: 1,
      nodeCount: 2,
      edgeCount: 1,
      modelRefCount: 2,
      profileCount: 0,
      aggregateRootsCovered: 1,
      aggregateRootIds: ['production-order'],
      coverageRatio: 1,
      chains: [],
    }),
    querySemanticLayer: vi.fn().mockResolvedValue({ intents: [] }),
    queryLifecycle: vi.fn().mockResolvedValue({ entityId: 'production-order' }),
  },
}));

import { platformClient } from '../../src/client/platform-client.js';

describe('query_ontology tool', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('includes epc coverage when requested', async () => {
    const result = await queryOntologyTool.handler(
      {
        ontologyId: 'onto-1',
        includeActions: false,
        includeEvents: false,
        includeEpcCoverage: true,
      },
      mockCtx
    );
    const data = (result as { structuredContent: { data: Record<string, unknown> } }).structuredContent.data;
    expect(platformClient.queryEpcCoverage).toHaveBeenCalledWith('onto-1');
    expect(data.epcCoverage).toMatchObject({ chainCount: 1, nodeCount: 2 });
    expect(data).not.toHaveProperty('actions');
  });

  it('includes semantic layer and lifecycle when requested', async () => {
    const result = await queryOntologyTool.handler(
      {
        ontologyId: 'onto-1',
        includeActions: false,
        includeEvents: false,
        includeSemantic: true,
        includeLifecycle: true,
        entityId: 'production-order',
      },
      mockCtx
    );
    const data = (result as { structuredContent: { data: Record<string, unknown> } }).structuredContent.data;
    expect(platformClient.querySemanticLayer).toHaveBeenCalledWith('onto-1');
    expect(platformClient.queryLifecycle).toHaveBeenCalledWith('onto-1', 'production-order');
    expect(data.semanticLayer).toEqual({ intents: [] });
    expect(data.lifecycle).toEqual({ entityId: 'production-order' });
  });

  it('throws when includeLifecycle without entityId', async () => {
    await expect(
      queryOntologyTool.handler(
        {
          ontologyId: 'onto-1',
          includeActions: false,
          includeEvents: false,
          includeLifecycle: true,
        },
        mockCtx
      )
    ).rejects.toThrow('entityId is required');
  });
});
