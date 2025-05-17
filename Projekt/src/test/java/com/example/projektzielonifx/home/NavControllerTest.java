package com.example.projektzielonifx.home;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NavControllerTest {

    @Test
    public void testInitData_setsUserIdCorrectly() {
        NavController controller = new NavController();
        controller.initData(42);
        // używamy refleksji, bo userId jest prywatne i nie ma gettera
        try {
            var field = NavController.class.getDeclaredField("userId");
            field.setAccessible(true);
            int value = field.getInt(controller);
            assertEquals(42, value);
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        }
    }
}
