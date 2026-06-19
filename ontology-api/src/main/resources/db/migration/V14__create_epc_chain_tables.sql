-- V14__create_epc_chain_tables.sql
-- Phase 3d: EPC Chain graph tables

CREATE TABLE epc_chain (
    id VARCHAR(200) PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    aggregate_root_id VARCHAR(200),
    description TEXT,
    chain_type VARCHAR(50) DEFAULT 'production',  -- production, approval, notification
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE epc_node (
    id VARCHAR(200) PRIMARY KEY,
    chain_id VARCHAR(200) NOT NULL,
    node_type VARCHAR(50) NOT NULL,  -- event, function, connector, process_path, AND, OR, XOR
    name VARCHAR(500) NOT NULL,
    description TEXT,
    ref_type VARCHAR(50),            -- action, state, rule, event, sub_chain
    ref_id VARCHAR(200),             -- FK to referenced entity
    metadata JSONB,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT fk_node_chain FOREIGN KEY (chain_id) REFERENCES epc_chain(id)
);
CREATE INDEX idx_node_chain ON epc_node(chain_id, sort_order);

CREATE TABLE epc_edge (
    id VARCHAR(200) PRIMARY KEY,
    chain_id VARCHAR(200) NOT NULL,
    source_node_id VARCHAR(200) NOT NULL,
    target_node_id VARCHAR(200) NOT NULL,
    edge_type VARCHAR(50) DEFAULT 'control_flow',  -- control_flow, data_flow, message_flow
    label VARCHAR(500),
    condition_expr TEXT,
    metadata JSONB,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT fk_edge_chain FOREIGN KEY (chain_id) REFERENCES epc_chain(id),
    CONSTRAINT fk_edge_source FOREIGN KEY (source_node_id) REFERENCES epc_node(id),
    CONSTRAINT fk_edge_target FOREIGN KEY (target_node_id) REFERENCES epc_node(id)
);
CREATE INDEX idx_edge_chain ON epc_edge(chain_id, sort_order);

CREATE TABLE epc_model_ref (
    id VARCHAR(200) PRIMARY KEY,
    chain_id VARCHAR(200) NOT NULL,
    model_type VARCHAR(50) NOT NULL,  -- state_machine, action, rule, event
    model_id VARCHAR(200) NOT NULL,
    ref_metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT fk_ref_chain FOREIGN KEY (chain_id) REFERENCES epc_chain(id)
);
CREATE INDEX idx_ref_chain ON epc_model_ref(chain_id);

CREATE TABLE epc_profile (
    id VARCHAR(200) PRIMARY KEY,
    chain_id VARCHAR(200),
    profile_data JSONB NOT NULL,
    profile_version VARCHAR(50) DEFAULT '1.0',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_profile_chain ON epc_profile(chain_id);
