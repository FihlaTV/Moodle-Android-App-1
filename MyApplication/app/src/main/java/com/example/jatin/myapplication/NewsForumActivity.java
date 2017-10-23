package com.example.jatin.myapplication;

import android.app.ProgressDialog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewsForumActivity extends AppCompatActivity{
    Intent in;
    ProgressDialog pd;
    //stores the html page for subject page, news forum page and Post page
    Document doc_subject,doc_news_forum,doc_post;
    //stores the list of the links and the date from the the news forum page
    ArrayList<String> link_list = new ArrayList<String>();
    ArrayList<String> date_list = new ArrayList<String>();

    String url,user,pass;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    //stores the time difference through intent
    long set_time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_forum);
        getSupportActionBar().setTitle("News Forum");
        //gets the local DB login_details
        sp = getSharedPreferences("login_details", Context.MODE_PRIVATE);
        //gets the ID, PASSWORD, url of the subject page and the time difference
        in = getIntent();
        url = in.getStringExtra("url");
        user = in.getStringExtra("username");
        pass = in.getStringExtra("password");
        set_time = in.getLongExtra("set_time",986400000);
        //create the object of class Title and calls execute() method on it
        new NewsForumActivity.Title(user,pass).execute();
    }
    //class defined to start the progress dialog and souping the moodle in background
    private class Title extends AsyncTask<Void, Void, Void> {
        Element e, ele_sub,ele_auth,ele_con;
        Elements elements;
        String user;
        String pass;
        //list to add the title, author and content of the posts which were made in the time range
        ArrayList<String> list_subject = new ArrayList<String>();
        ArrayList<String> list_author = new ArrayList<String>();
        ArrayList<String> list_content = new ArrayList<String>();

        //constructor
        public Title(String userName, String password) {
            this.user = userName;
            this.pass = password;
        }

        //converts the </br> tag in html into \n newline
        public String br2nl(Element document) {
            document.select("br").append("\\n");
            document.select("p").prepend("\\n\\n");
            return document.text().replace("\\n", "\n");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //starting progress dialog
            pd = new ProgressDialog(NewsForumActivity.this);
            pd.setMessage("Loading new posts...");
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
                        .data("submit","Log in")
                        .data("formlogin","t")
                        .method(Connection.Method.POST)
                        .execute();
                //gets the cookies
                Map<String, String> cookies = res.cookies();
                //gets the html for the course page which has been selected from the list
                doc_subject = Jsoup.connect(url.trim())
                        .followRedirects(true)
                        .cookies(cookies)
                        .get();
               //select link from the page which contains /forum/ in it
                e = doc_subject.select("a[href*=/forum/]").first();
                //gets the value of href from the <a> tag
                String l = e.attr("href").toString();
                //gets the html for the news forum page
                doc_news_forum = Jsoup.connect(l)
                        .followRedirects(true)
                        .cookies(cookies)
                        .get();
                //gets the link, date for last post made & stores it in a list
                elements = doc_news_forum.select("td.lastpost > a[href^=http://moodle.iitb.ac.in/mod/forum/discuss.php?]");
                for (Element ele : elements) {
                    link_list.add(ele.attr("href").toString());
                    date_list.add(ele.text().toString());
                }
                for (String s: date_list) {
                        // creating date time format and parsing the
                        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM y, h:m a");
                        Date date1 = dateFormat.parse(s);
                        //format: Mon, 05 Oct 2017, 08:45 AM; gets the current date and time
                        Date date2 = dateFormat.parse(new SimpleDateFormat("EEE, d MMM y, h:m a").format(Calendar.getInstance().getTime()));
                        //finds the difference between current time and the Posts' time
                        long diff = date2.getTime() - date1.getTime();
                        if(diff > set_time) {
                            //time exceeds the given limit
                            break;
                        }
                        else{
                            //time within the range
                            doc_post = Jsoup.connect(link_list.get(date_list.indexOf(s)).trim())
                                    .followRedirects(true)
                                    .cookies(cookies)
                                    .get();
                            //selecting the div which contains the content of the post
                            ele_con = doc_post.select("div[class=posting fullpost]").first();
                            //converting the html breaks into new lines in java
                            String text = br2nl(ele_con);
                            //selecting subject and author using div class
                            ele_sub = doc_post.select("div[class=subject]").first();
                            ele_auth = doc_post.select("div[class=author]").first();
                            //adding the subject and author name into the lists to use them in onPostExecute method
                            list_subject.add(ele_sub.text().toString().trim());
                            list_author.add(ele_auth.text().toString().trim());
                            //adding content into the list
                            list_content.add(text);
                        }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //creating a list of type ThreeStrings
            List<ThreeStrings> threeStringsList = new ArrayList<>();
            //adding ThreeStrings objects into the list
            for(int i=0;i<list_content.size();i++) {
                threeStringsList.add(new ThreeStrings(list_subject.get(i),list_author.get(i),list_content.get(i)));
            }
            ListView listView = (ListView) findViewById(R.id.custom_list);
            //creating and setting the customAdapter
            CustomAdapter cAdapter = new CustomAdapter(getApplicationContext(), R.layout.list_row, threeStringsList);
            listView.setAdapter(cAdapter);
            //if no posts made in the given time limit
            if(list_subject.isEmpty())
                Toast.makeText(getApplicationContext(),"No posts since the date and time you selected.",Toast.LENGTH_LONG).show();
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
        Intent i = new Intent(NewsForumActivity.this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
