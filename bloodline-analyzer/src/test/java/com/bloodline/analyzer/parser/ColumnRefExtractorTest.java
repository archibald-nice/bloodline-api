package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedColumnRef;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ColumnRefExtractorTest {

    private final ColumnRefExtractor extractor = new ColumnRefExtractor();

    @Test
    void testExtractSimpleSelect() {
        String sql = "SELECT order_id, user_id, amount FROM orders";

        List<ParsedColumnRef> refs = extractor.extract(sql, "order-service", "OrderMapper.xml:findAll");

        assertThat(refs).hasSize(3);
        Set<String> columns = refs.stream()
                .map(ParsedColumnRef::getColumnName)
                .collect(Collectors.toSet());
        assertThat(columns).containsExactlyInAnyOrder("order_id", "user_id", "amount");

        assertThat(refs).allMatch(ref -> ref.getOperationType().equals("SELECT"));
        assertThat(refs).allMatch(ref -> ref.getOperationDetail().equals("READ"));
        assertThat(refs).allMatch(ref -> ref.getTableName().equals("orders"));
        assertThat(refs).allMatch(ref -> ref.getAppId().equals("order-service"));
        assertThat(refs).allMatch(ref -> ref.getSourceLocation().equals("OrderMapper.xml:findAll"));
        assertThat(refs).allMatch(ref -> ref.getSqlSignature() != null && !ref.getSqlSignature().isEmpty());
        assertThat(refs).allMatch(ref -> ref.getSqlPreview() != null);
    }

    @Test
    void testExtractSelectWithWhere() {
        String sql = "SELECT order_id, user_id FROM orders WHERE status = 'ACTIVE' AND amount > 100";

        List<ParsedColumnRef> refs = extractor.extract(sql, "order-service", "OrderMapper.xml:findByStatus");

        // order_id, user_id (READ), status (WHERE), amount (WHERE) = 4
        assertThat(refs).hasSize(4);

        List<ParsedColumnRef> readRefs = refs.stream()
                .filter(ref -> ref.getOperationDetail().equals("READ"))
                .collect(Collectors.toList());
        assertThat(readRefs).hasSize(2);
        assertThat(readRefs).extracting(ParsedColumnRef::getColumnName)
                .containsExactlyInAnyOrder("order_id", "user_id");

        List<ParsedColumnRef> whereRefs = refs.stream()
                .filter(ref -> ref.getOperationDetail().equals("WHERE"))
                .collect(Collectors.toList());
        assertThat(whereRefs).hasSize(2);
        assertThat(whereRefs).extracting(ParsedColumnRef::getColumnName)
                .containsExactlyInAnyOrder("status", "amount");
    }

    @Test
    void testExtractUpdate() {
        String sql = "UPDATE user SET name = 'John', age = age + 1 WHERE id = 5";

        List<ParsedColumnRef> refs = extractor.extract(sql, "user-service", "UserMapper.xml:updateUser");

        // name (WRITE), age (WRITE), age (READ), id (WHERE) = 4
        assertThat(refs).hasSize(4);

        List<ParsedColumnRef> writeRefs = refs.stream()
                .filter(ref -> ref.getOperationDetail().equals("WRITE"))
                .collect(Collectors.toList());
        assertThat(writeRefs).hasSize(2);
        assertThat(writeRefs).extracting(ParsedColumnRef::getColumnName)
                .containsExactlyInAnyOrder("name", "age");

        List<ParsedColumnRef> readRefs = refs.stream()
                .filter(ref -> ref.getOperationDetail().equals("READ"))
                .collect(Collectors.toList());
        assertThat(readRefs).hasSize(1);
        assertThat(readRefs.get(0).getColumnName()).isEqualTo("age");

        List<ParsedColumnRef> whereRefs = refs.stream()
                .filter(ref -> ref.getOperationDetail().equals("WHERE"))
                .collect(Collectors.toList());
        assertThat(whereRefs).hasSize(1);
        assertThat(whereRefs.get(0).getColumnName()).isEqualTo("id");
    }

    @Test
    void testTableAliasResolution() {
        String sql = "SELECT o.id, u.name FROM orders o JOIN user u ON o.user_id = u.id WHERE o.status = 'A'";

        List<ParsedColumnRef> refs = extractor.extract(sql, "order-service", "OrderMapper.xml:findWithUser");

        assertThat(refs).isNotEmpty();

        // Verify aliases are resolved to real table names
        Set<String> tableNames = refs.stream()
                .map(ParsedColumnRef::getTableName)
                .collect(Collectors.toSet());
        assertThat(tableNames).containsExactlyInAnyOrder("orders", "user");
        assertThat(tableNames).doesNotContain("o", "u");

        // Verify columns from orders table
        List<ParsedColumnRef> ordersCols = refs.stream()
                .filter(ref -> "orders".equals(ref.getTableName()))
                .collect(Collectors.toList());
        assertThat(ordersCols).extracting(ParsedColumnRef::getColumnName)
                .contains("id", "user_id", "status");

        // Verify columns from user table
        List<ParsedColumnRef> userCols = refs.stream()
                .filter(ref -> "user".equals(ref.getTableName()))
                .collect(Collectors.toList());
        assertThat(userCols).extracting(ParsedColumnRef::getColumnName)
                .contains("id", "name");
    }
}
