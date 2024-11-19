package com.student_developer.track_my_grade;

public class Subject2 {
    private String code;
    private int credit;
    private String grade;
    private int gradePoint;

    public Subject2(String code, int credit, String grade) {
        this.code = code;
        this.credit = credit;
        this.grade = grade;
        this.gradePoint = getGradePoint(grade);
    }

    public String getCode() {
        return code;
    }

    public int getCredit() {
        return credit;
    }

    public String getGrade() {
        return grade;
    }

    public int getGradePoint() {
        return gradePoint;
    }

    private int getGradePoint(String grade) {
        switch (grade) {
            case "A+": return 10;
            case "A": return 9;
            case "B+": return 8;
            case "B": return 7;
            case "C": return 6;
            case "U": return 0;
            default: return 0;
        }
    }

    public int getGPAContribution() {
        return gradePoint * credit;
    }
}
