package com.raports.raportlibrary;

import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Klasa {@code com.raports.raportlibrary.ProjectProgressReportGenerator} jest odpowiedzialna za generowanie
 * raportów PDF dotyczących postępu projektów.
 * <p>
 * Umożliwia tworzenie raportów dla jednego lub wielu projektów, z opcjonalnym
 * filtrowaniem wyników na podstawie statusu projektu oraz ID menedżera projektu.
 * Dane do raportu pobierane są z bazy danych, w szczególności z widoku
 * {@code vw_ProjectProgress}.
 * </p>
 * <p>
 * Raport zawiera kluczowe informacje o projekcie, takie jak: nazwa projektu, menedżer,
 * status, ogólny postęp, liczba kamieni milowych i ich średni postęp, liczba zadań
 * (wszystkich, ukończonych, anulowanych), zaangażowane zespoły oraz ich liderzy,
 * a także listy nazw kamieni milowych i tytułów zadań.
 * </p>
 * <p>
 * Do generowania dokumentów PDF wykorzystywana jest biblioteka iText 7.
 * Raporty są zapisywane w wybranym przez użytkownika folderze lub, jeśli nie zostanie
 * on podany, w folderze "Dokumenty" użytkownika. Nazwa pliku może być dostosowana
 * lub generowana automatycznie z sygnaturą czasową.
 * </p>
 * W raportach używana jest czcionka DejaVuSans, która jest osadzana w dokumencie PDF
 * w celu zapewnienia poprawnego wyświetlania polskich znaków.
 */
public class ProjectProgressReportGenerator {
    /**
     * Generuje raport postępu dla pojedynczego projektu, używając domyślnych ustawień
     * (bez filtrowania według statusu czy menedżera).
     * <p>
     * Jest to metoda pomocnicza, która deleguje zadanie do
     * {@link #generateFilteredReport(int, String, File, String, Integer)}.
     * </p>
     *
     * @param projectId         ID projektu, dla którego ma zostać wygenerowany raport.
     * @param customFileName    Opcjonalna, niestandardowa nazwa pliku (bez rozszerzenia .pdf).
     *                          Jeśli {@code null} lub pusty, nazwa pliku zostanie wygenerowana automatycznie.
     * @param selectedDirectory Katalog, w którym ma zostać zapisany raport. Jeśli {@code null},
     *                          raport zostanie zapisany w domyślnym folderze "Dokumenty".
     * @throws SQLException jeśli wystąpi błąd podczas dostępu do bazy danych.
     * @throws IOException  jeśli wystąpi błąd podczas operacji wejścia/wyjścia, np. zapisu pliku
     *                      lub odczytu pliku czcionki.
     */

    public static void generateReport(int projectId, String customFileName, File selectedDirectory) throws SQLException, IOException {
        generateFilteredReport(projectId, customFileName, selectedDirectory, null, null);
    }
    /**
     * Generuje raport postępu dla listy projektów, używając domyślnych ustawień
     * (bez filtrowania według statusu czy menedżera).
     * <p>
     * Jest to metoda pomocnicza, która deleguje zadanie do
     * {@link #generateMultipleFilteredReport(List, String, File, String, Integer)}.
     * </p>
     *
     * @param projectIds        Lista identyfikatorów (ID) projektów, dla których ma zostać wygenerowany raport.
     * @param customFileName    Opcjonalna, niestandardowa nazwa pliku (bez rozszerzenia .pdf).
     * @param selectedDirectory Katalog, w którym ma zostać zapisany raport.
     * @throws SQLException jeśli wystąpi błąd podczas dostępu do bazy danych.
     * @throws IOException  jeśli wystąpi błąd podczas operacji wejścia/wyjścia.
     */

