package com.etl.control.repository;

import com.etl.control.entity.SqlDefinition;
import com.etl.control.entity.SqlDefinition.SqlStatus;
import com.etl.control.entity.SqlDefinition.SqlType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SQL Definition Repository
 */
@Repository
public interface SqlDefinitionRepository extends JpaRepository<SqlDefinition, Long> {

    /**
     * Find by name
     */
    Optional<SqlDefinition> findByName(String name);

    /**
     * Find by status
     */
    List<SqlDefinition> findByStatus(SqlStatus status);

    /**
     * Find by SQL type
     */
    List<SqlDefinition> findBySqlType(SqlType sqlType);

    /**
     * Find by associated schema
     */
    List<SqlDefinition> findByAssociatedSchema(String associatedSchema);

    /**
     * Find active SQL by type
     */
    List<SqlDefinition> findBySqlTypeAndStatus(SqlType sqlType, SqlStatus status);

    /**
     * Check if name exists
     */
    boolean existsByName(String name);
}
