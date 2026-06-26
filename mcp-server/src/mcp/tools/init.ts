// =============================================
// Tool Initialization — Register all fixed tools
// Entity-specific tools are NOT auto-registered at startup.
// They are staged via load_ontology_model and applied via apply_ontology_model.
// =============================================

import { toolRegistry } from './registry.js';
import { resolveIntentTool } from './resolve-intent.js';
import { queryOntologyTool } from './query-ontology.js';
import { traverseGraphTool } from './traverse-graph.js';
import { validateInstructionTool } from './validate-instruction.js';
import { executeActionTool } from './execute-action.js';

export function initializeTools(): void {
  toolRegistry.register(resolveIntentTool);
  toolRegistry.register(queryOntologyTool);
  toolRegistry.register(traverseGraphTool);
  toolRegistry.register(validateInstructionTool);
  toolRegistry.register(executeActionTool);

  console.log('[tools] 5 fixed tools registered');
  console.log('[tools] Entity/rule tools are NOT auto-loaded — use load_ontology_model then apply_ontology_model to activate');
}

export { toolRegistry };
