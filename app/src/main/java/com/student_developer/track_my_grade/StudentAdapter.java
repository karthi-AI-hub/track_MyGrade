package com.student_developer.track_my_grade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class StudentAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> studentNames;

    public StudentAdapter(Context context, List<String> studentNames) {
        super(context, R.layout.list_item_student, studentNames);
        this.context = context;
        this.studentNames = studentNames;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_student, parent, false);
        }

        TextView nameTextView = convertView.findViewById(R.id.studentNameTextView);

        nameTextView.setText(studentNames.get(position));

        return convertView;
    }
}
