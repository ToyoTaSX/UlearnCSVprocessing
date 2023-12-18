package classes;

import java.util.Objects;

public abstract class CourseElement {

    protected String id;
    protected String title;
    public CourseElement() {
        id = "Undefined";
        title = "Undefined";
    }

    public CourseElement(String id) {
        this.id = id;
        this.title = "Undefined";
    }

    public CourseElement(String title, String id) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CourseElement))
            return false;
        return Objects.equals(((CourseElement) obj).getId(), getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }
}
