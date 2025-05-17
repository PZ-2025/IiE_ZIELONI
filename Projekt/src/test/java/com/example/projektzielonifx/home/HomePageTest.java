package com.example.projektzielonifx.home;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 * Test jednostkowy dla klasy HomePage.
 * Testuje logikę przekazywania identyfikatora użytkownika do kontrolerów podrzędnych.
 */
public class HomePageTest {

    private HomePage homePage;
    private NavController mockNavController;
    private MainContentController mockMainContentController;

    @BeforeEach
    public void setUp() {
        homePage = new HomePage();
        mockNavController = mock(NavController.class);
        mockMainContentController = mock(MainContentController.class);

        // Ręczne wstrzyknięcie zależności
        homePage.navPanelController = mockNavController;
        homePage.mainContentController = mockMainContentController;
    }

    /**
     * Sprawdza, czy metoda initializeWithId poprawnie ustawia userId
     * i wywołuje odpowiednie metody inicjalizacji kontrolerów podrzędnych.
     */
    @Test
    public void testInitializeWithId() {
        int userId = 42;

        homePage.initializeWithId(userId);

        verify(mockNavController).initData(userId);
        verify(mockMainContentController).initData(userId);
    }
}
