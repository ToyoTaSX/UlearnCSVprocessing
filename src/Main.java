import com.opencsv.exceptions.CsvValidationException;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args)
            throws
            CsvValidationException,
            IOException,
            URISyntaxException,
            InterruptedException,
            ParseException
    {
        var br = new BufferedReader(new FileReader("token.txt"));
        var token = br.readLine();
        var api = new UlearnApiParser(token);
    }
}