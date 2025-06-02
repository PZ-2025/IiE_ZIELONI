package com.example.projektzielonifx.settings;

import org.junit.jupiter.api.Test;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test klasy ThemeManager.
 */
public class ThemeManagerTest {

    @Test
    public void testSingleton() {
        ThemeManager tm1 = ThemeManager.getInstance();
        ThemeManager tm2 = ThemeManager.getInstance();
        assertSame(tm1, tm2, "getInstance powinien zwracać tę samą instancję");
    }

    @Test
    public void testToggleTheme() {
        ThemeManager tm = ThemeManager.getInstance();

        boolean originalTheme = tm.isDarkMode();

        tm.toggleTheme();

        assertNotEquals(originalTheme, tm.isDarkMode(), "toggleTheme powinno zmienić isDarkMode");

        // Przywróć oryginalny stan, żeby testy były niezależne
        tm.setTheme(originalTheme);
    }

    @Test
    public void testSetThemeAndPreferenceSaved() {
        ThemeManager tm = ThemeManager.getInstance();

        tm.setTheme(true);
        assertTrue(tm.isDarkMode());

        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        assertEquals("dark", prefs.get("theme", ""), "Preferencja powinna być ustawiona na 'dark'");

        tm.setTheme(false);
        assertFalse(tm.isDarkMode());
        assertEquals("light", prefs.get("theme", ""), "Preferencja powinna być ustawiona na 'light'");

        System.out.println("Test ThemeManager wykonany poprawnie.");
    }
}
