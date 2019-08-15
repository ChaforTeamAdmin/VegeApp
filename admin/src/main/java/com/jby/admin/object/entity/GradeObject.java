package com.jby.admin.object.entity;

public class GradeObject {
    private String poId, gradeId, grade;

    public GradeObject(String poId, String gradeId, String grade) {
        this.poId = poId;
        this.gradeId = gradeId;
        this.grade = grade;
    }

    public GradeObject(String gradeId, String grade) {
        this.gradeId = gradeId;
        this.grade = grade;
    }

    public String getPoId() {
        return poId;
    }

    public String getGradeId() {
        return gradeId;
    }

    public String getGrade() {
        return grade;
    }

    @Override
    public String toString() {
        return grade;
    }
}
