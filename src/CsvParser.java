import classes.*;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CsvParser {
    private String fileName;

    public CsvParser(String fileName) {
        setFileName(fileName);
    }

    public void setFileName(String fileName) {
        var file = new File(fileName);
        if (file.exists()) {
            this.fileName = fileName;
            return;
        }
        throw new IllegalArgumentException();
    }

    public ArrayList<Student> parseStudents() throws IOException, CsvValidationException {
        var students = new ArrayList<Student>();
        var reader = getReader(3);
        var fullNameIndex = getFullnameColumn();
        var ulearnIdIndex = getUidColumn();
        var emailIndex = getEmailColumn();
        var groupIndex = getGroupColumn();

        String[] row;
        while ((row = reader.readNext()) != null) {
            var name = new ArrayList<>(List.of(row[fullNameIndex].split(" +")));
            if (name.size() < 2)
                continue;
            var firstname = name.get(0);
            name.remove(0);
            var lastname = String.join(" ", name);
            var id = row[ulearnIdIndex];
            var email = row[emailIndex];
            var group = row[groupIndex];
            students.add(new Student(firstname, lastname, id, email, group));
        }
        return students;
    }

    public HashMap<String, HashMap<Exercise, Integer>> parseUsersExercisesScores(
            ArrayList<Exercise> exercises
    ) throws CsvValidationException, IOException {
        var colIndexes = getCourseElementsColumns(exercises, "Упр: ");
        return getUidScores(colIndexes);
    }

    public HashMap<String, HashMap<Practice, Integer>> parseUsersPracticesScores(
            ArrayList<Practice> practices
    ) throws CsvValidationException, IOException {
        var colIndexes = getCourseElementsColumns(practices, "ДЗ: ");
        return getUidScores(colIndexes);
    }

    private <T extends CourseElement>
    HashMap<T, Integer> getCourseElementsColumns(
            ArrayList<T> elements,
            String headerPrefix
    ) throws CsvValidationException, IOException {
        var headerRow = getHeaders();
        var indexes = new HashMap<T, Integer>();
        for (var e: elements) {
            indexes.put(e, ArrayUtils.indexOf(headerRow, headerPrefix + e.getTitle()));
        }
        return indexes;
    }

    private <T extends CourseElement>
    HashMap<String, HashMap<T, Integer>> getUidScores(
            HashMap<T, Integer> elemsIndexes
    ) throws IOException, CsvValidationException {
        var reader = getReader(3);
        var uidIndex = getUidColumn();
        var result = new HashMap<String, HashMap<T, Integer>>();
        String[] row;
        while ((row = reader.readNext()) != null) {
            var uid = row[uidIndex];
            var scores = new HashMap<T, Integer>();
            for (var elem: elemsIndexes.keySet()) {
                var column = elemsIndexes.get(elem);
                scores.put(elem, Integer.valueOf(row[column]));
            }
            result.put(uid, scores);
        }
        return result;
    }

    private String[] getHeaders() throws IOException, CsvValidationException {
        String[] row = null;
        var reader = getReader(1);
        return reader.readNext();
    }

    private int getUidColumn() throws CsvValidationException, IOException {
        return ArrayUtils.indexOf(getHeaders(),"Ulearn id");
    }

    private int getFullnameColumn() throws CsvValidationException, IOException {
        return ArrayUtils.indexOf(getHeaders(),"Фамилия Имя");
    }

    private int getGroupColumn() throws CsvValidationException, IOException {
        return ArrayUtils.indexOf(getHeaders(),"Группа");
    }

    private int getEmailColumn() throws CsvValidationException, IOException {
        return ArrayUtils.indexOf(getHeaders(),"Эл. почта");
    }

    private CSVReader getReader() throws IOException {
        var parser = new CSVParserBuilder().withSeparator(';').build();
        var fileReader = new FileReader(fileName, Charset.forName("windows-1251"));
        return new CSVReaderBuilder(fileReader).withCSVParser(parser).build();
    }

    private CSVReader getReader(int skip) throws IOException {
        var parser = new CSVParserBuilder().withSeparator(';').build();
        var fileReader = new FileReader(fileName, Charset.forName("windows-1251"));
        return new CSVReaderBuilder(fileReader).withCSVParser(parser).withSkipLines(skip).build();
    }
}
