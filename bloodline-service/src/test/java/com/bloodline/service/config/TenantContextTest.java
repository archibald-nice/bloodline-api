package com.bloodline.service.config;

import com.bloodline.common.context.TenantContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @Test
    void testSetAndGetTenant() {
        TenantContext.setCurrentTenant(42L);
        assertEquals(Long.valueOf(42), TenantContext.getCurrentTenant());
        TenantContext.clear();
        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void testClearRemovesTenant() {
        TenantContext.setCurrentTenant(99L);
        assertNotNull(TenantContext.getCurrentTenant());
        TenantContext.clear();
        assertNull(TenantContext.getCurrentTenant());
    }
}
