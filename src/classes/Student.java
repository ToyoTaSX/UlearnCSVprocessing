package classes;

public class Student {
    public final String firstname;
    public final String lastname;
    public final String ulearnID;
    public final String email;
    public final String group;

    public Student(String firstname, String lastname, String ulearnID, String email, String group) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.ulearnID = ulearnID;
        this.email = email;
        this.group = group;
    }
}
