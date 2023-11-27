import com.opencsv.exceptions.CsvValidationException;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class CsvConsoleApp {
    private String filename;
    private String dbFilename;
    private String apiToken;
    private boolean isAppRunning;
    private final Scanner sc;
    private CsvParser parser;
    private SqliteConnection db;
    private UlearnApiParser api;

    private String[] commands = new String[] {"HELP", "EXIT", "SUMMARY"};

    public CsvConsoleApp(Scanner sc, String apiToken, String dbFilename) {
        this.sc = sc;
        this.apiToken = apiToken;
        this.dbFilename = dbFilename;
    }



    public void Run() throws URISyntaxException, IOException, ParseException, InterruptedException, CsvValidationException, SQLException {
        isAppRunning = true;
        System.out.println("Введите имя файла .csv");
        //filename = sc.nextLine();
        filename = "basicprogramming.csv";
        onStartUp();
        System.out.println("Введите одну из команд");
        System.out.println(String.join(", ", commands));
        System.out.println();
        while (isAppRunning) {
            var command = sc.nextLine();
            switch (command.toUpperCase()) {
                case "EXIT":
                    isAppRunning = false;
                    break;

                case "HELP":
                    System.out.println(String.join(", ", commands));
                    break;

                case "SUMMARY":
                    System.out.println(summary());
                    break;
            }
            System.out.println();
        }
    }

    private String summary() throws SQLException, ParseException, InterruptedException {
        var studentsCount = db.getStudents().size();
        var themesCount = db.getThemes().size();
        var practicesCount = db.getPractices().size();
        var exercisesCount = db.getExercises().size();
        return String.format(
                "Студентов: %d\n" +
                "Тем: %d\n" +
                "Практик: %d\n" +
                "Упражнений: %d\n",
                studentsCount,
                themesCount,
                practicesCount,
                exercisesCount);
    }

    private void load() throws URISyntaxException, IOException, ParseException, InterruptedException {
        api = new UlearnApiParser(apiToken);
        parser = new CsvParser(filename);
        db = new SqliteConnection(dbFilename);
        db.connect();
    }

    private void onStartUp()
            throws CsvValidationException, IOException, SQLException, URISyntaxException, ParseException, InterruptedException {
        System.out.println("Создание обработчиков...");
        load();
        System.out.println("Обработчики созданы");
        System.out.println();
        System.out.println("Пропустить парсинг и запись? Y/N");
        var answer = sc.nextLine();
        if (answer.equals("Y"))
            return;

        System.out.println("Парсинг...");
        var students = parser.parseStudents();
        var exercises = api.getAllExercises();
        var practices = api.getAllPractices();
        var themes = api.getAllThemes();
        var exercisesScores = parser.parseUsersExercisesScores(exercises);
        var practicesScores = parser.parseUsersPracticesScores(practices);
        System.out.println("Парсинг успешен");
        System.out.println();

        System.out.println("Запись в базу данных...");
        db.createTablesIfNotExists(exercises, practices);
        db.addStudentsToDataBase(students);
        db.addPractices(practices);
        db.addExercises(exercises);
        db.addThemes(themes);
        db.addExercisesScores(exercisesScores);
        db.addPracticesScores(practicesScores);
        System.out.println("База данных сформирована успешно");
        System.out.println();
    }
}
