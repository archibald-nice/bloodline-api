package com.bloodline.service.service;

import com.bloodline.common.context.TenantContext;
import com.bloodline.domain.entity.LineageSnapshot;
import com.bloodline.domain.mapper.LineageEdgeV2Mapper;
import com.bloodline.domain.mapper.LineageSnapshotMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceTest {

    @Mock
    private LineageSnapshotMapper snapshotMapper;

    @Mock
    private LineageEdgeV2Mapper edgeMapper;

    private SnapshotService snapshotService;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);
        snapshotService = new SnapshotService(edgeMapper, snapshotMapper);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testGetSnapshot() {
        LineageSnapshot expected = new LineageSnapshot();
        expected.setId(1L);
        expected.setSnapshotName("test");
        when(snapshotMapper.findById(1L)).thenReturn(expected);

        LineageSnapshot result = snapshotService.getSnapshot(1L);

        assertEquals("test", result.getSnapshotName());
        verify(snapshotMapper).findById(1L);
    }

    @Test
    void testCreateSnapshotWithoutTenantThrows() {
        TenantContext.clear();

        assertThrows(IllegalStateException.class, () -> {
            snapshotService.createSnapshot("name", "BRANCH", "ref");
        });
    }
}
