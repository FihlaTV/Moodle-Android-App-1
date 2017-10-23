package com.example.jatin.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //holds the object for ProgressDialog
    ProgressDialog mProgressDialog;
    //stores the username and password as passed by the user
    String u, p;
    //stores the html after connecting to the page "http://moodle.iitb.ac.in/my"
    Document doc;
    //EditText objects to bind with the UI EditTexts
    EditText username,password;
    //holds the object for local DB "login_details"
    SharedPreferences mPreferences ;
    //Editor object for storing ID and PASSWORD and LOGIN value in the DB
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Login");
        //creates the local DB which can be accessed from within the application only
        mPreferences=getSharedPreferences("login_details",  Context.MODE_PRIVATE);
        //checks if the user is already logged in using the LOGIN value from local DB
        first_time_check();
        //binding with UI components
        Button btn_login = (Button) findViewById(R.id.btn_login);
        username = (EditText) findViewById(R.id.autoCompleteTextView);
        password = (EditText) findViewById(R.id.editText);
        //setting action to perform on clicking the LOGIN button
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //checks if internet available
                if(!isNetworkAvailable()){
                    Toast.makeText(getApplicationContext(), "Internet Unavailable.", Toast.LENGTH_LONG).show();
                    return;
                }
                //stores the username and password as passed by the user
                u = username.getText().toString().trim();
                p = password.getText().toString().trim();
                //checks conditions
                if(u.equals("")){
                    username.setError("Enter your moodle id");
                    return;
                }
                if(p.equals("")){
                    password.setError("Enter your moodle password");
                    return;
                }
                //create the object of class Title and calls execute() method on it
                new Title(u,p).execute();
            }
        });
    }
    //class defined to start the progress dialog and souping the moodle in background
    private class Title extends AsyncTask<Void, Void, Void> {
        String title;
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
            //initialize the progressDialog and displays it
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Logging into Moodle");
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //POST's the username and password to the moodle
                org.jsoup.Connection.Response res = Jsoup.connect("http://moodle.iitb.ac.in/login/index.php")
                        .data("username", this.user)
                        .data("password", this.pass)
                        .data("submit","Log in")
                        .data("formlogin","t")
                        .method(Connection.Method.POST)
                        .execute();
                //stores the cookies
                Map<String, String> cookies = res.cookies();
                //connects to the 'my' page on moodle and get its html
                doc = Jsoup.connect("http://moodle.iitb.ac.in/my")
                        .followRedirects(true)
                        .cookies(cookies)
                        .get();
                //gets the title from the 'my' page
                title = doc.title().trim().toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //if no response obtained
            if(title==null){
                Toast.makeText(getApplicationContext(), "Server error occured. Please try again.",
                        Toast.LENGTH_LONG).show();
            }
            //if student connected to ther network
            else if(title.equals("Redirect")){
                Toast.makeText(getApplicationContext(), "Not connected to IIT-B network.",
                        Toast.LENGTH_LONG).show();
            }
            //if successfully log's in
            else if(title.equals("IITB Moodle: My home")){
                //stores the ID and PASSWORD to local DB
                CalltoLocalDB(u, p);
                //redirects to the SubjectList Activity
                Intent ent =new Intent(MainActivity.this,SubjectListActivity.class);
                ent.putExtra("username",u);
                ent.putExtra("password",p);
                startActivity(ent);
                finish();
            }
            // wrong credentials passed
            else{
                Toast.makeText(getApplicationContext(), "Invalid login, please try again.", Toast.LENGTH_SHORT).show();
            }
            mProgressDialog.dismiss();
        }
    }
    //stores the ID, PASSWORD and LOGIN into local DB
    private void CalltoLocalDB(String id,String pass) {
        editor=mPreferences.edit();
        editor.putString("ID", id);
        editor.putString("PASSWORD", pass);
        editor.putBoolean("LOGIN", true);
        editor.apply();
        editor.commit();
    }
    //checks if the user is already logged in
    private void first_time_check() {
        Boolean login = mPreferences.getBoolean("LOGIN",false);
        if(login)
        {
            //if already logged in; passes the saved ID and PASSWORD in intent
            Intent i = new Intent(MainActivity.this, SubjectListActivity.class);
            i.putExtra("username",mPreferences.getString("ID",null));
            i.putExtra("password",mPreferences.getString("PASSWORD",null));
            startActivity(i);
            finish();
        }
    }
    //checks whether the network is available or not
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
