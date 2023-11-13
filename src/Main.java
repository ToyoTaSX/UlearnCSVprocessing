import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

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

        var api = new UlearnApiParser(token);
        var db = new SqliteConnection(dbFilename);
        db.connect();
        var parser = new CsvParser(csvFilename);


        var exercises = ArrayUtils.toStringArray(api.getIdExercises().keySet().toArray());
        var practices = ArrayUtils.toStringArray(api.getIdPractices().keySet().toArray());
        db.createTablesIfNotExists(exercises, practices);

        var idThemes = api.getIdThemes();
        db.addIdThemes(idThemes);

        var idPract = api.getIdPractices();
        db.addIdPractices(idPract);

        var idExerc = api.getIdExercises();
        db.addIdExercises(idExerc);

        var pract = api.getThemesPracticesIDs();
        var exer = api.getThemesExercisesIDs();
        db.addThemesPracticesAndExercises(exer, pract);

        var practicesScores = parser.parsePracticesScores();
        db.addPracticesScores(practicesScores);

        var exercisesScores = parser.parseExercisesScores();
        db.addExercisesScores(exercisesScores);

        var students = parser.parseStudents();
        db.addStudentsToDataBase(students);
    }
}