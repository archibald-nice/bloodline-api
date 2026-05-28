package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedColumnRef;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ColumnRefExtractor {

    public List<ParsedColumnRef> extract(String sql, String appId, String sourceLocation) {
        List<ParsedColumnRef> results = new ArrayList<>();
        try {
            String normalizedSql = sql.replaceAll("#\\{[^}]*\\}", "?");
            Statement stmt = CCJSqlParserUtil.parse(normalizedSql);
            String sqlSignature = md5Hex(normalizedSql);
            String sqlPreview = normalizedSql.length() > 200
                    ? normalizedSql.substring(0, 200)
                    : normalizedSql;

            if (stmt instanceof Select) {
                extractFromSelect((Select) stmt, appId, sqlSignature, sqlPreview, sourceLocation, results);
            } else if (stmt instanceof Insert) {
                extractFromInsert((Insert) stmt, appId, sqlSignature, sqlPreview, sourceLocation, results);
            } else if (stmt instanceof Update) {
                extractFromUpdate((Update) stmt, appId, sqlSignature, sqlPreview, sourceLocation, results);
            } else if (stmt instanceof Delete) {
                extractFromDelete((Delete) stmt, appId, sqlSignature, sqlPreview, sourceLocation, results);
            }

            deduplicate(results);
        } catch (Exception e) {
            // Return empty list on parse failure
        }
        return results;
    }

    private void extractFromSelect(Select select, String appId, String sqlSignature,
                                   String sqlPreview, String sourceLocation, List<ParsedColumnRef> results) {
        if (!(select.getSelectBody() instanceof PlainSelect)) {
            return;
        }
        PlainSelect ps = (PlainSelect) select.getSelectBody();
        Map<String, String> aliasMap = buildAliasMap(ps);
        String defaultTable = extractDefaultTable(ps);

        // SELECT projection items -> READ
        for (SelectItem item : ps.getSelectItems()) {
            if (item instanceof SelectExpressionItem) {
                Expression expr = ((SelectExpressionItem) item).getExpression();
                collectColumns(expr, aliasMap, defaultTable, appId, sqlSignature, sqlPreview, sourceLocation, "SELECT", "READ", results);
            } else if (item instanceof AllTableColumns) {
                AllTableColumns atc = (AllTableColumns) item;
                String tableName = resolveTableName(atc.getTable(), aliasMap);
                if (tableName == null) {
                    tableName = defaultTable;
                }
                ParsedColumnRef ref = createRef(appId, tableName, "*", sqlSignature, sqlPreview, sourceLocation, "SELECT", "READ");
                results.add(ref);
            }
        }

        // WHERE -> WHERE
        if (ps.getWhere() != null) {
            collectColumns(ps.getWhere(), aliasMap, defaultTable, appId, sqlSignature, sqlPreview, sourceLocation, "SELECT", "WHERE", results);
        }

        // JOIN ON expressions -> JOIN
        if (ps.getJoins() != null) {
            for (Join join : ps.getJoins()) {
                if (join.getOnExpression() != null) {
                    collectColumns(join.getOnExpression(), aliasMap, defaultTable, appId, sqlSignature, sqlPreview, sourceLocation, "SELECT", "JOIN", results);
                }
            }
        }

        // GROUP BY -> READ
        if (ps.getGroupBy() != null) {
            for (Expression expr : ps.getGroupBy().getGroupByExpressions()) {
                collectColumns(expr, aliasMap, defaultTable, appId, sqlSignature, sqlPreview, sourceLocation, "SELECT", "READ", results);
            }
        }

        // ORDER BY -> READ
        if (ps.getOrderByElements() != null) {
            for (OrderByElement obe : ps.getOrderByElements()) {
                collectColumns(obe.getExpression(), aliasMap, defaultTable, appId, sqlSignature, sqlPreview, sourceLocation, "SELECT", "READ", results);
            }
        }

        // HAVING -> WHERE
        if (ps.getHaving() != null) {
            collectColumns(ps.getHaving(), aliasMap, defaultTable, appId, sqlSignature, sqlPreview, sourceLocation, "SELECT", "WHERE", results);
        }
    }

    private void extractFromInsert(Insert insert, String appId, String sqlSignature,
                                   String sqlPreview, String sourceLocation, List<ParsedColumnRef> results) {
        String tableName = insert.getTable() != null ? insert.getTable().getName() : null;

        // Column list -> WRITE
        if (insert.getColumns() != null) {
            for (Column col : insert.getColumns()) {
                String colName = col.getColumnName();
                ParsedColumnRef ref = createRef(appId, tableName, colName, sqlSignature, sqlPreview, sourceLocation, "INSERT", "WRITE");
                results.add(ref);
            }
        }

        // VALUES expressions / SELECT subquery -> READ
        if (insert.getItemsList() != null) {
            collectColumnsFromItemsList(insert.getItemsList(), appId, sqlSignature, sqlPreview, sourceLocation, "INSERT", "READ", results);
        }

        // Also handle SELECT subquery in INSERT ... SELECT
        if (insert.getSelect() != null) {
            extractFromSelect(insert.getSelect(), appId, sqlSignature, sqlPreview, sourceLocation, results);
        }
    }

    private void extractFromUpdate(Update update, String appId, String sqlSignature,
                                   String sqlPreview, String sourceLocation, List<ParsedColumnRef> results) {
        String tableName = update.getTable() != null ? update.getTable().getName() : null;
        Map<String, String> aliasMap = new HashMap<>();
        if (update.getTable() != null && update.getTable().getAlias() != null) {
            aliasMap.put(update.getTable().getAlias().getName(), tableName);
        }

        // Use UpdateSet API for full column/expression pairs
        if (update.getUpdateSets() != null) {
            for (UpdateSet updateSet : update.getUpdateSets()) {
                List<Column> columns = updateSet.getColumns();
                List<Expression> expressions = updateSet.getExpressions();
                for (int i = 0; i < columns.size(); i++) {
                    Column col = columns.get(i);
                    String colName = col.getColumnName();
                    String resolvedTable = tableName;
                    if (col.getTable() != null && col.getTable().getName() != null) {
                        resolvedTable = aliasMap.getOrDefault(col.getTable().getName(), col.getTable().getName());
                    }
                    // SET left side -> WRITE
                    ParsedColumnRef writeRef = createRef(appId, resolvedTable, colName, sqlSignature, sqlPreview, sourceLocation, "UPDATE", "WRITE");
                    results.add(writeRef);

                    // SET right side -> READ
                    if (i < expressions.size()) {
                        Expression expr = expressions.get(i);
                        collectColumns(expr, aliasMap, tableName, appId, sqlSignature, sqlPreview, sourceLocation, "UPDATE", "READ", results);
                    }
                }
            }
        }

        // WHERE -> WHERE
        if (update.getWhere() != null) {
            collectColumns(update.getWhere(), aliasMap, tableName, appId, sqlSignature, sqlPreview, sourceLocation, "UPDATE", "WHERE", results);
        }
    }

    private void extractFromDelete(Delete delete, String appId, String sqlSignature,
                                   String sqlPreview, String sourceLocation, List<ParsedColumnRef> results) {
        String tableName = delete.getTable() != null ? delete.getTable().getName() : null;
        Map<String, String> aliasMap = new HashMap<>();
        if (delete.getTable() != null && delete.getTable().getAlias() != null) {
            aliasMap.put(delete.getTable().getAlias().getName(), tableName);
        }

        // WHERE -> WHERE
        if (delete.getWhere() != null) {
            collectColumns(delete.getWhere(), aliasMap, tableName, appId, sqlSignature, sqlPreview, sourceLocation, "DELETE", "WHERE", results);
        }
    }

    private Map<String, String> buildAliasMap(PlainSelect ps) {
        Map<String, String> aliasMap = new HashMap<>();

        FromItem from = ps.getFromItem();
        if (from instanceof Table) {
            Table t = (Table) from;
            if (t.getAlias() != null) {
                aliasMap.put(t.getAlias().getName(), t.getName());
            }
        }

        if (ps.getJoins() != null) {
            for (Join join : ps.getJoins()) {
                if (join.getRightItem() instanceof Table) {
                    Table t = (Table) join.getRightItem();
                    if (t.getAlias() != null) {
                        aliasMap.put(t.getAlias().getName(), t.getName());
                    }
                }
            }
        }

        return aliasMap;
    }

    private void collectColumns(Expression expression, Map<String, String> aliasMap, String defaultTable,
                                String appId, String sqlSignature, String sqlPreview, String sourceLocation,
                                String operationType, String operationDetail, List<ParsedColumnRef> results) {
        if (expression == null) {
            return;
        }
        ColumnCollectingVisitor visitor = new ColumnCollectingVisitor();
        expression.accept(visitor);
        for (Column col : visitor.getColumns()) {
            String tableName = resolveTableName(col.getTable(), aliasMap);
            if (tableName == null) {
                tableName = defaultTable;
            }
            String colName = col.getColumnName();
            ParsedColumnRef ref = createRef(appId, tableName, colName, sqlSignature, sqlPreview, sourceLocation, operationType, operationDetail);
            results.add(ref);
        }
    }

    private String resolveTableName(Table table, Map<String, String> aliasMap) {
        if (table == null || table.getName() == null) {
            return null;
        }
        String name = table.getName();
        return aliasMap.getOrDefault(name, name);
    }

    private String extractDefaultTable(PlainSelect ps) {
        FromItem from = ps.getFromItem();
        if (from instanceof Table) {
            Table t = (Table) from;
            return t.getName();
        }
        return null;
    }

    private void collectColumnsFromItemsList(ItemsList itemsList, String appId,
                                             String sqlSignature, String sqlPreview, String sourceLocation,
                                             String operationType, String operationDetail, List<ParsedColumnRef> results) {
        if (itemsList instanceof ExpressionList) {
            ExpressionList exprList = (ExpressionList) itemsList;
            for (Expression expr : exprList.getExpressions()) {
                collectColumns(expr, Collections.emptyMap(), null, appId, sqlSignature, sqlPreview, sourceLocation, operationType, operationDetail, results);
            }
        } else if (itemsList instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) itemsList;
            if (subSelect.getSelectBody() instanceof PlainSelect) {
                Select select = new Select();
                select.setSelectBody(subSelect.getSelectBody());
                extractFromSelect(select, appId, sqlSignature, sqlPreview, sourceLocation, results);
            }
        }
        // MultiExpressionList is also possible but we handle the common cases
    }

    private ParsedColumnRef createRef(String appId, String tableName, String columnName,
                                      String sqlSignature, String sqlPreview, String sourceLocation,
                                      String operationType, String operationDetail) {
        ParsedColumnRef ref = new ParsedColumnRef();
        ref.setAppId(appId);
        ref.setTableName(tableName);
        ref.setColumnName(columnName);
        ref.setSqlSignature(sqlSignature);
        ref.setSqlPreview(sqlPreview);
        ref.setOperationType(operationType);
        ref.setOperationDetail(operationDetail);
        ref.setSourceLocation(sourceLocation);
        return ref;
    }

    private void deduplicate(List<ParsedColumnRef> results) {
        Set<ParsedColumnRef> seen = new LinkedHashSet<>();
        List<ParsedColumnRef> deduped = new ArrayList<>();
        for (ParsedColumnRef ref : results) {
            if (seen.add(ref)) {
                deduped.add(ref);
            }
        }
        results.clear();
        results.addAll(deduped);
    }

    private String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
    }

    private static class ColumnCollectingVisitor extends ExpressionVisitorAdapter {
        private final List<Column> columns = new ArrayList<>();

        @Override
        public void visit(Column column) {
            columns.add(column);
            super.visit(column);
        }

        public List<Column> getColumns() {
            return columns;
        }
    }
}
