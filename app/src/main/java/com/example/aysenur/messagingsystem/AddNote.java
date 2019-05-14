package com.example.aysenur.messagingsystem;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.aysenur.messagingsystem.Model.Schedule;
import com.example.aysenur.messagingsystem.reminder.AlarmSchedule;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.util.Calendar;

public class AddNote extends AppCompatActivity {

    EditText edtAddNote;
    TextView txtAddDates, txtAddTime;
    ImageView btnAddNote;

    Schedule schedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        edtAddNote = findViewById(R.id.edtAddNote);
        txtAddDates = findViewById(R.id.txtAddDates);
        txtAddTime = findViewById(R.id.txtAddTime);
        btnAddNote = findViewById(R.id.btnAddNote);

        final Intent i = getIntent();
        final Schedule s = (Schedule) i.getSerializableExtra("Schedule");

        if(s != null) {
            try {
                updateSchedule(s);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        txtAddDates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(AddNote.this, new DatePickerDialog.OnDateSetListener() {
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

        txtAddTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar time = Calendar.getInstance();
                int hour = time.get(Calendar.HOUR_OF_DAY);
                int minute = time.get(Calendar.MINUTE);

                TimePickerDialog timePicker = new TimePickerDialog(AddNote.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int Minute) {
                        txtAddTime.setText(hourOfDay + ":" + Minute);
                    }
                }, hour, minute, true);
                timePicker.show();
            }
        });

        if(s == null){
            btnAddNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(edtAddNote.getText().length() > 0){
                        try {
                            addSchedule();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(AddNote.this, MainActivity.class);
                        startActivity(intent);
                    }
                    else {
                        new AlertDialog.Builder(AddNote.this)
                                .setMessage("Please, Do not leave fields empty.")

                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                    }
                }
            });
        }

    }

    private void addSchedule() throws ParseException {

        String id = null;
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        DatabaseReference mFirebaseDatabase = mFirebaseInstance.getReference("schedule");

        if (TextUtils.isEmpty(id)) {
            id = mFirebaseDatabase.push().getKey();
        }

        schedule  = new Schedule(id, txtAddDates.getText().toString(),
                txtAddTime.getText().toString(), edtAddNote.getText().toString());

        mFirebaseDatabase.child(id).setValue(schedule);

        Intent notificationIntent = new Intent(this, AlarmReceiver.class);

        AlarmSchedule a = new AlarmSchedule();

        int mins = a.diffDate(txtAddDates.getText().toString(), txtAddTime.getText().toString());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, mins);
        a.setAlarm(AddNote.this, notificationIntent, cal);
    }

    private void updateSchedule(final Schedule updateSchedule) throws ParseException {

        edtAddNote.setText(updateSchedule.getNote());
        txtAddDates.setText(updateSchedule.getDate());
        txtAddTime.setText(updateSchedule.getTime());

        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtAddNote.getText().length() > 0 ){
                    DatabaseReference dR = FirebaseDatabase.getInstance().getReference("schedule").child(updateSchedule.getId());
                    Schedule s = new Schedule(updateSchedule.getId(), txtAddDates.getText().toString(),
                            txtAddTime.getText().toString(), edtAddNote.getText().toString());
                    dR.setValue(s);
                    Intent intent = new Intent(AddNote.this, MainActivity.class);
                    startActivity(intent);

                    Intent notificationIntent = new Intent(AddNote.this, AlarmReceiver.class);

                    AlarmSchedule a = new AlarmSchedule();

                    int mins = 0;
                    try {
                        mins = a.diffDate(txtAddDates.getText().toString(), txtAddTime.getText().toString());
                        if(mins < 0) {
                            Toast.makeText(AddNote.this, "Please, Choose a Future Date .", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(mins >= 0){
                        Toast.makeText(AddNote.this, "Updated.", Toast.LENGTH_SHORT).show();
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MINUTE, mins);
                        a.setAlarm(AddNote.this, notificationIntent, cal);
                    }
                }
            }
        });

    }

    private void delete(String ID) {

        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("schedule").child(ID);
        dR.removeValue();
        Toast.makeText(AddNote.this, "Deleted.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AddNote.this, MainActivity.class);
        startActivity(intent);
    }

}
