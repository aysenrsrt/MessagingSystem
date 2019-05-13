package com.example.aysenur.messagingsystem.Adapter;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.aysenur.messagingsystem.AddNote;
import com.example.aysenur.messagingsystem.Model.Schedule;
import com.example.aysenur.messagingsystem.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    List<Schedule> scheduleList;
    Context context;

    public ScheduleAdapter(List<Schedule> scheduleList, Context context) {
        this.scheduleList = scheduleList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.row_todo, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        viewHolder.txtDate.setText(scheduleList.get(i).getDate());
        viewHolder.txtTime.setText(scheduleList.get(i).getTime());
        viewHolder.txtNote.setText(scheduleList.get(i).getNote());

        viewHolder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(scheduleList.get(i));
            }
        });

        viewHolder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to delete?")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                delete(scheduleList.get(i).getId());
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView txtDate, txtNote, txtTime;
        public ImageView imgEdit, imgDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDate = itemView.findViewById(R.id.txtDate);
            txtNote = itemView.findViewById(R.id.txtNote);
            txtTime = itemView.findViewById(R.id.txtTime);

            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }

        @Override
        public void onClick(View v) {
        }
    }

    @Override
    public int getItemCount() { return scheduleList.size(); }

    private Schedule updateSchedule;
    private void update(Schedule schedule) {

        updateSchedule = schedule;
        final android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View dialogView = inflater.inflate(R.layout.activity_add_note, null );
        dialogBuilder.setView(dialogView);

        final EditText edtAddNote = dialogView.findViewById(R.id.edtAddNote);
        final TextView txtAddDates = dialogView.findViewById(R.id.txtAddDates);
        final TextView txtAddTime = dialogView.findViewById(R.id.txtAddTime);
        Button btnAddNote = dialogView.findViewById(R.id.btnAddNote);
        ImageView imgCalendar = dialogView.findViewById(R.id.imgCalendar);
        ImageView imgTime = dialogView.findViewById(R.id.imgTime);

        edtAddNote.setText(updateSchedule.getNote());
        txtAddDates.setText(updateSchedule.getDate());
        txtAddTime.setText(updateSchedule.getTime());

        imgCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int Year, int Month, int DayOfMonth) {
                        txtAddDates.setText(DayOfMonth + "/" + (Month+1)+ "/" + Year);
                        view.setMinDate(System.currentTimeMillis() - 1000);
                    }
                }, year, month, day);
                dpd.getDatePicker().setMinDate(calendar.getTimeInMillis());
                dpd.show();
            }
        });

        imgTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar time = Calendar.getInstance();
                int hour = time.get(Calendar.HOUR_OF_DAY);
                int minute = time.get(Calendar.MINUTE);

                TimePickerDialog timePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int Minute) {
                        txtAddTime.setText(hourOfDay + ":" + Minute);
                    }
                }, hour, minute, true);
                timePicker.show();
            }
        });

        final android.app.AlertDialog b = dialogBuilder.create();
        b.show();

        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtAddNote.getText().length() > 0 ){
                    DatabaseReference dR = FirebaseDatabase.getInstance().getReference("schedule").child(updateSchedule.getId());
                    Schedule s = new Schedule(updateSchedule.getId(), txtAddDates.getText().toString(),
                            txtAddTime.getText().toString(), edtAddNote.getText().toString());
                    dR.setValue(s);
                    b.dismiss();
                    Toast.makeText(context, "Updated.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void delete(String ID) {

        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("schedule").child(ID);
        dR.removeValue();
        Toast.makeText(context, "Deleted.", Toast.LENGTH_SHORT).show();

    }

}