    public static void generateMultipleProjectReport(List<Integer> projectIds, String customFileName, File selectedDirectory) throws SQLException, IOException {
        generateMultipleFilteredReport(projectIds, customFileName, selectedDirectory, null, null);
    }
    /**
     * Generuje raport postępu dla pojedynczego projektu z możliwością filtrowania
     * według statusu projektu i ID menedżera.
     * <p>
     * Metoda ta tworzy listę zawierającą pojedyncze ID projektu i następnie deleguje
     * zadanie do {@link #generateMultipleFilteredReport(List, String, File, String, Integer)}.
     * </p>
     *
     * @param projectId         ID projektu, dla którego generowany jest raport.
     * @param customFileName    Opcjonalna, niestandardowa nazwa pliku.
     * @param selectedDirectory Katalog zapisu raportu.
     * @param projectStatus     Filtr statusu projektu (np. "Aktywny"). Może być {@code null}.
     * @param managerId         Filtr ID menedżera projektu. Może być {@code null}.
     * @throws SQLException jeśli wystąpi błąd podczas interakcji z bazą danych.
     * @throws IOException  jeśli wystąpi błąd wejścia/wyjścia.
     */
    public static void generateFilteredReport(int projectId, String customFileName, File selectedDirectory, 
                                             String projectStatus, Integer managerId) throws SQLException, IOException {
        List<Integer> projectIds = List.of(projectId);
        generateMultipleFilteredReport(projectIds, customFileName, selectedDirectory, projectStatus, managerId);
    }
    /**
     * Generuje raport PDF dotyczący postępu dla listy określonych projektów,
     * z możliwością filtrowania wyników na podstawie statusu projektu oraz ID menedżera.
     * <p>
     * Proces generowania obejmuje:
     * <ol>
     *     <li>Przygotowanie nazwy pliku i ścieżki zapisu.</li>
     *     <li>Załadowanie i skonfigurowanie czcionki DejaVuSans.</li>
     *     <li>Zbudowanie dynamicznego zapytania SQL do bazy danych na podstawie przekazanych ID projektów
     *         oraz opcjonalnych filtrów statusu i menedżera. Dane pobierane są z widoku {@code vw_ProjectProgress}.</li>
     *     <li>Nawiązanie połączenia z bazą danych i wykonanie zapytania dla każdego projektu z listy.</li>
     *     <li>Utworzenie dokumentu PDF i dodanie do niego tytułu oraz sygnatury czasowej.</li>
     *     <li>Dla każdego projektu, który spełnia kryteria (w tym filtry), dodawana jest sekcja
     *         do raportu zawierająca:
     *         <ul>
     *             <li>Nagłówek z nazwą projektu (jeśli raport dotyczy wielu projektów).</li>
     *             <li>Tabelę z kluczowymi informacjami: nazwa projektu, menedżer, status, ogólny postęp,
     *                 liczba kamieni milowych i ich średni postęp, liczba zadań (wszystkich, ukończonych,
     *                 anulowanych), zaangażowane zespoły, liderzy zespołów.</li>
     *             <li>Sekcję z listą nazw kamieni milowych.</li>
     *             <li>Sekcję z listą tytułów zadań w projekcie.</li>
     *         </ul>
     *         Jeśli generowany jest raport dla wielu projektów, każda sekcja projektu (oprócz pierwszej)
     *         jest poprzedzona znakiem podziału strony.</li>
     *     <li>Jeśli dla żadnego z wybranych projektów (i ewentualnych filtrów) nie znaleziono danych,
     *         do raportu dodawany jest odpowiedni komunikat.</li>
     *     <li>Zamknięcie dokumentu PDF i zasobów bazodanowych.</li>
     * </ol>
     * </p>
     *
     * @param projectIds        Lista identyfikatorów (ID) projektów, dla których ma zostać wygenerowany raport.
     *                          Nie może być {@code null}.
     * @param customFileName    Opcjonalna, niestandardowa nazwa pliku (bez rozszerzenia .pdf).
     *                          Jeśli {@code null} lub pusty, nazwa pliku zostanie wygenerowana automatycznie
     *                          z prefiksem "Raport_postepu_projektu_" i sygnaturą czasową.
     * @param selectedDirectory Katalog {@link File}, w którym ma zostać zapisany wygenerowany raport.
     *                          Jeśli {@code null}, raport zostanie zapisany w domyślnym folderze "Dokumenty"
     *                          użytkownika.
     * @param projectStatus     Opcjonalny filtr statusu projektu. Jeśli podany, raport będzie zawierał tylko
     *                          projekty o tym statusie. Może być {@code null}.
     * @param managerId         Opcjonalny filtr ID menedżera projektu. Jeśli podany, raport będzie zawierał tylko
     *                          projekty zarządzane przez tego menedżera. Może być {@code null}.
     * @throws SQLException jeśli wystąpi błąd podczas interakcji z bazą danych.
     * @throws IOException  jeśli wystąpi błąd podczas operacji wejścia/wyjścia.
     */
    public static void generateMultipleFilteredReport(List<Integer> projectIds, String customFileName, File selectedDirectory, 
                                                    String projectStatus, Integer managerId) throws SQLException, IOException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String fileName = (customFileName != null && !customFileName.isEmpty()) 
                ? customFileName + ".pdf" 
                : "Raport_postepu_projektu_" + timestamp + ".pdf";

        File file = (selectedDirectory != null) 
                ? new File(selectedDirectory, fileName) 
                : new File(System.getProperty("user.home"), "Documents/" + fileName);

        InputStream fontStream = ProjectProgressReportGenerator.class.getResourceAsStream("/fonts/DejaVuSans.ttf");
        FontProgram fontProgram = FontProgramFactory.createFont(fontStream.readAllBytes());
        PdfFont font = PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

