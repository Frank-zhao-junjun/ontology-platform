// =============================================
// Tool Initialization — Register all fixed tools
// Entity-specific tools are NOT auto-registered at startup.
// They are staged via load_ontology_model and applied via apply_ontology_model.
// On startup, any previously applied (persisted) model is auto-loaded.
// =============================================

import { toolRegistry } from './registry.js';
import { resolveIntentTool } from './resolve-intent.js';
import { queryOntologyTool } from './query-ontology.js';
import { traverseGraphTool } from './traverse-graph.js';
import { validateInstructionTool } from './validate-instruction.js';
import { executeActionTool } from './execute-action.js';
import { autoLoadPersistedModel } from './auto-entity-tools.js';

export function initializeTools(): void {
  toolRegistry.register(resolveIntentTool);
  toolRegistry.register(queryOntologyTool);
  toolRegistry.register(traverseGraphTool);
  toolRegistry.register(validateInstructionTool);
  toolRegistry.register(executeActionTool);

  console.log('[tools] 5 fixed tools registered');

  // Auto-load previously applied ontology model (if persisted)
  const loaded = autoLoadPersistedModel();
  if (loaded) {
    console.log('[tools] Persisted ontology model auto-loaded at startup');
  } else {
    console.log('[tools] No persisted ontology model — use upload_ontology_model then apply_ontology_model to activate');
  }
}

export { toolRegistry };
