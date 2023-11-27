import classes.Course;
import classes.Exercise;
import classes.Practice;
import classes.Theme;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;

public class UlearnApiParser {
    private final String token;
    private JSONObject json;
    private Date lastJsonUpdate = new Date(1);
    private final long timeToUpdate = 5 * 60 * 1000;
    private final URI apiUrI = new URI("https://api.ulearn.me/courses/basicprogramming");
    public UlearnApiParser(String authToken) throws URISyntaxException, IOException, ParseException, InterruptedException {
        token = authToken;
        json = getResponseJSON();
    }

    public Course getCourse() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var title = (String) getResponseJSON().get("title");
        var id = (String) getResponseJSON().get("id");
        var themes = getAllThemes();
        return new Course(themes, title, id);
    }

    public ArrayList<Practice> getAllPractices() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var practicesSlides = getAllPracticesSlides();
        var practices = new ArrayList<Practice>();
        for (var slideObj: practicesSlides) {
            var slide = (JSONObject) slideObj;
            practices.add(new Practice((String) slide.get("title"), (String) slide.get("id")));
        }
        return practices;
    }

    public ArrayList<Exercise> getAllExercises() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var exercisesSlides = getAllExerciseSlides();
        var exercises = new ArrayList<Exercise>();
        for (var slideObj: exercisesSlides) {
            var slide = (JSONObject) slideObj;
            exercises.add(new Exercise((String) slide.get("title"), (String) slide.get("id")));
        }
        return exercises;
    }

    public ArrayList<Theme> getAllThemes() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var units = getUnits();
        var themes = new ArrayList<Theme>();
        for (var unit: units) {
            themes.add(unitToTheme((JSONObject) unit));
        }
        return themes;
    }

    private ArrayList<Practice> getUnitPractices(JSONObject unit) {
        var practices = new ArrayList<Practice>();
        for (var slideObj: getUnitSlides(unit)) {
            var slide = (JSONObject) slideObj;
            if (isSlideIsPractice(slide)) {
                practices.add(slideToPractice(slide));
            }
        }
        return practices;
    }

    private ArrayList<Exercise> getUnitExercises(JSONObject unit) {
        var exercises = new ArrayList<Exercise>();
        for (var slideObj: getUnitSlides(unit)) {
            var slide = (JSONObject) slideObj;
            if (isSlideIsExercise(slide)) {
                exercises.add(slideToExercise(slide));
            }
        }
        return exercises;
    }

    private Practice slideToPractice(JSONObject slide) {
        return new Practice((String) slide.get("title"), (String) slide.get("id"));
    }

    private Exercise slideToExercise(JSONObject slide) {
        return new Exercise((String) slide.get("title"), (String) slide.get("id"));
    }

    private Theme unitToTheme(JSONObject unit) {
        var title = (String) unit.get("title");
        var id = (String) unit.get("id");
        var practices = getUnitPractices(unit);
        var exercises = getUnitExercises(unit);
        return new Theme(title, id, practices, exercises);
    }

    private JSONArray getUnitSlides(JSONObject unit) {
        return (JSONArray) unit.get("slides");
    }

    private JSONArray getUnits() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var json = getResponseJSON();
        return (JSONArray)json.get("units");
    }

    private JSONArray getAllSlides() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var units = getUnits();
        var result = new JSONArray();
        for (var unitObj: units) {
            var unit = (JSONObject) unitObj;
            var unitSlides = (JSONArray) getUnitSlides(unit);
            for (var slideObj: unitSlides) {
                var slide = (JSONObject) slideObj;
                result.add(slide);
            }
        }
        return result;
    }

    private JSONArray getAllExerciseSlides() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var allSlides = getAllSlides();
        var result = new JSONArray();
        for (var slideObj: allSlides) {
            var slide = (JSONObject) slideObj;
            if (isSlideIsExercise(slide))
                result.add(slide);
        }
        return result;
    }

    private JSONArray getAllPracticesSlides() throws URISyntaxException, IOException, ParseException, InterruptedException {
        var allSlides = getAllSlides();
        var result = new JSONArray();
        for (var slideObj: allSlides) {
            var slide = (JSONObject) slideObj;
            if (isSlideIsPractice(slide))
                result.add(slide);
        }
        return result;
    }

    private boolean isSlideIsExercise(JSONObject slide) {
        return ((String)slide.get("scoringGroup")).equals("exercise") ;
    }

    private boolean isSlideIsPractice(JSONObject slide) {
        return ((String)slide.get("scoringGroup")).equals("homework");
    }

    private JSONObject getResponseJSON() throws URISyntaxException, IOException, InterruptedException, ParseException {
        //return loadJson();
        if ((new Date()).getTime() - lastJsonUpdate.getTime() < timeToUpdate) {
            return json;
        }
        lastJsonUpdate = new Date();
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(apiUrI)
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        json = (JSONObject) new JSONParser().parse(response.body());
        return json;
    }

    private JSONObject loadJson() throws IOException, ParseException {
        var parser = new JSONParser();
        return (JSONObject) parser.parse(new FileReader("jsononline-net.json"));

    }

}
