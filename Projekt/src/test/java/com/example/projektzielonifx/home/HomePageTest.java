package com.example.projektzielonifx.home;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testy jednostkowe klasy HomePage.
 * Sprawdzają przypisanie ID i wywołanie initData() na kontrolerach – bez użycia Mockito.
 */
public class HomePageTest {

    private HomePage homePage;
    private StubNavController stubNavController;
    private StubMainContentController stubMainContentController;

    @BeforeEach
    void setUp() {
        homePage = new HomePage();
        stubNavController = new StubNavController();
        stubMainContentController = new StubMainContentController();

        homePage.navPanelController = stubNavController;
        homePage.mainContentController = stubMainContentController;
    }

    /**
     * Test sprawdza, czy metoda initializeWithId
     * przypisuje ID i wywołuje initData na kontrolerach.
     */
    @Test
    void testInitializeWithId() {
        int userId = 123;

        homePage.initializeWithId(userId);

        assertEquals(userId, homePage.userId);
        assertEquals(userId, stubNavController.receivedUserId);
        assertEquals(userId, stubMainContentController.receivedUserId);

        System.out.println("Test initializeWithId wykonany poprawnie.");
    }

    // Stub NavController
    private static class StubNavController extends NavController {
        int receivedUserId = -1;

        @Override
        public void initData(int userId) {
            this.receivedUserId = userId;
        }
    }

    // Stub MainContentController
    private static class StubMainContentController extends MainContentController {
        int receivedUserId = -1;

        @Override
        public void initData(int userId) {
            this.receivedUserId = userId;
        }
    }
}
