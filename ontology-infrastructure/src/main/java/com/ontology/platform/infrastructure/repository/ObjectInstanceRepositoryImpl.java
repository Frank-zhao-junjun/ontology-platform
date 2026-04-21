package com.ontology.platform.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.entity.ObjectInstance;
import com.ontology.platform.domain.repository.ObjectInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对象实例仓储实现
 * ObjectInstance Repository Implementation
 * 使用PostgreSQL JSONB存储属性
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ObjectInstanceRepositoryImpl implements ObjectInstanceRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final String TABLE_NAME = "object_instance";

    @Override
    public Optional<ObjectInstance> findById(String id) {
        log.debug("Finding instance by id: {}", id);
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        List<ObjectInstance> results = jdbcTemplate.query(sql, new InstanceRowMapper(objectMapper), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<ObjectInstance> findByOntologyIdAndPrimaryKeyValue(String ontologyId, String primaryKeyValue) {
        log.debug("Finding instance by ontologyId and primaryKeyValue: {}, {}", ontologyId, primaryKeyValue);
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE ontology_id = ? AND primary_key_value = ?";
        List<ObjectInstance> results = jdbcTemplate.query(sql, new InstanceRowMapper(objectMapper), ontologyId, primaryKeyValue);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<ObjectInstance> findByOntologyIdAndObjectTypeId(String ontologyId, String objectTypeId) {
        log.debug("Finding instances by ontologyId and objectTypeId: {}, {}", ontologyId, objectTypeId);
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE ontology_id = ? AND object_type_id = ?";
        return jdbcTemplate.query(sql, new InstanceRowMapper(objectMapper), ontologyId, objectTypeId);
    }

    @Override
    public List<ObjectInstance> findByOntologyId(String ontologyId) {
        log.debug("Finding instances by ontologyId: {}", ontologyId);
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE ontology_id = ?";
        return jdbcTemplate.query(sql, new InstanceRowMapper(objectMapper), ontologyId);
    }

    @Override
    public List<ObjectInstance> findByObjectTypeId(String objectTypeId) {
        log.debug("Finding instances by objectTypeId: {}", objectTypeId);
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE object_type_id = ?";
        return jdbcTemplate.query(sql, new InstanceRowMapper(objectMapper), objectTypeId);
    }

    @Override
    public ObjectInstance save(ObjectInstance instance) {
        log.debug("Saving instance: {}", instance.getId());
        String sql = """
            INSERT INTO {} (id, ontology_id, object_type_id, object_type_name, primary_key_value, 
                           properties, status, version, created_at, updated_at, created_by)
            VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?)
            """.replace("{}", TABLE_NAME);
        
        String propertiesJson = toJson(instance.getProperties());
        
        jdbcTemplate.update(sql,
                instance.getId(),
                instance.getOntologyId(),
                instance.getObjectTypeId(),
                instance.getObjectTypeName(),
                instance.getPrimaryKeyValue(),
                propertiesJson,
                instance.getStatus(),
                instance.getVersion(),
                instance.getCreatedAt(),
                instance.getUpdatedAt(),
                instance.getCreatedBy()
        );
        
        return instance;
    }

    @Override
    public ObjectInstance update(ObjectInstance instance) {
        log.debug("Updating instance: {}", instance.getId());
        String sql = """
            UPDATE {} SET 
                primary_key_value = ?,
                properties = ?::jsonb,
                status = ?,
                version = ?,
                updated_at = ?
            WHERE id = ?
            """.replace("{}", TABLE_NAME);
        
        String propertiesJson = toJson(instance.getProperties());
        
        jdbcTemplate.update(sql,
                instance.getPrimaryKeyValue(),
                propertiesJson,
                instance.getStatus(),
                instance.getVersion(),
                instance.getUpdatedAt(),
                instance.getId()
        );
        
        return instance;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting instance by id: {}", id);
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void markAsDeleted(String id) {
        log.debug("Marking instance as deleted: {}", id);
        String sql = "UPDATE {} SET status = 'deleted', updated_at = ? WHERE id = ?".replace("{}", TABLE_NAME);
        jdbcTemplate.update(sql, Instant.now(), id);
    }

    @Override
    public boolean existsByOntologyIdAndObjectTypeIdAndPrimaryKeyValue(String ontologyId, String objectTypeId, String primaryKeyValue) {
        log.debug("Checking existence: ontology={}, type={}, pk={}", ontologyId, objectTypeId, primaryKeyValue);
        String sql = """
            SELECT COUNT(*) FROM {} 
            WHERE ontology_id = ? AND object_type_id = ? AND primary_key_value = ?
            """.replace("{}", TABLE_NAME);
        Long count = jdbcTemplate.queryForObject(sql, Long.class, ontologyId, objectTypeId, primaryKeyValue);
        return count != null && count > 0;
    }

    @Override
    public long countByOntologyIdAndObjectTypeId(String ontologyId, String objectTypeId) {
        log.debug("Counting instances: ontology={}, type={}", ontologyId, objectTypeId);
        String sql = "SELECT COUNT(*) FROM {} WHERE ontology_id = ? AND object_type_id = ? AND status != 'deleted'".replace("{}", TABLE_NAME);
        Long count = jdbcTemplate.queryForObject(sql, Long.class, ontologyId, objectTypeId);
        return count != null ? count : 0;
    }

    @Override
    public long countByOntologyId(String ontologyId) {
        log.debug("Counting instances by ontology: {}", ontologyId);
        String sql = "SELECT COUNT(*) FROM {} WHERE ontology_id = ? AND status != 'deleted'".replace("{}", TABLE_NAME);
        Long count = jdbcTemplate.queryForObject(sql, Long.class, ontologyId);
        return count != null ? count : 0;
    }

    @Override
    public List<ObjectInstance> saveAll(List<ObjectInstance> instances) {
        log.debug("Batch saving instances: count={}", instances.size());
        List<ObjectInstance> saved = new ArrayList<>();
        for (ObjectInstance instance : instances) {
            saved.add(save(instance));
        }
        return saved;
    }

    @Override
    public void deleteAllByIds(List<String> ids) {
        log.debug("Batch deleting instances: count={}", ids.size());
        if (ids.isEmpty()) {
            return;
        }
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id IN (" + placeholders + ")";
        jdbcTemplate.update(sql, ids.toArray());
    }

    /**
     * JSON转换辅助方法
     */
    private String toJson(Map<String, Object> map) {
        if (map == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize properties to JSON", e);
            return "{}";
        }
    }

    /**
     * RowMapper实现
     */
    private static class InstanceRowMapper implements RowMapper<ObjectInstance> {
        
        private final ObjectMapper objectMapper;

        public InstanceRowMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public ObjectInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
            ObjectInstance instance = ObjectInstance.builder()
                    .id(rs.getString("id"))
                    .ontologyId(rs.getString("ontology_id"))
                    .objectTypeId(rs.getString("object_type_id"))
                    .objectTypeName(rs.getString("object_type_name"))
                    .primaryKeyValue(rs.getString("primary_key_value"))
                    .status(rs.getString("status"))
                    .version(rs.getInt("version"))
                    .createdAt(rs.getTimestamp("created_at").toInstant())
                    .updatedAt(rs.getTimestamp("updated_at").toInstant())
                    .createdBy(rs.getString("created_by"))
                    .build();

            // 解析JSONB属性
            String propertiesJson = rs.getString("properties");
            if (propertiesJson != null && !propertiesJson.isBlank()) {
                try {
                    Map<String, Object> properties = objectMapper.readValue(
                            propertiesJson, 
                            new TypeReference<Map<String, Object>>() {}
                    );
                    instance.setProperties(properties);
                } catch (JsonProcessingException e) {
                    instance.setProperties(new HashMap<>());
                }
            } else {
                instance.setProperties(new HashMap<>());
            }

            return instance;
        }
    }
}
