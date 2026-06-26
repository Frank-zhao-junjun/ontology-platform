// =============================================
// Ontology Model Loader — reads & validates .ontology-model.json
// =============================================

import { readFileSync, existsSync } from 'fs';
import * as path from 'path';

export interface OntologyEntityAttribute {
  name: string;
  nameEn?: string;
  type: string;
  required?: boolean;
  description?: string;
}

export interface OntologyEntityRelation {
  target: string;
  type: string;
  name?: string;
}

export interface OntologyEntity {
  id: string;
  name: string;
  nameEn?: string;
  description?: string;
  attributes: OntologyEntityAttribute[];
  relations: OntologyEntityRelation[];
}

export interface OntologyStateMachine {
  id: string;
  name: string;
  entity: string;
  statusField?: string;
  states: Array<{ id: string; name: string; isInitial?: boolean; isFinal?: boolean }>;
  transitions: Array<{ id: string; name: string; from: string; to: string; trigger?: string }>;
}

export interface OntologyBusinessChain {
  valueDomains: Array<{ id: string; name: string; nameEn?: string }>;
  capabilities: Array<{ id: string; name: string; nameEn?: string; parentId: string }>;
  scenarios: Array<{ id: string; name: string; parentId: string }>;
  epcProcesses: Array<{ id: string; name: string; parentId: string }>;
}

export interface OntologyRule {
  id: string;
  name: string;
  description?: string;
  entity: string;
  field?: string;
  condition: unknown;
  severity: string;
}

export interface OntologyMetric {
  id: string;
  name: string;
  nameEn?: string;
  formula?: string;
  unit?: string;
  measurementType?: string;
  targetValue?: number;
}

export interface OntologyModel {
  version: string;
  exportedAt: string;
  project: {
    id: string;
    name: string;
    domain: string;
  };
  entities: OntologyEntity[];
  stateMachines: OntologyStateMachine[];
  businessChain: OntologyBusinessChain;
  metrics: OntologyMetric[];
  rules: OntologyRule[];
  governance: {
    roles: Array<{ id: string; name: string; permissions: unknown[] }>;
  };
  dataSources: Array<{ id: string; name: string; type: string }>;
}

/**
 * Load ontology model from a JSON file path.
 * Returns null if the file doesn't exist or is invalid.
 */
export function loadOntologyModel(filePath?: string): OntologyModel | null {
  const resolvedPath = filePath || process.env.ONTOLOGY_MODEL_PATH;
  if (!resolvedPath) {
    console.warn('[model-loader] No ONTOLOGY_MODEL_PATH set — skipping ontology model load');
    return null;
  }

  if (!existsSync(resolvedPath)) {
    console.warn(`[model-loader] Ontology model not found at "${resolvedPath}"`);
    return null;
  }

  try {
    const raw = readFileSync(resolvedPath, 'utf-8');
    const model = JSON.parse(raw) as OntologyModel;

    if (!model.version || !model.project || !Array.isArray(model.entities)) {
      console.warn('[model-loader] Invalid ontology model format — missing required fields');
      return null;
    }

    console.log(`[model-loader] Loaded ontology model: "${model.project.name}" (${model.entities.length} entities)`);
    return model;
  } catch (err) {
    console.error(`[model-loader] Failed to parse ontology model:`, err);
    return null;
  }
}

/**
 * Reload the ontology model from disk (for hot-reload use cases).
 */
let _cachedModel: OntologyModel | null = null;

export function getOntologyModel(filePath?: string): OntologyModel | null {
  if (!_cachedModel) {
    _cachedModel = loadOntologyModel(filePath);
  }
  return _cachedModel;
}

export function reloadOntologyModel(filePath?: string): OntologyModel | null {
  _cachedModel = loadOntologyModel(filePath);
  return _cachedModel;
}
