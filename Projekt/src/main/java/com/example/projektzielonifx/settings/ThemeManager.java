package com.example.projektzielonifx.settings;

import javafx.scene.Scene;

import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class ThemeManager {
    // Theme style sheet paths
    protected static final String LIGHT_THEME = "/com/example/projektzielonifx/styles.css";
    protected static final String DARK_THEME = "/com/example/projektzielonifx/dark-theme.css";

    // Preference key for saving theme selection
    protected static final String PREF_THEME = "theme";
    protected static final String THEME_LIGHT = "light";
    protected static final String THEME_DARK = "dark";

    // Singleton instance
    protected static ThemeManager instance;

    // List of all scenes in the application
    protected final List<Scene> managedScenes = new ArrayList<>();

    // Current theme
    protected boolean isDarkMode;
    protected final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);

    /**
     * protected constructor for singleton pattern
     */
    protected ThemeManager() {
        // Load previously saved theme preference
        String savedTheme = prefs.get(PREF_THEME, THEME_LIGHT);
        isDarkMode = savedTheme.equals(THEME_DARK);
    }

    /**
     * Get the singleton instance of ThemeManager
     * @return ThemeManager instance
     */
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Register a scene to be managed by the ThemeManager
     * @param scene The Scene to manage
     */
    public void addManagedScene(Scene scene) {
        managedScenes.add(scene);
        applyThemeToScene(scene);
    }

    /**
     * Remove a scene from theme management
     * @param scene The Scene to remove
     */
    public void removeManagedScene(Scene scene) {
        managedScenes.remove(scene);
    }

    /**
     * Toggle between light and dark theme
     */
    public void toggleTheme() {
        isDarkMode = !isDarkMode;
        saveThemePreference();

        // Apply theme to all managed scenes
        managedScenes.forEach(this::applyThemeToScene);
    }

    /**
     * Set theme explicitly
     * @param darkMode true for dark mode, false for light mode
     */
    public void setTheme(boolean darkMode) {
        if (this.isDarkMode != darkMode) {
            this.isDarkMode = darkMode;
            saveThemePreference();

            // Apply theme to all managed scenes
            managedScenes.forEach(this::applyThemeToScene);
        }
    }

    /**
     * Check if dark mode is currently enabled
     * @return true if dark mode is enabled
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }

    /**
     * Apply the current theme to a specific scene
     * @param scene The Scene to apply the theme to
     */
    protected void applyThemeToScene(Scene scene) {
        // Remove old theme
        scene.getStylesheets().clear();

        // Add new theme
        URL themeUrl = getClass().getResource(isDarkMode ? DARK_THEME : LIGHT_THEME);
        if (themeUrl != null) {
            scene.getStylesheets().add(themeUrl.toExternalForm());
        } else {
            System.err.println("Theme stylesheet not found: " + (isDarkMode ? DARK_THEME : LIGHT_THEME));
        }
    }

    /**
     * Save the current theme preference
     */
    protected void saveThemePreference() {
        prefs.put(PREF_THEME, isDarkMode ? THEME_DARK : THEME_LIGHT);
    }

    /**
     * Update specific ImageView graphics to match the current theme
     * This method is useful for icons that need to change with the theme
     * @param imageView The ImageView to update
     * @param lightImagePath Path to the light theme image
     * @param darkImagePath Path to the dark theme image
     */
    public void updateThemedImage(ImageView imageView, String lightImagePath, String darkImagePath) {
        String path = isDarkMode ? darkImagePath : lightImagePath;
        try {
            Image image = new Image(getClass().getResourceAsStream(path));
            imageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Failed to load themed image: " + path);
            e.printStackTrace();
        }
    }
}