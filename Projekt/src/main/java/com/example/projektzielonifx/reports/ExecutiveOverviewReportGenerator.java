package org.example;

import com.example.projektzielonifx.database.DatabaseConnection;
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
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;


/**
 * Klasa {@code ExecutiveOverviewReportGenerator} służy do generowania szczegółowych
 * raportów zarządczych (Executive Overview) w formacie PDF dla poszczególnych projektów.
 * <p>
 * Raporty te dostarczają kompleksowego przeglądu projektu, obejmując jego status, postęp,
 * zaangażowane zespoły i pracowników, kamienie milowe, zadania (w tym opóźnione),
 * wskaźniki ukończenia oraz inne kluczowe metryki. Dane do raportu są pobierane
 * głównie z widoku bazy danych {@code vw_ExecutiveOverview} oraz tabeli {@code Projects}.
 * </p>
 * <p>
 * Klasa oferuje możliwość generowania raportów z zastosowaniem różnorodnych filtrów,
 * takich jak status projektu, ID menedżera, obecność opóźnionych zadań lub kamieni milowych,
 * oraz zakres procentowy ukończenia zadań.
 * </p>
 * <p>
 * Do tworzenia dokumentów PDF wykorzystywana jest biblioteka iText 7. Czcionka DejaVuSans
 * jest ładowana statycznie i buforowana ({@code cachedFont}) przy pierwszym użyciu,
 * aby zoptymalizować proces i zapewnić obsługę polskich znaków diakrytycznych.
 * Wewnętrzna klasa {@link ProjectData} (DTO) służy do przechowywania i zarządzania
 * danymi projektu pobranymi z bazy danych przed ich sformatowaniem w raporcie.
 * </p>
 * Raporty są zapisywane w wybranym przez użytkownika folderze lub w domyślnym folderze
 * "Dokumenty", z możliwością dostosowania nazwy pliku.
 */
public class ExecutiveOverviewReportGenerator {

    // Buforowana instancja czcionki, aby uniknąć wielokrotnego ładowania z pliku.
    private static PdfFont cachedFont;

    // Statyczny blok inicjalizacyjny do jednorazowego załadowania czcionki przy pierwszym
    // odwołaniu do klasy.
    static {
        try {
            InputStream fontStream = ExecutiveOverviewReportGenerator.class.getResourceAsStream("/fonts/DejaVuSans.ttf");
            if (fontStream != null) {
                FontProgram fontProgram = FontProgramFactory.createFont(fontStream.readAllBytes());
                cachedFont = PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            }
        } catch (IOException e) {
            System.err.println("Error loading font: " + e.getMessage());
        }
    }

    /**
     * Generuje raport zarządczy dla określonego projektu bez dodatkowych filtrów.
     * Jest to uproszczona metoda, która wywołuje
     * {@link #generateFilteredReport(int, String, File, String, Integer, boolean, boolean, Double, Double)}
     * z wartościami domyślnymi ({@code null} lub {@code false}) dla parametrów filtrujących.
     *
     * @param projectId         ID projektu, dla którego ma zostać wygenerowany raport.
     * @param customFileName    Opcjonalna, niestandardowa nazwa pliku raportu (bez rozszerzenia .pdf).
     *                          Jeśli {@code null} lub pusty, nazwa zostanie wygenerowana automatycznie.
     * @param selectedDirectory Katalog, w którym raport ma zostać zapisany. Jeśli {@code null},
     *                          używany jest domyślny folder "Dokumenty".
     * @throws SQLException jeśli wystąpi błąd podczas komunikacji z bazą danych.
     * @throws IOException  jeśli wystąpi błąd wejścia/wyjścia podczas tworzenia pliku raportu
     *                      lub ładowania zasobów (np. czcionki).
     */
    public static void generateReport(int projectId, String customFileName, File selectedDirectory) throws SQLException, IOException {
        generateFilteredReport(projectId, customFileName, selectedDirectory, null, null, false, false, null, null);
    }

    /**
     * Wewnętrzna klasa DTO (Data Transfer Object) służąca do przechowywania
     * informacji o projekcie pobranych z bazy danych.
     * <p>
     * Pola tej klasy odpowiadają kolumnom z widoku {@code vw_ExecutiveOverview} oraz
     * dodatkowym danym. Zawiera metody dostępowe (gettery), które obsługują
     * przypadki, gdy wartość z bazy danych jest {@code null}, zwracając wtedy
     * pusty ciąg znaków lub "0" dla wartości numerycznych, co ułatwia formatowanie raportu.
     * </p>
     * Metoda {@link #isEmpty()} pozwala sprawdzić, czy obiekt zawiera jakiekolwiek dane projektu.
     */
    private static class ProjectData {
        private String project;
        private String projectStatus;
        private String projectProgress;
        private String projectManager;
        private String teamsInvolved;
        private String employeesAssigned;
        private String milestones;
        private String totalTasks;
        private String tasksDone;
        private String tasksCanceled;
        private String taskCompletionRate;
        private String avgMilestoneProgress;
        private String overdueMilestones;
        private String overdueTasks;
        private String involvedTeams;
        private String teamLeaders;
        private String taskTitles;

