package com.example.simonpradier.cornerforumoffline;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Simon on 2016-03-23.
 */
public class Thread_Activity extends AppCompatActivity {



    ArrayList<String> Titles = new ArrayList<String>();
    ArrayList<String> Bodys = new ArrayList<String>();
    ArrayList<String> Infos = new ArrayList<String>();
    ArrayList<String> Quotes = new ArrayList<String>();
    ArrayList<String> URLlinks = new ArrayList<String>();

    String[] myTitles ;
    String[] myBodys ;
    String[] myInfos ;
    String[] myQuotes;
    String[] myLinks;
    int position;
    String WebsiteString;
    ListView mylistView ;
    private ProgressBar spinner;
    Boolean saved;
    String catchstring = " XXXXXSimonisHere";


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);





        savedInstanceState.putStringArray("myTitles", myTitles);
        savedInstanceState.putStringArray("myBodys", myBodys);
        savedInstanceState.putStringArray("myInfos", myInfos );
        savedInstanceState.putStringArray("myQuotes", myQuotes);
        savedInstanceState.putStringArray("myLinks", myLinks);



    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thread_layout);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        mylistView = (ListView) findViewById(R.id.list);
        spinner.setVisibility(View.GONE);
        saved = false;
        if (savedInstanceState != null)
        {


            myTitles = savedInstanceState.getStringArray("myTitles");
            myBodys = savedInstanceState.getStringArray("myBodys");
            myInfos = savedInstanceState.getStringArray("myInfos");
            myQuotes = savedInstanceState.getStringArray("myQuotes");
            myLinks = savedInstanceState.getStringArray("myLinks");
            //spinner.setVisibility(View.GONE);
            saved = true;
            if(myTitles != null) {
                simpleArray();
            }
            else {

                Toast.makeText(Thread_Activity.this, "Data not loaded !", Toast.LENGTH_LONG).show();
                Intent newintent = new Intent(Thread_Activity.this, Menu_Activity.class);
                startActivity(newintent);
            }

        }




        Intent intent = getIntent();
        Bundle bd2 = intent.getExtras();
        if(bd2 != null && !saved )
        {

            WebsiteString = (String) bd2.get("myWebsiteArray");
            new myData2().execute(WebsiteString);
            Log.d("State", "Here");
        }






        mylistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                if (myLinks[position].contains("http")) {//lsitener for the fetched intenet links
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(myLinks[position])));
                }
            }
        });


    }




    private void simpleArray(){// filling the listview with appropriatee content

        String[] from = new String[] {"rowid", "col_1", "col_2","col_3","col_4"};
        int[] to = new int[] { R.id.first_line, R.id.second_line, R.id.third_line, R.id.fourth_line,R.id.fifth_line};




        List<HashMap<String,String>> fillMaps = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < myTitles.length; i++){
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("rowid", myTitles[i]);
            map.put("col_1", myInfos[i]);
            map.put("col_2", myQuotes[i]);
            map.put("col_3", myBodys[i]);
            map.put("col_4", myLinks[i]);
            fillMaps.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.three_items, from, to);
        //Linkify.addLinks(mylistView., Linkify.ALL);
        mylistView.setAdapter(adapter);
    }





    void HtmlParse(String Website) throws IOException {//parsing algortithm specific to the web page

        Titles.clear();
        Bodys.clear();
        Infos.clear();
        Quotes.clear();
        URLlinks.clear();
        Boolean alreadySawYou = false;
        Document doc;

        String[] splitter = {Website};
        if(Website.contains(catchstring))
        splitter = Website.split(catchstring);



        for(int k = 0; k< splitter.length;k++)

        {
            doc = Jsoup.parse(splitter[k]);





            int finder = 0;


            Elements findbeginning = doc.select("div");
            Elements br = doc.select("br");

            for (Element src : br) {
                src.append("brton1");
            }


            for (int i = finder; i < findbeginning.size(); i++) {
                if (findbeginning.get(i).className().contains("thread_main")) {

                    finder = i;
                }
            }


            for (int i = finder; i < findbeginning.size(); i++) {
                findbeginning.get(i).getElementsByTag("img").remove();


                if ((findbeginning.get(i).className().contains("thread_main")) && !alreadySawYou)

                {
                    alreadySawYou = true;


                    if (findbeginning.get(i).getElementsByClass("thread_info").text().contains(": link")) {
                        Infos.add(findbeginning.get(i).getElementsByClass("thread_info").text().replace(": link", ""));//first paragraph author and time
                    } else {
                        Infos.add(findbeginning.get(i).getElementsByClass("thread_info").text());
                    }
                    findbeginning.get(i).getElementsByClass("thread_info").remove();
                    Titles.add(findbeginning.get(i).getElementsByClass("thread_post_header").text());//first paragraph title

                    if (findbeginning.get(i).select("a").text().contains("New Window")) {


                        URLlinks.add(findbeginning.get(i).getElementsByClass("thread_post_body").select("a").get(0).attr("abs:href"));
                        findbeginning.get(i).getElementsByClass("thread_post_body").select("a").remove();
                    } else
                        URLlinks.add(" ");

                    if (findbeginning.get(i).select("tbody").size() > 0)//quotes
                    {
                        Quotes.add(findbeginning.get(i).getElementsByTag("tbody").text().replace("brton1", "\n"));
                        findbeginning.get(i).getElementsByTag("tbody").remove();
                    } else
                        Quotes.add(" ");

                    Bodys.add(findbeginning.get(i).getElementsByClass("thread_post_body").text().replace("brton1", "\n").replace("- (", "( Link below"));//fist paragraph body


                } else if (findbeginning.get(i).className().equals("thread_post")) {
                    if (findbeginning.get(i).getElementsByClass("thread_info").text().contains(": link")) {
                        Infos.add(findbeginning.get(i).getElementsByClass("thread_info").text().replace(": link", ""));//first paragraph author and time
                    } else {
                        Infos.add(findbeginning.get(i).getElementsByClass("thread_info").text());
                    }
                    findbeginning.get(i).getElementsByClass("thread_info").remove();
                    Titles.add(findbeginning.get(i).getElementsByClass("thread_post_header").text());//TITLE

                    if (findbeginning.get(i).select("a").text().contains("New Window")) {


                        URLlinks.add(findbeginning.get(i).getElementsByClass("thread_post_body").select("a").get(0).attr("abs:href"));
                        findbeginning.get(i).getElementsByClass("thread_post_body").select("a").remove();
                    } else
                        URLlinks.add(" ");


                    if (findbeginning.get(i).select("tbody").size() > 0) {
                        String mystring = findbeginning.get(i).getElementsByTag("tbody").text().replace("Quote:", "\"").concat("\"");
                        findbeginning.get(i).getElementsByTag("tbody").remove();

                        if (findbeginning.get(i).getElementsByClass("thread_post_body").text().contains("said")) {
                            String[] mytwostrings = findbeginning.get(i).getElementsByClass("thread_post_body").text().split("said");
                            Quotes.add(mytwostrings[0].concat("said: ").concat(mystring).replace("brton1", "\n"));
                            Bodys.add(mytwostrings[1].replace(":", " ").replace("- ()", "LINK: ").replace("brton1", "\n"));
                        } else {
                            Quotes.add(mystring.replace("brton1", "\n"));
                            Bodys.add(findbeginning.get(i).getElementsByClass("thread_post_body").text().replace("- (", " ( Link below").replace("brton1", "\n"));

                        }

                    } else {
                        Quotes.add(" ");

                        Bodys.add(findbeginning.get(i).getElementsByClass("thread_post_body").text().replace("- (", " ( Link below ").replace("brton1", "\n"));//BODY
                    }

                }

            }

        }

        myTitles = Titles.toArray(new String[Titles.size()]);
        myBodys = Bodys.toArray(new String[Bodys.size()]);
        myInfos = Infos.toArray(new String[Infos.size()]);
        myQuotes = Quotes.toArray(new String[Infos.size()]);
        myLinks = URLlinks.toArray(new String[URLlinks.size()]);



    }


    private class myData2 extends AsyncTask<String, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinner.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(String... params) {


            try {
                HtmlParse(params[0]);
            } catch (IOException e) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            spinner.setVisibility(View.GONE);
            simpleArray();

        }
    }

}
