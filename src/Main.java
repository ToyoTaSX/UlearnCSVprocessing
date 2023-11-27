import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args)
            throws
            CsvValidationException,
            IOException,
            URISyntaxException,
            InterruptedException,
            ParseException, SQLException {
        var br = new BufferedReader(new FileReader("token.txt"));
        var token = br.readLine();
        var dbFilename = "basicprogrammingDB.db";
        var csvFilename = "basicprogramming.csv";

        var app = new CsvConsoleApp(new Scanner(System.in), token, dbFilename);
        app.Run();

        var api = new UlearnApiParser(token);
        var parser = new CsvParser(csvFilename);
        var db = new SqliteConnection(dbFilename);
        db.connect();

        var students = parser.parseStudents();
        var exercises = api.getAllExercises();
        var practices = api.getAllPractices();
        var themes = api.getAllThemes();
        var exercisesScores = parser.parseUsersExercisesScores(exercises);
        var practicesScores = parser.parseUsersPracticesScores(practices);

        db.createTablesIfNotExists(exercises, practices);
        db.addStudentsToDataBase(students);
        db.addPractices(practices);
        db.addExercises(exercises);
        db.addThemes(themes);
        db.addExercisesScores(exercisesScores);
        db.addPracticesScores(practicesScores);

        var studentsDB = db.getStudents();
        var exercisesDB = db.getExercises();
        var practicesDB = db.getPractices();
        var themesDB = db.getThemes();
        var exercisesScoresDB = db.getUidExercisesScores();
        var practicesScoresDB = db.getUidPracticesScores();
    }
}