        /**
         * Sprawdza, czy obiekt {@code ProjectData} zawiera jakiekolwiek dane projektu.
         * Uznaje się, że dane są puste, jeśli nazwa projektu ({@code project}) jest {@code null} lub pusta.
         * @return {@code true}, jeśli dane projektu są puste, w przeciwnym razie {@code false}.
         */
        public boolean isEmpty() {
            return project == null || project.isEmpty();
        }

        // Gettery z obsługą wartości null - zwracają pusty string lub "0"
        public String getProject() {
            return project != null ? project : "";
        }

        public String getProjectStatus() {
            return projectStatus != null ? projectStatus : "";
        }

        public String getProjectProgress() {
            return projectProgress != null ? projectProgress : "0";
        }

        public String getProjectManager() {
            return projectManager != null ? projectManager : "";
        }

        public String getTeamsInvolved() {
            return teamsInvolved != null ? teamsInvolved : "0";
        }

        public String getEmployeesAssigned() {
            return employeesAssigned != null ? employeesAssigned : "0";
        }

        public String getMilestones() {
            return milestones != null ? milestones : "0";
        }

        public String getTotalTasks() {
            return totalTasks != null ? totalTasks : "0";
        }

        public String getTasksDone() {
            return tasksDone != null ? tasksDone : "0";
        }

        public String getTasksCanceled() {
            return tasksCanceled != null ? tasksCanceled : "0";
        }

        public String getTaskCompletionRate() {
            return taskCompletionRate != null ? taskCompletionRate : "0";
        }

        public String getAvgMilestoneProgress() {
            return avgMilestoneProgress != null ? avgMilestoneProgress : "0";
        }

        public String getOverdueMilestones() {
            return overdueMilestones != null ? overdueMilestones : "0";
        }

        public String getOverdueTasks() {
            return overdueTasks != null ? overdueTasks : "0";
        }

        public String getInvolvedTeams() {
            return involvedTeams != null ? involvedTeams : "Brak";
        }

        public String getTeamLeaders() {
            return teamLeaders != null ? teamLeaders : "Brak";
        }

        public String getTaskTitles() {
            return taskTitles != null ? taskTitles : "Brak";
        }
    }
    /**
     * Generuje szczegółowy raport zarządczy PDF dla określonego projektu,
     * uwzględniając przekazane opcje filtrowania.
     * <p>
     * Proces generowania obejmuje:
     * <ol>
     *     <li>Przygotowanie nazwy pliku i ścieżki zapisu.</li>
     *     <li>Wykorzystanie pre-ładowanej czcionki DejaVuSans.</li>
     *     <li>Zbudowanie dynamicznego, zoptymalizowanego zapytania SQL do bazy danych,
     *         które pobiera dane z widoku {@code vw_ExecutiveOverview} i tabeli {@code Projects},
     *         stosując wszystkie aktywne filtry.</li>
     *     <li>Wykonanie zapytania i zmapowanie wyników do obiektu {@link ProjectData}.</li>
     *     <li>Jeśli dla danego projektu i filtrów nie znaleziono danych, generowany jest
     *         PDF z odpowiednim komunikatem.</li>
     *     <li>W przeciwnym razie, tworzony jest pełny raport zawierający:
     *         <ul>
     *             <li>Tytuł i sygnaturę czasową.</li>
     *             <li>Tabelę z kluczowymi informacjami o projekcie (status, postęp, menedżer,
     *                 liczby zespołów, pracowników, kamieni milowych, zadań, wskaźniki, opóźnienia).</li>
     *             <li>Listę zaangażowanych zespołów i ich liderów.</li>
     *             <li>Listę zadań w projekcie.</li>
     *         </ul>
     *     </li>
     *     <li>Zamknięcie dokumentu PDF i zasobów.</li>
     * </ol>
     * </p>
     *
     * @param projectId             ID projektu, dla którego generowany jest raport.
     * @param customFileName        Opcjonalna, niestandardowa nazwa pliku (bez rozszerzenia .pdf).
     * @param selectedDirectory     Katalog zapisu raportu.
     * @param projectStatus         Filtr statusu projektu (np. "Aktywny").
     * @param managerId             Filtr ID menedżera projektu.
     * @param showOverdueTasks      Flaga wskazująca, czy pokazywać tylko projekty z opóźnionymi zadaniami.
     * @param showOverdueMilestones Flaga wskazująca, czy pokazywać tylko projekty z opóźnionymi kamieniami milowymi.
     * @param minCompletionRate     Minimalny wskaźnik ukończenia zadań (0-100).
     * @param maxCompletionRate     Maksymalny wskaźnik ukończenia zadań (0-100).
     * @throws SQLException jeśli wystąpi błąd podczas interakcji z bazą danych.
     * @throws IOException  jeśli wystąpi błąd wejścia/wyjścia (np. zapis pliku, ładowanie czcionki).
     */
    public static void generateFilteredReport(int projectId, String customFileName, File selectedDirectory,
                                            String projectStatus, Integer managerId,
                                            boolean showOverdueTasks, boolean showOverdueMilestones,
                                            Double minCompletionRate, Double maxCompletionRate) throws SQLException, IOException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String fileName = (customFileName != null && !customFileName.isEmpty()) 
                ? customFileName + ".pdf" 
                : "Raport_zarzadczy_" + timestamp + ".pdf";

