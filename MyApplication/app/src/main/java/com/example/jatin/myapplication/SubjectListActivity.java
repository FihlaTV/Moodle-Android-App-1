package com.example.jatin.myapplication;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class SubjectListActivity extends AppCompatActivity implements
        View.OnClickListener{
    //stores the object for the 'my' page
    Document doc;
    ProgressDialog pd;
    //stores the ID, PASSWORD from the intent
    String user,pass;
    //stors date and time selected by the user
    private Calendar calendar;
    private int hour, min;
    private int year, month, day;

    private TextView date,time;
    Button date_btn,time_btn;
    //stores the subject names in the list
    ArrayList<String> links = new ArrayList<String>();
    //stores the subject names in the list
    ArrayList<String> sub_name = new ArrayList<String>();
    Intent in;
    ListView listView;
    //stores the difference of the current time and the time set by the user
    long diff;
    //SharedPreference object for local DB
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_list);
        getSupportActionBar().setTitle("Subjects");
        //checks if the network is available or not
        if(!isNetworkAvailable()){
            Toast.makeText(SubjectListActivity.this,"Internet Unavailable.",Toast.LENGTH_SHORT).show();
            return;
        }
        //gets the object of local DB
        sp = getSharedPreferences("login_details",  Context.MODE_PRIVATE);
        //gets and store the ID, PASSWORD from the intent
        in = getIntent();
        user = in.getStringExtra("username");
        pass = in.getStringExtra("password");
        //binding date and time views
        date = (TextView) findViewById(R.id.date);
        date_btn = (Button) findViewById(R.id.date_btn);
        time = (TextView) findViewById(R.id.time);
        time_btn = (Button) findViewById(R.id.time_btn);
        //setting onClick listener
        date_btn.setOnClickListener( this);
        time_btn.setOnClickListener( this);
        //gets the current date and time
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        //create the object of class Title and calls execute() method on it
        new SubjectListActivity.Title(user, pass).execute();
    }

    @Override
    public void onClick(View v) {
        //if SET DATE button clicked
        if (v == date_btn) {
            //Launch the DatePickerDialog and sets the selected date in the textbox
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            date.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        }
                    }, year, month, day);
            datePickerDialog.show();
        }
        //if SET TIME button clicked
        if (v == time_btn) {
            // Launch Time Picker Dialog and sets the selected time in the textbox
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            time.setText(hourOfDay + ":" + minute);
                        }
                    }, hour, min, false);
            timePickerDialog.show();
        }
    }
    //class defined to start the progress dialog and souping the moodle in background
    private class Title extends AsyncTask<Void, Void, Void> {
        //sores the list of headings with Subject name and their page links
        Elements e;
        String user;
        String pass;

        //constructor
        public Title(String userName, String password) {
            this.user = userName;
            this.pass = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //starting progress dialog
            pd = new ProgressDialog(SubjectListActivity.this);
            pd.setMessage("Loading your subjects...");
            pd.setIndeterminate(false);
            pd.setCanceledOnTouchOutside(false);
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //POST's the username and password to the moodle
                org.jsoup.Connection.Response res = Jsoup.connect("http://moodle.iitb.ac.in/login/index.php")
                        .data("username", this.user)
                        .data("password", this.pass)
                        .data("submit", "Log in")
                        .data("formlogin", "t")
                        .method(Connection.Method.POST)
                        .execute();
                //stores the cookies
                Map<String, String> cookies = res.cookies();
                //connects to the 'my' page on moodle and get its html
                doc = Jsoup.connect("http://moodle.iitb.ac.in/my")
                        .followRedirects(true)
                        .cookies(cookies)
                        .get();
                //gets the h2 heading having subject name and their link
                e = doc.select("h2[class=title]");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //if nothing is returned from the server
            if(e.isEmpty()) {
                pd.dismiss();
                Toast.makeText(getApplicationContext(), "Not connected to IIT-B network or some Server error occured.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(getApplicationContext(),"Logged in as " + doc.select("span[class=usertext]").text(),Toast.LENGTH_SHORT).show();
            //adds the subject name and their page links into the lists
            for (Element div : e) {
                sub_name.add(div.text());
                links.add(div.children().attr("href").toString());
            }
            //creats an adapter for the list which shows the subject names
            ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.activity_listview,R.id.label, sub_name);
            listView = (ListView) findViewById(R.id.mobile_list);
            listView.setAdapter(adapter);
            //setting onClickListener to the list items
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    //checks if the date is not selected
                    if(date.getText().toString().trim().equals("DATE")){
                        Toast.makeText(getApplicationContext(),"Please select the Date",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //checks if the time is not selected
                    if(time.getText().toString().trim().equals("TIME")){
                        Toast.makeText(getApplicationContext(),"Please select the Time too",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        //format: 27-02-2017 21:32
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                        //gets the date object from the date, tiem choosen by the user
                        Date date1 = dateFormat.parse(date.getText().toString().trim() + " " + time.getText().toString().trim());
                        Date date2 = null;
                        //gets the current date, time object
                        date2 = dateFormat.parse(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(Calendar.getInstance().getTime()));
                        //finds the difference between the two times
                        diff = date2.getTime() - date1.getTime();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                    //intents to the NewsForumActivity with the url, ID, PASSWORD and time difference
                    Intent in = new Intent(SubjectListActivity.this,NewsForumActivity.class);
                    in.putExtra("url",links.get(position));
                    in.putExtra("username",user);
                    in.putExtra("password",pass);
                    in.putExtra("set_time",diff);
                    startActivity(in);
                }
            });
            pd.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //action to perform when menu item is selected
        switch (item.getItemId()) {
            case R.id.action_logout:
                    logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //clears the local DB and intents to login Activty
    private void logout(){
        editor = sp.edit();
        editor.clear();
        editor.commit();
        Intent i = new Intent(SubjectListActivity.this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
    //checks whether the network is available or not
    private boolean isNetworkAvailable() {
        ConnectivityManager cm
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
