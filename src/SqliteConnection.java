import classes.*;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class SqliteConnection {

    //region Инфраструктура
    private final String filename;
    private Connection connection;
    private Statement st;

    private boolean isConnect;

    public SqliteConnection(String filename) {
        this.filename = filename;
    }

    public boolean isConnected() {
        return isConnect;
    }

    public boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + filename);
            st = connection.createStatement();
            isConnect = true;
            return true;
        } catch (SQLException e) {
            return false;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    //region Создание таблиц
    public void createTablesIfNotExists(ArrayList<Exercise> exercises, ArrayList<Practice> practices) throws SQLException {
        createTableStudents();
        createTableThemes();
        createTableExercises();
        createTablePractices();
        createTableExercisesScores(exercises);
        createTablePracticesScores(practices);
        createTableThemesPracticesAndExercises();
    }

    private void createTableStudents() throws SQLException {
        var query = "CREATE TABLE IF NOT EXISTS 'Students' ('UlearnID' VARCHAR PRIMARY KEY, 'firstname' TEXT, 'lastname' TEXT, 'email' TEXT, 'studentGroup' TEXT);";
        st.execute(query);
    }

    private void createTableThemes() throws SQLException {
        var query = "CREATE TABLE IF NOT EXISTS 'Themes' ('id' VARCHAR PRIMARY KEY, 'title' TEXT);";
        st.execute(query);
    }

    private void createTableExercises() throws SQLException {
        var query = "CREATE TABLE IF NOT EXISTS 'Exercises' ('id' VARCHAR PRIMARY KEY, 'title' TEXT);";
        st.execute(query);
    }

    private void createTablePractices() throws SQLException {
        var query = "CREATE TABLE IF NOT EXISTS 'Practices' ('id' VARCHAR PRIMARY KEY, 'title' TEXT);";
        st.execute(query);
    }

    private void createTableExercisesScores(ArrayList<Exercise> exercises) throws SQLException {
        var ids = exercises.stream().map(Exercise::getId).collect(Collectors.toList());
        createTableWithSameTypeColumnsAndUID("ExercisesScores", ids, "SMALLINT");
    }

    private void createTablePracticesScores(ArrayList<Practice> practices) throws SQLException {
        var ids = practices.stream().map(Practice::getId).collect(Collectors.toList());
        createTableWithSameTypeColumnsAndUID("PracticesScores", ids, "SMALLINT");
    }

    private void createTableWithSameTypeColumnsAndUID(String tableName, List<String> columnsNames, String type) throws SQLException {
        var builder = new StringBuilder();
        builder.append(String.format("CREATE TABLE IF NOT EXISTS '%s' ('UlearnID' VARCHAR PRIMARY KEY", tableName));
        for (var id: columnsNames) {
            builder.append(String.format(", '%s' %s", id, type));
        }
        builder.append(");");
        st.execute(builder.toString());
    }

    private void createTableThemesPracticesAndExercises() throws SQLException {
        var q = "CREATE TABLE IF NOT EXISTS 'ThemesPracticesAndExercises' ('id' VARCHAR PRIMARY KEY, 'practices' TEXT, 'exercises' TEXT);";
        st.execute(q);
    }

    //endregion

    public void addStudentsToDataBase(ArrayList<Student> students) throws SQLException {
        var count = 0;
        for (var s: students) {
            var query = String.format(
                    "REPLACE INTO Students (UlearnID, firstname, lastname, email, studentGroup) VALUES ('%s', '%s', '%s', '%s', '%s')",
                    s.ulearnID.isEmpty() ? "None" : s.ulearnID,
                    s.firstname.isEmpty() ? "None" : s.firstname,
                    s.lastname.isEmpty() ? "None" : s.lastname,
                    s.email.isEmpty() ? "None" : s.email,
                    s.group.isEmpty() ? "None" : s.group);
            st.addBatch(query);
            count++;
            if (count > 500) {
                st.executeBatch();
                count = 0;
            }
        }
        st.executeBatch();
    }

    public void addThemes(ArrayList<Theme> themes) throws SQLException {
        addIdTitle(themes, "Themes");
        for (var theme: themes) {
            var jsonArrExercises = new JSONArray();
            jsonArrExercises.addAll(theme.getExercises().stream().map(e -> e.getId()).toList());
            var jsonArrPractices = new JSONArray();
            jsonArrPractices.addAll(theme.getPractices().stream().map(p -> p.getId()).toList());
            var q = String.format("REPLACE INTO ThemesPracticesAndExercises (id, exercises, practices) VALUES ('%s', '%s', '%s')",
                    theme.getId(),
                    jsonArrExercises.toJSONString(),
                    jsonArrPractices.toJSONString());
            st.addBatch(q);
        }
        st.executeBatch();
    }

    public void addExercises(ArrayList<Exercise> exercises) throws SQLException {
        addIdTitle(exercises, "Exercises");
    }

    public void addPractices(ArrayList<Practice> practices) throws SQLException {
        addIdTitle(practices, "Practices");
    }

    public void addPracticesScores(HashMap<String, HashMap<Practice, Integer>> allScores) throws SQLException {
        addUidScores(allScores, "PracticesScores");
    }

    public void addExercisesScores(HashMap<String, HashMap<Exercise, Integer>> allScores) throws SQLException {
        addUidScores(allScores, "ExercisesScores");
    }

    public ArrayList<Practice> getPractices() throws SQLException {
        var q = "SELECT id, title FROM Practices;";
        var queryRes = st.executeQuery(q);
        var result = new ArrayList<Practice>();
        while (queryRes.next()){
            var id = queryRes.getString("id");
            var title = queryRes.getString("title");
            result.add(new Practice(title, id));
        }
        return result;
    }

    public ArrayList<Exercise> getExercises() throws SQLException {
        var q = "SELECT id, title FROM Exercises;";
        var queryRes = st.executeQuery(q);
        var result = new ArrayList<Exercise>();
        while (queryRes.next()){
            var id = queryRes.getString("id");
            var title = queryRes.getString("title");
            result.add(new Exercise(title, id));
        }
        return result;
    }

    public ArrayList<Theme> getThemes() throws SQLException, ParseException {
        var titles = getThemesTitles();
        var themes = new ArrayList<Theme>();
        var allExercises = getExercises();
        var allPractices = getPractices();
        var q = "SELECT * FROM ThemesPracticesAndExercises;";
        var queryRes = st.executeQuery(q);
        while (queryRes.next()){
            var id = queryRes.getString("id");
            var title = titles.get(id);

            var parser = new JSONParser();
            var jsonExercises = (JSONArray) parser.parse(queryRes.getString("exercises"));
            var jsonPractices = (JSONArray) parser.parse(queryRes.getString("practices"));
            var practicesIds = new HashSet<String>(jsonPractices);
            var exercisesIds = new HashSet<String>(jsonExercises);
            var practices = new ArrayList<>(allPractices.stream().filter(p -> practicesIds.contains(p.getId())).toList());
            var exercises = new ArrayList<>(allExercises.stream().filter(e -> exercisesIds.contains(e.getId())).toList());

            themes.add(new Theme(title, id, practices, exercises));
        }
        return themes;
    }

    public ArrayList<Student> getStudents() throws SQLException {
        var q = "SELECT * FROM Students;";
        var queryRes = st.executeQuery(q);
        var result = new ArrayList<Student>();
        while (queryRes.next()){
            var firstname = queryRes.getString("firstname");
            var lastname = queryRes.getString("lastname");
            var ulearnID = queryRes.getString("UlearnID");
            var email = queryRes.getString("email");
            var group = queryRes.getString("studentGroup");
            result.add(new Student(firstname, lastname, ulearnID, email, group));
        }
        return result;
    }

    public HashMap<String, HashMap<Exercise, Integer>> getUidExercisesScores() throws SQLException {
        var exercises = getExercises();
        return getUidScores(exercises, "ExercisesScores");
    }

    public HashMap<Exercise, ArrayList<Integer>> getExercisesScores() throws SQLException {
        return getScores(getUidExercisesScores());
    }

    public HashMap<String, HashMap<Practice, Integer>> getUidPracticesScores() throws SQLException {
        var practices = getPractices();
        return getUidScores(practices, "PracticesScores");

    }

    public HashMap<Practice, ArrayList<Integer>> getPracticesScores() throws SQLException {
        return getScores(getUidPracticesScores());
    }

    private <T extends CourseElement>  HashMap<String, HashMap<T, Integer>> getUidScores(ArrayList<T> elems, String tableName)
            throws SQLException {
        var q = String.format("SELECT * FROM %s", tableName);
        var qRes = st.executeQuery(q);
        var res = new HashMap<String, HashMap<T, Integer>>();
        while (qRes.next()){
            var uid = qRes.getString("UlearnID");
            var scores = new HashMap<T, Integer>();
            for (var elem: elems) {
                var score = qRes.getInt(elem.getId());
                scores.put(elem, score);
            }
            res.put(uid, scores);
        }
        return res;
    }

    private <T extends CourseElement> HashMap<T, ArrayList<Integer>> getScores(HashMap<String, HashMap<T, Integer>> uidScores) {
        var result = new HashMap<T, ArrayList<Integer>>();
        for (var map: uidScores.values()) {
            for (var item: map.entrySet()) {
                var key = item.getKey();
                var score = item.getValue();
                if (result.containsKey(key)) {
                    result.get(key).add(score);
                } else {
                    result.put(key, new ArrayList<>(score));
                }
            }
        }
        return result;
    }


    private <T extends CourseElement> void addIdTitle(ArrayList<T> elements, String tableName)
            throws SQLException
    {
        var count = 0;
        for (var element: elements) {
            var query = String.format(
                    "REPLACE INTO %s (id, title) VALUES ('%s', '%s')",
                    tableName,
                    element.getId(),
                    element.getTitle());
            st.addBatch(query);
            count++;
            if (count > 500) {
                st.executeBatch();
                count = 0;
            }
        }
        st.executeBatch();
    }


    private <T extends CourseElement> void addUidScores(
            HashMap<String, HashMap<T, Integer>> uidScores,
            String tableName
    ) throws SQLException {
        var paramsBuilder = new StringBuilder();
        var valuesBuilder = new StringBuilder();
        var count = 0;
        for (var uid: uidScores.keySet()) {
            paramsBuilder.setLength(0);
            valuesBuilder.setLength(0);
            var scores = uidScores.get(uid);
            for (var elem: scores.keySet()) {
                var score = scores.get(elem);
                paramsBuilder.append(String.format("'%s', ", elem.getId()));
                valuesBuilder.append(String.format("'%d', ", score));
            }
            var query = String.format("REPLACE INTO %s ('UlearnID', %s) VALUES ('%s', %s);",
                    tableName,
                    paramsBuilder.substring(0, paramsBuilder.length() - 2),
                    uid,
                    valuesBuilder.substring(0, valuesBuilder.length() - 2));
            if (uid.isEmpty()) {
                System.out.println("1");
            }
            st.addBatch(query);
            count++;

            if (count > 500) {
                st.executeBatch();
                count = 0;
            }
        }
        st.executeBatch();
    }

    private HashMap<String, String> getThemesTitles() throws SQLException {
        var q = "SELECT id, title FROM Themes;";
        var queryRes = st.executeQuery(q);
        var result = new HashMap<String, String>();
        while (queryRes.next()){
            var id = queryRes.getString("id");
            var title = queryRes.getString("title");
            result.put(id, title);
        }
        return result;
    }

}