        File file = (selectedDirectory != null) 
                ? new File(selectedDirectory, fileName) 
                : new File(System.getProperty("user.home"), "Documents/" + fileName);

        // Użycie buforowanej czcionki
        PdfFont font = cachedFont;
        if (font == null) {
            try (InputStream fontStream = ExecutiveOverviewReportGenerator.class.getResourceAsStream("/fonts/DejaVuSans.ttf")) {
                if (fontStream != null) {
                    FontProgram fontProgram = FontProgramFactory.createFont(fontStream.readAllBytes());
                    font = PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    cachedFont = font;
                }
            }
        }

        // Budowanie zapytania SQL
        StringBuilder queryBuilder = new StringBuilder(
            "SELECT v.*, p.manager_id FROM vw_ExecutiveOverview v JOIN Projects p ON v.project_id = p.id WHERE v.project_id = ?"
        );

        // Add dynamic WHERE conditions
        if (projectStatus != null && !projectStatus.isEmpty()) {
            queryBuilder.append(" AND v.project_status = ?");
        } else {
            queryBuilder.append(" AND (? IS NULL OR v.project_status = ?)");
        }

        if (managerId != null) {
            queryBuilder.append(" AND p.manager_id = ?");
        } else {
            queryBuilder.append(" AND (? IS NULL OR p.manager_id = ?)");
        }

        if (showOverdueTasks) {
            queryBuilder.append(" AND v.overdue_tasks > 0");
        } else {
            queryBuilder.append(" AND (v.overdue_tasks > 0 OR ? = FALSE)");
        }

        if (showOverdueMilestones) {
            queryBuilder.append(" AND v.overdue_milestones > 0");
        } else {
            queryBuilder.append(" AND (v.overdue_milestones > 0 OR ? = FALSE)");
        }

        if (minCompletionRate != null || maxCompletionRate != null) {
            if (minCompletionRate != null) {
                queryBuilder.append(" AND v.task_completion_rate >= ?");
            }
            if (maxCompletionRate != null) {
                queryBuilder.append(" AND v.task_completion_rate <= ?");
            }
        } else {
            queryBuilder.append(" AND v.task_completion_rate BETWEEN ? AND ?");
        }


