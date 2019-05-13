package com.example.aysenur.messagingsystem;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.aysenur.messagingsystem.Model.Schedule;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddNote extends AppCompatActivity {

    EditText edtAddNote;
    TextView txtAddDates, txtAddTime;
    Button btnAddNote;
    ImageView imgCalendar, imgTime;

    Schedule schedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        edtAddNote = findViewById(R.id.edtAddNote);
        txtAddDates = findViewById(R.id.txtAddDates);
        txtAddTime = findViewById(R.id.txtAddTime);
        btnAddNote = findViewById(R.id.btnAddNote);
        imgCalendar = findViewById(R.id.imgCalendar);
        imgTime = findViewById(R.id.imgTime);

        imgCalendar.setOnClickListener(new View.OnClickListener() {
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

        imgTime.setOnClickListener(new View.OnClickListener() {
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
        int mins = diffDate(txtAddDates.getText().toString(), txtAddTime.getText().toString());

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent(this, AlarmReceiver.class);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, mins);

        setAlarm(this, notificationIntent, schedule, cal);
    }

    public static void setAlarm(Context context, Intent intent, Schedule schedule, Calendar calendar) {
        intent.putExtra("SCHEDULE_ID", schedule);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private int diffDate (String addedDate, String addedTime ) throws ParseException {

        String total = addedDate + " " + addedTime;

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date time = formatter.parse(total);

        Date currentTime = Calendar.getInstance().getTime();

        long diff = time.getTime() - currentTime.getTime();
        int Hours = (int) (diff/(1000 * 60 * 60) * 60);
        int Mins = (int) (diff/(1000*60)) % 60;
        int totalTime = Hours + Mins;
        Log.i("diff: ", ""+ totalTime + ", " + time + ", c: " + currentTime);

        return totalTime;
    }
}
