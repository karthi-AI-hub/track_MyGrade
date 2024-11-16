package com.student_developer.track_my_grade;

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

    public String getName() {
        return subjectName;
    }

    public void setName(String name) {
        this.subjectName = name;
    }

    public String getCredits() {
        return cr;
    }

    public void setCredits(String credits) {
        this.cr = credits;
    }

    public String getGrade() {
        return gp;
    }

    public void setGrade(String grade) {
        this.gp = grade;
    }
}