        ProjectData projectData = new ProjectData();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {

            int paramIndex = 1;


            stmt.setInt(paramIndex++, projectId);


            if (projectStatus != null && !projectStatus.isEmpty()) {
                stmt.setString(paramIndex++, projectStatus);
            } else {
                stmt.setNull(paramIndex++, Types.VARCHAR);
                stmt.setString(paramIndex++, "");  // Dummy value, won't be used
            }


            if (managerId != null) {
                stmt.setInt(paramIndex++, managerId);
            } else {
                stmt.setNull(paramIndex++, Types.INTEGER);
                stmt.setInt(paramIndex++, 0);  // Dummy value, won't be used
            }


            if (!showOverdueTasks) {
                stmt.setBoolean(paramIndex++, showOverdueTasks);
            }


            if (!showOverdueMilestones) {
                stmt.setBoolean(paramIndex++, showOverdueMilestones);
            }


            if (minCompletionRate != null || maxCompletionRate != null) {
                if (minCompletionRate != null) {
                    stmt.setDouble(paramIndex++, minCompletionRate);
                }
                if (maxCompletionRate != null) {
                    stmt.setDouble(paramIndex++, maxCompletionRate);
                }
            } else {
                stmt.setDouble(paramIndex++, 0);
                stmt.setDouble(paramIndex++, 100);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Map ResultSet to ProjectData DTO
                    projectData.project = rs.getString("project");
                    projectData.projectStatus = rs.getString("project_status");
                    projectData.projectProgress = rs.getString("project_progress");
                    projectData.projectManager = rs.getString("project_manager");
                    projectData.teamsInvolved = rs.getString("teams_involved");
                    projectData.employeesAssigned = rs.getString("employees_assigned");
                    projectData.milestones = rs.getString("milestones");
                    projectData.totalTasks = rs.getString("total_tasks");
                    projectData.tasksDone = rs.getString("tasks_done");
                    projectData.tasksCanceled = rs.getString("tasks_canceled");

                    // Handle NULL values for task_completion_rate and avg_milestone_progress
                    String taskCompletionRate = rs.getString("task_completion_rate");
                    projectData.taskCompletionRate = (taskCompletionRate != null) ? taskCompletionRate : "0";

                    String avgMilestoneProgress = rs.getString("avg_milestone_progress");
                    projectData.avgMilestoneProgress = (avgMilestoneProgress != null) ? avgMilestoneProgress : "0";

                    projectData.overdueMilestones = rs.getString("overdue_milestones");
                    projectData.overdueTasks = rs.getString("overdue_tasks");
                    projectData.involvedTeams = rs.getString("involved_teams");
                    projectData.teamLeaders = rs.getString("team_leaders");
                    projectData.taskTitles = rs.getString("task_titles");
                }
            }
        }

        // sprawdzenie czy nie puste
        if (projectData.isEmpty()) {
            try (PdfWriter writer = new PdfWriter(file);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                if (font != null) {
                    document.setFont(font);
                }


                Div messageDiv = new Div();
                messageDiv.setKeepTogether(true);
                messageDiv.add(new Paragraph("Brak danych dla wybranego projektu."));
                document.add(messageDiv);
            }
            System.out.println("Raport zapisany jako: " + file.getAbsolutePath());
            return;
        }

        // Generowanie pdf
        try (PdfWriter writer = new PdfWriter(file);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            if (font != null) {
                document.setFont(font);
            }

            // Dodanie tytułu i daty
            document.add(new Paragraph("RAPORT ZARZĄDCZY PROJEKTU")
                    .setFontSize(20).setBold()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

            document.add(new Paragraph("Wygenerowano: " + timestamp)
                    .setFontSize(10).setItalic()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            // Tabela z danymi projektu
            Div reportDiv = new Div();
            reportDiv.setKeepTogether(true);

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                    .useAllAvailableWidth().setMarginBottom(20);

            // Definowanie tabeli
            String[][] rows = {
                    {"Projekt", projectData.getProject()},
                    {"Status", projectData.getProjectStatus()},
                    {"Postęp projektu", projectData.getProjectProgress() + "%"},
                    {"Menedżer projektu", projectData.getProjectManager()},
                    {"Liczba zespołów", projectData.getTeamsInvolved()},
                    {"Liczba pracowników", projectData.getEmployeesAssigned()},
                    {"Liczba kamieni milowych", projectData.getMilestones()},
                    {"Liczba zadań", projectData.getTotalTasks()},
                    {"Zadania zakończone", projectData.getTasksDone()},
                    {"Zadania anulowane", projectData.getTasksCanceled()},
                    {"% ukończonych zadań", projectData.getTaskCompletionRate() + "%"},
                    {"Średni postęp kamieni", projectData.getAvgMilestoneProgress() + "%"},
                    {"Opóźnione kamienie milowe", projectData.getOverdueMilestones()},
                    {"Opóźnione zadania", projectData.getOverdueTasks()},
                    {"Zespoły", projectData.getInvolvedTeams()},
                    {"Liderzy zespołów", projectData.getTeamLeaders()}
            };

            for (int i = 0; i < rows.length; i++) {
                Cell key = new Cell().add(new Paragraph(rows[i][0])).setBold();
                Cell value = new Cell().add(new Paragraph(rows[i][1]));
                if (i % 2 == 0) {
                    key.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                    value.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                }
                infoTable.addCell(key);
                infoTable.addCell(value);
            }

            reportDiv.add(infoTable);

            // Sekcja z zadaniami
            reportDiv.add(new Paragraph("Zadania w projekcie:")
                    .setFontSize(12).setBold().setMarginBottom(4));
            reportDiv.add(new Paragraph(projectData.getTaskTitles()));


            document.add(reportDiv);
        }

        System.out.println("Raport zapisany jako: " + file.getAbsolutePath());
    }
}
