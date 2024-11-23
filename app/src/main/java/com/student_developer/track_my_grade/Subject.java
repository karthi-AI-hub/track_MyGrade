package com.student_developer.track_my_grade;
import com.google.firebase.firestore.PropertyName;

public class Subject {
    private String subjectName;
    private String cr;
    private String gp;


    public Subject() {
    }

    public Subject(String subjectName, String CR, String GP) {
        this.subjectName = subjectName;
        this.cr = CR;
        this.gp = GP;
    }


    @PropertyName("name")
    public String getName() {
        return subjectName;
    }


    @PropertyName("name")
    public void setName(String name) {
        this.subjectName = name;
    }


    @PropertyName("credits")
    public String getCredits() {
        return cr;
    }


    @PropertyName("credits")
    public void setCredits(String credits) {
        this.cr = credits;
    }

    @PropertyName("grade")
    public String getGrade() {
        return gp;
    }

    @PropertyName("grade")
    public void setGrade(String grade) {
        this.gp = grade;
    }
}
