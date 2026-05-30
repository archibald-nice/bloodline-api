package com.bloodline.service.service;

import com.bloodline.domain.entity.LineageColumnRef;
import com.bloodline.domain.mapper.LineageColumnRefMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImpactAnalysisServiceTest {

    @Mock
    private LineageColumnRefMapper columnRefMapper;

    @InjectMocks
    private ImpactAnalysisService impactAnalysisService;

    @Test
    void testAnalyzeImpact() {
        // Setup: orders.amount referenced by order-service and price-service in same SQL
        // plus discount_rate co-occurring in same SQL
        LineageColumnRef ref1 = createRef("order-service", "orders", "amount",
                "sql-001", "SELECT amount, discount_rate, created_at FROM orders",
                "SELECT", "orders.amount in SELECT clause");
        LineageColumnRef ref2 = createRef("price-service", "orders", "amount",
                "sql-001", "SELECT amount, discount_rate, created_at FROM orders",
                "SELECT", "orders.amount in SELECT clause");
        LineageColumnRef ref3 = createRef("order-service", "orders", "discount_rate",
                "sql-001", "SELECT amount, discount_rate, created_at FROM orders",
                "SELECT", "orders.discount_rate in SELECT clause");
        LineageColumnRef ref4 = createRef("order-service", "orders", "created_at",
                "sql-001", "SELECT amount, discount_rate, created_at FROM orders",
                "SELECT", "orders.created_at in SELECT clause");

        when(columnRefMapper.findByColumn("orders", "amount"))
                .thenReturn(Arrays.asList(ref1, ref2));
        when(columnRefMapper.findBySqlSignature("sql-001"))
                .thenReturn(Arrays.asList(ref1, ref2, ref3, ref4));

        ImpactAnalysisService.ImpactRequest request = new ImpactAnalysisService.ImpactRequest(
                Collections.singletonList(
                        new ImpactAnalysisService.ChangeItem("app1", "orders", "amount", "MODIFY")
                )
        );

        ImpactAnalysisService.ImpactReport report = impactAnalysisService.analyze(request);

        // Verify summary: 2 apps affected, 3 fields related, 1 SQL involved
        assertThat(report.getSummary().getTotalAppsAffected()).isEqualTo(2);
        assertThat(report.getSummary().getTotalFieldsRelated()).isEqualTo(3);
        assertThat(report.getSummary().getTotalSqlsInvolved()).isEqualTo(1);

        // Verify appsAffected
        assertThat(report.getAppsAffected()).hasSize(2);

        ImpactAnalysisService.AppImpact orderServiceImpact = report.getAppsAffected().stream()
                .filter(a -> "order-service".equals(a.getAppId()))
                .findFirst()
                .orElse(null);
        assertThat(orderServiceImpact).isNotNull();
        assertThat(orderServiceImpact.getAffectedColumns()).hasSize(1);
        assertThat(orderServiceImpact.getAffectedColumns().get(0).getTableName()).isEqualTo("orders");
        assertThat(orderServiceImpact.getAffectedColumns().get(0).getColumnName()).isEqualTo("amount");
        assertThat(orderServiceImpact.getCoOccurredColumns()).hasSize(2);
        assertThat(orderServiceImpact.getCoOccurredColumns())
                .extracting(c -> c.getTableName() + "." + c.getColumnName())
                .containsExactlyInAnyOrder("orders.discount_rate", "orders.created_at");

        ImpactAnalysisService.AppImpact priceServiceImpact = report.getAppsAffected().stream()
                .filter(a -> "price-service".equals(a.getAppId()))
                .findFirst()
                .orElse(null);
        assertThat(priceServiceImpact).isNotNull();
        assertThat(priceServiceImpact.getAffectedColumns()).hasSize(1);
        assertThat(priceServiceImpact.getCoOccurredColumns()).isEmpty();

        // Verify crossFieldRelations contains orders.amount + orders.discount_rate
        assertThat(report.getCrossFieldRelations()).hasSize(1);
        ImpactAnalysisService.CrossFieldRelation crossField = report.getCrossFieldRelations().get(0);
        assertThat(crossField.getSqlSignature()).isEqualTo("sql-001");
        assertThat(crossField.getSqlPreview()).isEqualTo("SELECT amount, discount_rate, created_at FROM orders");
        assertThat(crossField.getColumns()).containsExactlyInAnyOrder("orders.amount", "orders.discount_rate", "orders.created_at");
        assertThat(crossField.getAppsInvolved()).containsExactlyInAnyOrder("order-service", "price-service");
    }

    @Test
    void testAnalyzeImpactWithEmptyRequest() {
        ImpactAnalysisService.ImpactReport report = impactAnalysisService.analyze(
                new ImpactAnalysisService.ImpactRequest(Collections.emptyList())
        );

        assertThat(report.getSummary().getTotalAppsAffected()).isEqualTo(0);
        assertThat(report.getSummary().getTotalFieldsRelated()).isEqualTo(0);
        assertThat(report.getSummary().getTotalSqlsInvolved()).isEqualTo(0);
        assertThat(report.getAppsAffected()).isEmpty();
        assertThat(report.getCrossFieldRelations()).isEmpty();
    }

    @Test
    void testAnalyzeImpactWithNullRequest() {
        ImpactAnalysisService.ImpactReport report = impactAnalysisService.analyze(null);

        assertThat(report.getSummary().getTotalAppsAffected()).isEqualTo(0);
        assertThat(report.getSummary().getTotalFieldsRelated()).isEqualTo(0);
        assertThat(report.getSummary().getTotalSqlsInvolved()).isEqualTo(0);
        assertThat(report.getAppsAffected()).isEmpty();
        assertThat(report.getCrossFieldRelations()).isEmpty();
    }

    @Test
    void testRiskLevel_HIGH_whenManyAppsAffected() {
        // 3 apps affected + 10+ related fields = HIGH
        LineageColumnRef ref1 = createRef("app1", "orders", "amount", "sql-001", "SELECT amount FROM orders", "SELECT", "READ");
        LineageColumnRef ref2 = createRef("app2", "orders", "amount", "sql-001", "SELECT amount FROM orders", "SELECT", "READ");
        LineageColumnRef ref3 = createRef("app3", "orders", "amount", "sql-001", "SELECT amount FROM orders", "SELECT", "READ");
        LineageColumnRef ref4 = createRef("app4", "orders", "amount", "sql-001", "SELECT amount FROM orders", "SELECT", "READ");

        when(columnRefMapper.findByColumn("orders", "amount"))
                .thenReturn(Arrays.asList(ref1, ref2, ref3, ref4));
        when(columnRefMapper.findBySqlSignature("sql-001"))
                .thenReturn(Arrays.asList(ref1, ref2, ref3, ref4));

        ImpactAnalysisService.ImpactRequest request = new ImpactAnalysisService.ImpactRequest(
                Collections.singletonList(new ImpactAnalysisService.ChangeItem("app1", "orders", "amount", "MODIFY"))
        );

        ImpactAnalysisService.ImpactReport report = impactAnalysisService.analyze(request);
        assertThat(report.getSummary().getRiskLevel()).isEqualTo("HIGH");
        assertThat(report.getSummary().getRiskReason()).contains("4");
    }

    @Test
    void testRiskLevel_MEDIUM_whenCrossApp() {
        // 2 apps affected = MEDIUM
        LineageColumnRef ref1 = createRef("app1", "orders", "amount", "sql-001", "SELECT amount FROM orders", "SELECT", "READ");
        LineageColumnRef ref2 = createRef("app2", "orders", "amount", "sql-001", "SELECT amount FROM orders", "SELECT", "READ");

        when(columnRefMapper.findByColumn("orders", "amount"))
                .thenReturn(Arrays.asList(ref1, ref2));
        when(columnRefMapper.findBySqlSignature("sql-001"))
                .thenReturn(Arrays.asList(ref1, ref2));

        ImpactAnalysisService.ImpactRequest request = new ImpactAnalysisService.ImpactRequest(
                Collections.singletonList(new ImpactAnalysisService.ChangeItem("app1", "orders", "amount", "MODIFY"))
        );

        ImpactAnalysisService.ImpactReport report = impactAnalysisService.analyze(request);
        assertThat(report.getSummary().getRiskLevel()).isEqualTo("MEDIUM");
    }

    @Test
    void testRiskLevel_LOW_whenSingleApp() {
        // 1 app affected = LOW
        LineageColumnRef ref1 = createRef("app1", "orders", "amount", "sql-001", "SELECT amount FROM orders", "SELECT", "READ");

        when(columnRefMapper.findByColumn("orders", "amount"))
                .thenReturn(Collections.singletonList(ref1));
        when(columnRefMapper.findBySqlSignature("sql-001"))
                .thenReturn(Collections.singletonList(ref1));

        ImpactAnalysisService.ImpactRequest request = new ImpactAnalysisService.ImpactRequest(
                Collections.singletonList(new ImpactAnalysisService.ChangeItem("app1", "orders", "amount", "MODIFY"))
        );

        ImpactAnalysisService.ImpactReport report = impactAnalysisService.analyze(request);
        assertThat(report.getSummary().getRiskLevel()).isEqualTo("LOW");
    }

    private LineageColumnRef createRef(String appId, String tableName, String columnName,
                                       String sqlSignature, String sqlPreview,
                                       String operationType, String operationDetail) {
        LineageColumnRef ref = new LineageColumnRef();
        ref.setAppId(appId);
        ref.setTableName(tableName);
        ref.setColumnName(columnName);
        ref.setSqlSignature(sqlSignature);
        ref.setSqlPreview(sqlPreview);
        ref.setOperationType(operationType);
        ref.setOperationDetail(operationDetail);
        return ref;
    }
}
