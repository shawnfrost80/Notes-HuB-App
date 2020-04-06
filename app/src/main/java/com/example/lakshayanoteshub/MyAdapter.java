package com.example.lakshayanoteshub;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    List<Note> noteList;
    Context context;
    String key;
    OnClick onClick;

    public MyAdapter(Context context, List<Note> noteList, String key, OnClick onClick) {
        this.context = context;
        this.noteList = noteList;
        this.key = key;
        this.onClick = onClick;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView textView, sizeView;
        ImageView imageView;
        private Context context;
        OnClick onClick;

        public MyViewHolder(@NonNull View itemView, OnClick onClick) {
            super(itemView);
            context = itemView.getContext();
            textView = itemView.findViewById(R.id.name);
            sizeView = itemView.findViewById(R.id.size);
            imageView = itemView.findViewById(R.id.offline_icon);
            this.onClick = onClick;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onClick.onClick(getAdapterPosition(), key);
        }

        @Override
        public boolean onLongClick(View v) {
            onClick.onLongClick(getAdapterPosition(), key);
            return true;
        }
    }

    public interface OnClick {
        void onClick(int position, String key);
        void onLongClick(int position, String key);
    }


    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.linear_list_view, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(v, onClick);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, final int position) {
        Note note = noteList.get(position);
        holder.textView.setText(note.name);
        holder.sizeView.setText(note.size);

        File pdfFile = new File(context.getExternalFilesDir(null) + "/LakshayaNotesHuB/" + note.name + note.subjectName + ".pdf");
        if (pdfFile.exists()) {
            holder.imageView.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setVisibility(View.INVISIBLE);
        }

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("Item Clicked", String.valueOf(position));
//            }
//        });
//
//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                Log.i("Long Clicked", String.valueOf(position));
//                return true;
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }
}
