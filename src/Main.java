import classes.Exercise;
import classes.Student;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;
import java.sql.SQLException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args)
            throws
            CsvValidationException,
            IOException,
            URISyntaxException,
            InterruptedException,
            ParseException, SQLException {

        var builder = new GraphicsBuilder();
        var db = new SqliteConnection("basicprogrammingDB.db");
        db.connect();
        //updateDatabase(db);

        var students = db.getStudents();
        var uidExercisesScores = db.getUidExercisesScores();
        var uidPracticesScores = db.getUidPracticesScores();
        var studentsScores = new HashMap<Student, Integer>();
        for (var student: students) {
            var total = 0;
            total += uidExercisesScores.get(student.ulearnID).values().stream().mapToInt(Integer::intValue).sum();
            total += uidPracticesScores.get(student.ulearnID).values().stream().mapToInt(Integer::intValue).sum();
            studentsScores.put(student, total);
        }

        var emailsCounts = students.stream()
                .collect(Collectors.groupingBy(s -> s.email.split("@")[1], Collectors.counting()))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() >= 2)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        builder.showChart(builder.getPieChart(new HashMap<String, Long>(emailsCounts), "Доля учеников с email..."));

        var groupsCounts = students.stream()
                .filter(s -> s.group.split("АТ-").length == 2)
                .collect(Collectors.groupingBy(s -> "АТ-" + s.group.split("АТ-")[1], Collectors.counting()))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() >= 2)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        builder.showChart(builder.getPieChart(new HashMap<String, Long>(groupsCounts), "Доля учеников в группе"));

        var groupsAverages = new HashMap<String, Long>();
        for (var groupCount: groupsCounts.entrySet()) {
            var group = groupCount.getKey();
            var studentsCount = groupCount.getValue();
            var totalScore = studentsScores.entrySet().stream()
                    .filter(e -> e.getKey().group.contains(group))
                    .mapToInt(Map.Entry::getValue)
                    .sum();
            groupsAverages.put(group, totalScore / studentsCount);
        }
        builder.showChart(builder.getBarChart(groupsAverages, "Средний балл по группе", "Группа", "Среднее значение"));

        var emailsAverages = new HashMap<String, Long>();
        for (var emailCount: emailsCounts.entrySet()) {
            var email = emailCount.getKey();
            var studentsCount = emailCount.getValue();
            var totalScore = studentsScores.entrySet().stream()
                    .filter(e -> e.getKey().email.contains(email))
                    .mapToInt(Map.Entry::getValue)
                    .sum();
            emailsAverages.put(email, totalScore / studentsCount);
        }
        builder.showChart(builder.getBarChart(emailsAverages, "Средний балл по email", "Email", "Среднее значение"));

        var practicesScores = db.getPracticesScores();
        var practicesAverages = new HashMap<String, Long>();
        for (var practiceEntry: practicesScores.entrySet()) {
            var practice = practiceEntry.getKey();
            var scores = practiceEntry.getValue();
            var max = scores.stream().max(Comparator.comparingInt(s -> s)).get();
            var avg = scores.stream().mapToDouble(s -> s / max.doubleValue()).average().getAsDouble();
            practicesAverages.put(practice.getTitle().substring(9), Math.round(avg * 100));
        }
        builder.showChart(builder.getBarChart(practicesAverages, "Средний относительный балл по практикам", "Практика", "Среднее значение %"));

        var themes = db.getThemes();
        var exercisesScores = db.getExercisesScores();
        var themesAverages = new HashMap<String, Long>();
        for (var theme: themes) {
            var totalPracticesScores = theme.getPractices()
                    .stream()
                    .mapToLong(p -> practicesScores.get(p)
                            .stream()
                            .mapToInt(Integer::intValue)
                            .sum())
                    .sum();
            var totalExercisesScores = theme.getExercises()
                    .stream()
                    .mapToLong(e -> exercisesScores.get(e)
                            .stream()
                            .mapToInt(Integer::intValue)
                            .sum())
                    .sum();

            var totalScore = totalExercisesScores + totalPracticesScores;
            var averageScore = totalScore / uidExercisesScores.size();

            var maxPracticesScore = theme.getPractices()
                    .stream()
                    .mapToLong(p -> practicesScores.get(p)
                            .stream()
                            .mapToInt(Integer::intValue)
                            .max()
                            .getAsInt())
                    .sum();
            var maxExercisesScore = theme.getExercises()
                    .stream()
                    .mapToLong(e -> exercisesScores.get(e)
                            .stream()
                            .mapToInt(Integer::intValue)
                            .max()
                            .getAsInt())
                    .sum();

            var maxScore = maxExercisesScore + maxPracticesScore;
            var relScore = averageScore / Double.valueOf(maxScore);
            themesAverages.put(theme.getTitle(), Math.round(relScore * 100));
        }
        builder.showChart(builder.getBarChart(themesAverages, "Средний относительный балл по темам", "Тема", "Среднее значение %"));
    }

    public static void updateDatabase(SqliteConnection db) throws IOException, URISyntaxException, ParseException, InterruptedException, SQLException, CsvValidationException {
        var br = new BufferedReader(new FileReader("token.txt"));
        var token = br.readLine();
        var csvParser = new CsvParser("basicprogramming.csv");
        var apiParser = new UlearnApiParser(token);

        var practices = apiParser.getAllPractices();
        var exercises = apiParser.getAllExercises();
        db.createTablesIfNotExists(exercises, practices);
        db.addExercises(exercises);
        db.addPractices(practices);

        var themes = apiParser.getAllThemes();
        db.addThemes(themes);

        var practicesScores = csvParser.parseUsersPracticesScores(practices);
        db.addPracticesScores(practicesScores);

        var exercisesScores = csvParser.parseUsersExercisesScores(exercises);
        db.addExercisesScores(exercisesScores);

        var students = csvParser.parseStudents();
        db.addStudentsToDataBase(students);
    }
}