        StringBuilder queryBuilder = new StringBuilder("""
        SELECT
            project,
            manager,
            status,
            overall_progress,
            total_milestones,
            milestone_names,
            total_tasks,
            task_titles,
            completed_tasks,
            canceled_tasks,
            avg_milestone_progress,
            involved_teams,
            team_leaders
        FROM vw_ProjectProgress
        WHERE project_id = ?
        """);

        // Dodanie filtrów do zapytania
        if (projectStatus != null && !projectStatus.isEmpty()) {
            queryBuilder.append(" AND status = ?");
        }

        if (managerId != null) {
            queryBuilder.append(" AND manager_id = ?");
        }

        String query = queryBuilder.toString();
        // Generowanie dokumentu PDF
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             PdfWriter writer = new PdfWriter(file);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.setFont(font);

            // Nagłówek raportu
            document.add(new Paragraph(projectIds.size() > 1 ? "RAPORT POSTĘPU PROJEKTÓW" : "RAPORT POSTĘPU PROJEKTU")
                    .setFontSize(20).setBold()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

            document.add(new Paragraph("Wygenerowano: " + timestamp)
                    .setFontSize(10).setItalic()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            boolean hasData = false;

            // Process each project
            for (int i = 0; i < projectIds.size(); i++) {
                int projectId = projectIds.get(i);
                int paramIndex = 1;

                // Ustawienie parametrów zapytania
                stmt.setInt(paramIndex++, projectId);


                if (projectStatus != null && !projectStatus.isEmpty()) {
                    stmt.setString(paramIndex++, projectStatus);
                }

                if (managerId != null) {
                    stmt.setInt(paramIndex++, managerId);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        hasData = true;

                        // Podział strony dla kolejnych projektów
                        if (i > 0) {
                            document.add(new AreaBreak());
                        }

                        // Sekcja projektu
                        Div projectDiv = new Div();
                        projectDiv.setKeepTogether(true);

                        // Nagłówek projektu (tylko dla wielu projektów)
                        if (projectIds.size() > 1) {
                            projectDiv.add(new Paragraph("Projekt: " + rs.getString("project"))
                                    .setFontSize(16)
                                    .setBold()
                                    .setMarginTop(0)
                                    .setMarginBottom(10));
                        }

                        // tablica z informacjami
                        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                                .useAllAvailableWidth().setMarginBottom(20);

                        String[][] rows = {
                                {"Projekt", rs.getString("project")},
                                {"Menedżer", rs.getString("manager")},
                                {"Status", rs.getString("status")},
                                {"Progres całkowity", rs.getString("overall_progress") + "%"},
                                {"Liczba kamieni milowych", rs.getString("total_milestones")},
                                {"Średni postęp kamieni", rs.getString("avg_milestone_progress") + "%"},
                                {"Liczba zadań", rs.getString("total_tasks")},
                                {"Ukończone zadania", rs.getString("completed_tasks")},
                                {"Anulowane zadania", rs.getString("canceled_tasks")},
                                {"Zespoły", Optional.ofNullable(rs.getString("involved_teams")).orElse("Brak")},
                                {"Liderzy zespołów", Optional.ofNullable(rs.getString("team_leaders")).orElse("Brak")}
                        };

                        for (int j = 0; j < rows.length; j++) {
                            Cell key = new Cell().add(new Paragraph(rows[j][0]).setFont(font)).setBold();
                            Cell value = new Cell().add(new Paragraph(rows[j][1]).setFont(font));
                            if (j % 2 == 0) {
                                key.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                                value.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                            }
                            infoTable.addCell(key);
                            infoTable.addCell(value);
                        }

                        projectDiv.add(infoTable);

                        projectDiv.add(new Paragraph("Kamienie milowe:")
                                .setFontSize(12).setBold().setMarginBottom(4));
                        projectDiv.add(new Paragraph(Optional.ofNullable(rs.getString("milestone_names")).orElse("Brak"))
                                .setFont(font).setMarginBottom(15));

                        projectDiv.add(new Paragraph("Zadania w projekcie:")
                                .setFontSize(12).setBold().setMarginBottom(4));
                        projectDiv.add(new Paragraph(Optional.ofNullable(rs.getString("task_titles")).orElse("Brak"))
                                .setFont(font));


                        document.add(projectDiv);
                    }
                }
            }
                // Komunikat gdy brak danych
            if (!hasData) {
                Div messageDiv = new Div();
                messageDiv.setKeepTogether(true);
                messageDiv.add(new Paragraph("Brak danych dla wybranych projektów.").setFont(font));
                document.add(messageDiv);
            }
        }

        System.out.println("Raport zapisany jako: " + file.getAbsolutePath());
    }
}
