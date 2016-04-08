package com.example.simonpradier.cornerforumoffline;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.view.View.OnClickListener;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Menu_Activity extends AppCompatActivity {

    int NUMBEROFTHREADS = 40;
    ListView mylistView;
    Button mybutton;
    String[] TitleArray = new String[NUMBEROFTHREADS];
    String[] URLArray = new String[NUMBEROFTHREADS];
    String[] AuthorArray = new String[NUMBEROFTHREADS];
    String[] WebsiteArray = new String[NUMBEROFTHREADS];
    Boolean ThreadTitlesDownloaded = false;
    String url = "http://www.corner.bigblueinteractive.com";
    ArrayList<String> Titles = new ArrayList<String>();
    ArrayList<String> Bodys = new ArrayList<String>();
    ArrayList<String> Infos = new ArrayList<String>();
    ArrayList<String> Quotes = new ArrayList<String>();
    ArrayList<String> URLlinks = new ArrayList<String>();
    String[][] myTitles = new String[NUMBEROFTHREADS][];
    String[][] myBodys = new String[NUMBEROFTHREADS][];
    String[][] myInfos = new String[NUMBEROFTHREADS][];
    String[][] myQuotes = new String[NUMBEROFTHREADS][];
    String[][] myLinks = new String[NUMBEROFTHREADS][];

    private String myTitlesfile = "myTitles.txt";
    private String myWebsitefile = "myWebsites.txt";
    private String myURLsfile = "myUrls.txt";
    private String myAuthorsfile = "myAuthors.txt";
    private String mySecondTitlesfile = "mySecondTitles.txt";
    private String myLinksfile = "myLinks.txt";
    private String myInfosfile = "myInfos.txt";
    private String myQuotesfile = "myQuotes.txt";
    private String myBodysfile = "myBodys.txt";
    String catchstring = " XXXXXSimonisHere";
    String catchstring2 = " YYYYYSimonisHere";
    private ProgressBar progressBar;
    static Boolean flag = false;
    Boolean dataexists;
    Boolean clicked = false;
    boolean progressBarIsShowing;
    Boolean isInternetWorking = false;


    private OnClickListener mylistner = new OnClickListener() {
        public void onClick(View v) {
            new checkInternet().execute();
            if (v.getId() == R.id.button) {

                if (isInternetWorking && isNetworkAvailable() ) {
                    if(!clicked) {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress(0);
                        new myData().execute();
                        Boolean isInternetWorking = false;
                    }
                } else {
                    Toast.makeText(Menu_Activity.this, "Please try again.", Toast.LENGTH_LONG).show();
                }
                flag =false;





            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if(progressBar.getVisibility() == View.VISIBLE)
        {
            progressBarIsShowing = true;
        }
        else
            progressBarIsShowing = false;

        savedInstanceState.putStringArray("TitleArray", TitleArray);
        savedInstanceState.putStringArray("URLArray", URLArray);
        savedInstanceState.putStringArray("AuthorArray", AuthorArray);
        savedInstanceState.putStringArray("WebsiteArray", WebsiteArray);

        savedInstanceState.putInt("Progessbarprogress", progressBar.getProgress());

        savedInstanceState.putBoolean("progressBarIsShowing", progressBarIsShowing);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        progressBar.setMax(38);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        mylistView = (ListView) findViewById(R.id.list);
        new checkInternet().execute();




        if (savedInstanceState != null)
        {


            TitleArray = savedInstanceState.getStringArray("TitleArray");
            URLArray = savedInstanceState.getStringArray("URLArray");
            AuthorArray = savedInstanceState.getStringArray("AuthorArray");
            WebsiteArray = savedInstanceState.getStringArray("WebsiteArray");
            progressBar.setProgress(savedInstanceState.getInt("ProgressBarprogress"));
            progressBarIsShowing = savedInstanceState.getBoolean("progressBarIsShowing");
            if (progressBarIsShowing)
                progressBar.setVisibility(View.VISIBLE);
            else
                progressBar.setVisibility(View.GONE);

            simpleArray();

        }
        //tinydb = new TinyDB(this);


        ThreadTitlesDownloaded = false;
        mybutton = (Button) findViewById(R.id.button);
        mybutton.setOnClickListener(mylistner);





        mylistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                //String position1 = String.valueOf(position);

                if (WebsiteArray[position] != null) {//createnew activity whenclicked passed it the website source data
                    Intent newintent = new Intent(Menu_Activity.this, Thread_Activity.class);
                    newintent.putExtra("myWebsiteArray", WebsiteArray[position]);
                    newintent.putExtra("position", position);
                    startActivity(newintent);
                }

            }
        });
    }


    @Override
    protected void onStop() {//save on stop
        super.onStop();
        flag =false;

        if (dataexists) {
            saveData1();
            saveData();
            Log.d("State", "on stop");
        }

    }








    @Override
    protected void onResume() {//load on resume
        super.onResume();
        Log.d("State", "trying Loaded 1");
        // if (settings.getBoolean("saved", true)) {
        if(!flag) {
            flag = true;
            File file = new File(getFilesDir(), myTitlesfile);
            if (file.exists() && !file.isDirectory()) {
                InputStream is = null;
                try {
                    is = new BufferedInputStream(new FileInputStream(file));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(is));


                try {
                    if (!(br.readLine() == null)) {
                        new loadmyData().execute();
                        loadData1();
                        Log.d("State", "Loaded 1");
                        simpleArray();
                        dataexists = true;

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
        else
            dataexists = false;
    }

    @Override
    protected void onStart() {//load on start
        super.onStart();
        Log.d("State", "trying Loaded 1");
        // if (settings.getBoolean("saved", true)) {
        if(!flag) {
            flag = true;
            File file = new File(getFilesDir(), myTitlesfile);
            if (file.exists() && !file.isDirectory()) {
                InputStream is = null;
                try {
                    is = new BufferedInputStream(new FileInputStream(file));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(is));


                try {
                    if (!(br.readLine() == null)) {
                        new loadmyData().execute();
                        loadData1();
                        Log.d("State", "Loaded 1");
                        dataexists = true;
                        simpleArray();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
        else
            dataexists = false;
    }

    private void saveData1() {//save authors urls and titles
        try {
            File file1 = new File(getFilesDir(), myTitlesfile);
            FileOutputStream fos1 = new FileOutputStream(file1);
            for (int i = 0; i < NUMBEROFTHREADS; i++) {
                fos1.write(TitleArray[i].getBytes());
                fos1.write("\n".getBytes());
            }
            fos1.flush();
            fos1.close();
            File file2 = new File(getFilesDir(), myAuthorsfile);
            FileOutputStream fos2 = new FileOutputStream(file2);
            for (int i = 0; i < NUMBEROFTHREADS; i++) {
                fos2.write(AuthorArray[i].getBytes());
                fos2.write("\n".getBytes());
            }
            fos2.flush();
            fos2.close();

            File file3 = new File(getFilesDir(), myURLsfile);
            FileOutputStream fos3 = new FileOutputStream(file3);
            for (int i = 0; i < NUMBEROFTHREADS; i++) {
                fos3.write(URLArray[i].getBytes());
                fos3.write("\n".getBytes());
            }
            fos3.flush();
            fos3.close();
            File file4 = new File(getFilesDir(), myWebsitefile);
            FileOutputStream fos4 = new FileOutputStream(file4);
            for (int i = 0; i < NUMBEROFTHREADS; i++) {
                fos4.write(WebsiteArray[i].getBytes());
                fos4.write(catchstring2.getBytes());
            }
            fos4.flush();
            fos4.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void saveData() {//save the website source data
        try {

            File file4 = new File(getFilesDir(), myWebsitefile);
            FileOutputStream fos4 = new FileOutputStream(file4);
            for (int i = 0; i < NUMBEROFTHREADS; i++) {
                fos4.write(WebsiteArray[i].getBytes());
                fos4.write(catchstring2.getBytes());
            }
            fos4.flush();
            fos4.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


       private void loadData1() {// load the title, authors and urls
        try {
            File file1 = new File(getFilesDir(), myTitlesfile);
            InputStream is = new
                    BufferedInputStream(new FileInputStream(file1));
            BufferedReader br = new
                    BufferedReader(new InputStreamReader(is));
            String input;

            int k = 0;
            int l = 0;
            while ((input = br.readLine()) != null) {
                TitleArray[k] = input;
                k++;
            }
            br.close();
            is.close();

            File file2 = new File(getFilesDir(), myAuthorsfile);
            InputStream is2 = new
                    BufferedInputStream(new FileInputStream(file2));
            BufferedReader br2 = new
                    BufferedReader(new InputStreamReader(is2));


            k = 0;
            while ((input = br2.readLine()) != null) {
                AuthorArray[k] = input;
                k++;
            }
            br2.close();
            is2.close();

            File file3 = new File(getFilesDir(), myURLsfile);
            InputStream is3 = new
                    BufferedInputStream(new FileInputStream(file3));
            BufferedReader br3 = new
                    BufferedReader(new InputStreamReader(is3));


            k = 0;
            while ((input = br3.readLine()) != null) {
                URLArray[k] = input;
                k++;
            }
            br3.close();
            is3.close();

            String[] splitter;
            StringBuilder everything = new StringBuilder();
            File file4 = new File(getFilesDir(), myWebsitefile);
            InputStream is4 = new
                    BufferedInputStream(new FileInputStream(file4));
            BufferedReader br4 = new
                    BufferedReader(new InputStreamReader(is4));


            k = 0;
            while ((input = br4.readLine()) != null) {
                everything.append(input + "\n");
            }
            splitter = everything.toString().split(catchstring2);
            for(int i = 0; i < 40 ; i++)
            {
                WebsiteArray[i] = splitter[i];
            }
            br4.close();
            is4.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {//load the website source data
        try {
            String[] splitter;
            StringBuilder everything = new StringBuilder();
            File file4 = new File(getFilesDir(), myWebsitefile);
            InputStream is4 = new
                    BufferedInputStream(new FileInputStream(file4));
            BufferedReader br4 = new
                    BufferedReader(new InputStreamReader(is4));

            String input;
            int k = 0;
            while ((input = br4.readLine()) != null) {
                everything.append(input + "\n");
            }
            splitter = everything.toString().split(catchstring2);
            for(int i = 0; i < 40 ; i++)
            {
                WebsiteArray[i] = splitter[i];
            }
            br4.close();
            is4.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void simpleArray() {// fill up the listview with appropriate content

        String[] from = new String[]{"rowid", "col_1"};
        int[] to = new int[]{R.id.first_line, R.id.second_line};

        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();

        for (int i = 0; i < TitleArray.length; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("rowid", TitleArray[i]);
            map.put("col_1", AuthorArray[i]);
            fillMaps.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.list_item, from, to);
        mylistView.setAdapter(adapter);
    }


    private void lockScreenOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void unlockScreenOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    private boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();


    }
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }
    public static boolean hasInternetAccess(Context context) {
        if (isOnline(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("http://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                Log.e("TAG", "Error checking internet connection", e);
            }
        } else {
            Log.d("TAG", "No network available!");
        }
        return false;
    }


    void HtmlParse() throws IOException {


            Document doc;
            Document doc1;

            int j = 0;

            String url = "http://www.corner.bigblueinteractive.com";
            doc = Jsoup.parse(new URL(url).openStream(), "windows-1252", url);


             Element table = doc.select("table").get(1);//second table is where threads a are located
            Elements rows = table.select("tr");

            for (int i = 0; i < rows.size(); i++) {//iterate to get all threads
                if (rows.get(i).className().contains("forumline")) {


                    URLArray[j] = rows.get(i).select("a").attr("abs:href").concat("&thread_page=1");//get URLS AUTHORS AND TITLEs


                    AuthorArray[j] = rows.get(i).select("td").get(1).text();


                    TitleArray[j] = rows.get(i).select("a").text();


                    //if a thread has more than one page add the other pages to the website array;
                    doc1 = Jsoup.parse(new URL(URLArray[j]).openStream(), "windows-1252", URLArray[j]);
                    String URLholder = URLArray[j].replace("thread_page=1", "thread_page=");
                    WebsiteArray[j] = doc1.toString();
                    int k =2;
                    while(doc1.toString().contains("<span class=\"top_pages\">Pages:"))
                    {

                        URLArray[j] = URLholder;
                        URLArray[j] = URLArray[j].concat(Integer.toString(k));
                        Log.d("website", URLArray[j]);
                        doc1 = Jsoup.connect(URLArray[j]).get();
                        if(doc1.toString().contains("<div class=\"thread_post\">")) {
                            WebsiteArray[j] = WebsiteArray[j].concat(catchstring).concat(doc1.toString());
                            k++;
                        }
                        else
                            break;

                    }

                    progressBar.setProgress(j);
                    j++;
                }

            }




}


    private class myData extends AsyncTask<Void, Void, Void> {//parsing takes place in this asynctask


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lockScreenOrientation();
            clicked = true;

        }

        @Override
        protected Void doInBackground(Void... params) {


                try {
                    HtmlParse();


                } catch (IOException e) {

                }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressBar.setVisibility(View.GONE);
            simpleArray();
            unlockScreenOrientation();
            clicked = false;
            dataexists = true;


        }
    }



    private class loadmyData extends AsyncTask<Void, Void, Void> {//the heavier loading takes place in this asynctask


        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected Void doInBackground(Void... params) {



            loadData();




            return null;
        }

        @Override
        protected void onPostExecute(Void result) {



        }
    }

    private class checkInternet extends AsyncTask<Void, Void, Void> {//this asynctask it to check whether the phone is connected to a funcitonning connection




        @Override
        protected Void doInBackground(Void... params) {



           isInternetWorking = hasInternetAccess(getApplicationContext());

        return null;
        }


    }
}

