import classes.Exercise;
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
        //var br = new BufferedReader(new FileReader("token.txt"));
        //var token = br.readLine();
        //var dbFilename = "basicprogrammingDB.db";
        //var app = new CsvConsoleApp(new Scanner(System.in), token, dbFilename);
        //app.Run();
        var builder = new GraphicsBuilder();
        var db = new SqliteConnection("basicprogrammingDB.db");
        db.connect();
//        var exercisesScores = db.getExercisesScores();
//        var data = exercisesScores.values()
//                .stream()
//                .flatMap(lst ->
//                         {
//                             var max = Collections.max(lst).doubleValue();
//                             return lst.stream().map(s -> s / max);
//                         })
//                .toList();
//        data.forEach(System.out::println);
//        System.out.println(data.size());
        var data = new ArrayList<Double>();
        for (var i = 0; i< 10000; i++)
            data.add(new Random().nextDouble() * 1000);
        builder.showHistogram(data, "тест");
    }
}

