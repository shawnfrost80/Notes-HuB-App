package com.example.lakshayanoteshub;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheet extends BottomSheetDialogFragment {

    String name;
    String subject;
    String description;
    Context context;

    public BottomSheet(String name, String subject, String description, Context context) {
        this.context = context;
        this.name = name;
        this.subject = subject;
        this.description = description;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet, container, false);
        TextView nameView, subjectView, descriptionView;
        nameView = v.findViewById(R.id.bottom_name);
        subjectView = v.findViewById(R.id.bottom_subject);
        descriptionView = v.findViewById(R.id.bottom_description);
        nameView.setText("Name: " + name);
        subjectView.setText("Subject: " + subject);
        descriptionView.setText("Description: " + description);
        return v;
    }

}
