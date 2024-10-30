package com.student_developer.track_my_grade;

public class Subject {
    private String subjectName;
    private String CR;
    private String GP;

    public Subject() {
    }

    public Subject(String subjectName, String CR, String GP) {
        this.subjectName = subjectName;
        this.CR = CR;
        this.GP = GP;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getCR() {
        return CR;
    }

    public String getGP() {
        return GP;
    }
}
