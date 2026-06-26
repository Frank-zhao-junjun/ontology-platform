// =============================================
// Tool Initialization — Register all tools
// =============================================

import { toolRegistry } from './registry.js';
import { resolveIntentTool } from './resolve-intent.js';
import { queryOntologyTool } from './query-ontology.js';
import { traverseGraphTool } from './traverse-graph.js';
import { validateInstructionTool } from './validate-instruction.js';
import { executeActionTool } from './execute-action.js';
import { generateEntityTools } from './auto-entity-tools.js';
import { loadOntologyModel } from '../../model-loader.js';

export function initializeTools(): void {
  toolRegistry.register(resolveIntentTool);
  toolRegistry.register(queryOntologyTool);
  toolRegistry.register(traverseGraphTool);
  toolRegistry.register(validateInstructionTool);
  toolRegistry.register(executeActionTool);
  let toolCount = 5;

  // Load ontology model and register entity-specific tools (Feature 2)
  const modelPath = process.env.ONTOLOGY_MODEL_PATH;
  if (modelPath) {
    loadOntologyModel(modelPath);
    const entityTools = generateEntityTools();
    if (entityTools.length > 0) {
      toolRegistry.registerAll(entityTools);
      toolCount += entityTools.length;
    }
  }

  console.log(`[tools] ${toolCount} tools registered (5 fixed + ${toolCount - 5} auto)`);
}

export { toolRegistry };
