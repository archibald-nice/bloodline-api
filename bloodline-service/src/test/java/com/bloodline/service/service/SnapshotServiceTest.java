package com.bloodline.service.service;

import com.bloodline.common.context.TenantContext;
import com.bloodline.domain.entity.LineageSnapshot;
import com.bloodline.domain.mapper.LineageEdgeV2Mapper;
import com.bloodline.domain.mapper.LineageSnapshotMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

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
        snapshotService = new SnapshotService(snapshotMapper, edgeMapper);
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
        expected.setTenantId(1L);
        when(snapshotMapper.findById(1L)).thenReturn(expected);

        LineageSnapshot result = snapshotService.getSnapshot(1L);

        assertEquals("test", result.getSnapshotName());
        verify(snapshotMapper).findById(1L);
    }

    @Test
    void testGetSnapshotNotFound() {
        when(snapshotMapper.findById(99L)).thenReturn(null);

        LineageSnapshot result = snapshotService.getSnapshot(99L);

        assertNull(result);
        verify(snapshotMapper).findById(99L);
    }

    @Test
    void testGetSnapshotWrongTenant() {
        LineageSnapshot snapshot = new LineageSnapshot();
        snapshot.setId(1L);
        snapshot.setTenantId(2L);
        when(snapshotMapper.findById(1L)).thenReturn(snapshot);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            snapshotService.getSnapshot(1L);
        });
        assertEquals("Snapshot does not belong to current tenant.", ex.getMessage());
    }

    @Test
    void testCreateSnapshotSuccess() {
        when(snapshotMapper.insert(any(LineageSnapshot.class))).thenReturn(1);

        LineageSnapshot result = snapshotService.createSnapshot("v1.0", "BRANCH", "main");

        ArgumentCaptor<LineageSnapshot> captor = ArgumentCaptor.forClass(LineageSnapshot.class);
        verify(snapshotMapper).insert(captor.capture());
        LineageSnapshot captured = captor.getValue();

        assertEquals("v1.0", captured.getSnapshotName());
        assertEquals("BRANCH", captured.getSnapshotType());
        assertEquals("main", captured.getRefId());
        assertEquals(1L, captured.getTenantId());
        assertEquals(0, captured.getEdgeCount());
        assertEquals(0, captured.getNodeCount());
    }

    @Test
    void testCreateSnapshotWithoutTenantThrows() {
        TenantContext.clear();

        assertThrows(IllegalStateException.class, () -> {
            snapshotService.createSnapshot("name", "BRANCH", "ref");
        });
    }

    @Test
    void testCreateSnapshotNullNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            snapshotService.createSnapshot(null, "BRANCH", "ref");
        });
    }

    @Test
    void testCreateSnapshotBlankNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            snapshotService.createSnapshot("   ", "BRANCH", "ref");
        });
    }

    @Test
    void testCreateSnapshotNullTypeThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            snapshotService.createSnapshot("name", null, "ref");
        });
    }

    @Test
    void testCreateSnapshotBlankTypeThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            snapshotService.createSnapshot("name", "   ", "ref");
        });
    }
}
