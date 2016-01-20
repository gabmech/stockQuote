package com.example.gabrielmechali.stockquote;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class StockInfoActivity extends Activity {

    private static final String TAG = "STOCKQUOTE";
    TextView companyNameTextView;
    TextView yearLowTextView;
    TextView yearHighTextView;
    TextView daysLowTextView;
    TextView daysHighTextView;
    TextView lastTradePriceTextView;
    TextView changeTextView;
    TextView dailyRangeTextView;

    static final String KEY_ITEM = "quote";
    static final String KEY_NAME = "Name";
    static final String KEY_YEAR_LOW = "YearLow";
    static final String KEY_YEAR_HIGH = "YearHigh";
    static final String KEY_DAYS_LOW = "DaysLow";
    static final String KEY_DAYS_HIGH = "DaysHigh";
    static final String KEY_LAST_TRADE_PRICE = "LastTradePriceOnly";
    static final String KEY_CHANGE = "Change";
    static final String KEY_DAYS_RANGE = "DaysRange";

    String companyName = "";
    String yearLow = "";
    String yearHigh = "";
    String daysLow = "" ;
    String daysHigh = "";
    String lastTradePriceOnly = "";
    String change = "";
    String dailyPriceRange = "";

    String yahooURLFirst = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22";
    String getYahooURLSecond = "%22)&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_info);

        Intent intent = getIntent();
        String stockSymbol = intent.getStringExtra(MainActivity.STOCK_SYMBOL);


        companyNameTextView = (TextView) findViewById(R.id.companyNameTextView);
        yearLowTextView = (TextView) findViewById(R.id.yearLowTextView);
        yearHighTextView = (TextView) findViewById(R.id.yearHighTextView);
        daysLowTextView = (TextView) findViewById(R.id.daysLowTextView);
        daysHighTextView = (TextView) findViewById(R.id.daysHighTextView);
        lastTradePriceTextView = (TextView) findViewById(R.id.lastTradePriceTextView);
        changeTextView = (TextView) findViewById(R.id.changeTextView);
        dailyRangeTextView = (TextView) findViewById(R.id.dailyPriceRangeTextView);

        Log.d(TAG, "Before URL creation " + stockSymbol);

        final String yqlURL = yahooURLFirst + stockSymbol + getYahooURLSecond;

        new MyAsyncTask().execute(yqlURL);

    }

    private class MyAsyncTask extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... args){

            try {
                System.err.println("Args is " + args[0]);

                URL url = new URL(args[0]);
                System.err.print("STEP1.1");

                URLConnection connection;
                System.err.print("STEP1.2");
                connection = url.openConnection();
                System.err.print("STEP2");
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                int responseCode = httpConnection.getResponseCode();
                System.err.print("STEP3");

                if (responseCode == HttpURLConnection.HTTP_OK){
                    InputStream in = httpConnection.getInputStream();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document dom = db.parse(in);
                    Element docEle = dom.getDocumentElement();
                    NodeList nl = docEle.getElementsByTagName("quote");
                    System.err.print("STEP4");

                    if (nl!=null && nl.getLength()>0){
                        for(int i =0; i<nl.getLength(); i++){
                            StockInfo theStock = getStockInformation(docEle);
                            companyName=theStock.getName();
                            yearHigh = theStock.getYearHigh();
                            yearLow = theStock.getYearLow();
                            daysHigh = theStock.getDaysHigh();
                            daysLow = theStock.getDaysLow();
                            lastTradePriceOnly = theStock.getLastTradePrice();
                            change = theStock.getChange();
                            dailyPriceRange = theStock.getDaysRange();
                            System.err.print("STEP5");



                        }

                    }

                }
            }
            catch (MalformedURLException e){
                Log.d(TAG, "MalformedURLException ", e);
            } catch (IOException e){
                Log.d(TAG, "IOException ", e);
            }catch (ParserConfigurationException e){
                Log.d(TAG, "Parser Configuration ", e);
            }catch (SAXException e){
                Log.d(TAG, "SAX Exception ", e);
            }
            finally{}
            return null;
        }

        protected void onPostExecute(String result){
            companyNameTextView.setText(companyName);
            yearLowTextView.setText("Year Low: " + yearLow);
            yearHighTextView.setText("Year High: " + yearHigh);
            daysLowTextView.setText("Day Low: " + daysLow);
            daysHighTextView.setText("Day High: " + daysHigh);
            lastTradePriceTextView.setText("Last Price: " + lastTradePriceOnly);
            changeTextView.setText("Change: " + change);
            dailyRangeTextView.setText("Daily Price Range: " + dailyPriceRange);

        }

        private StockInfo getStockInformation(Element entry){

            String stockName = getTextValue(entry, "Name");
            String stockYearLow = getTextValue(entry, "YearLow");
            String stockYearHigh = getTextValue(entry, "YearHigh");
            String stockDaysLow = getTextValue(entry, "DaysLow");
            String stockDaysHigh = getTextValue(entry, "DaysHigh");
            String stockLastTradedPriceOnly = getTextValue(entry, "LastTradePriceOnly");
            String stockChange = getTextValue(entry, "Change");
            String stockDailyPriceRange = getTextValue(entry, "DaysRange");

            StockInfo theStock = new StockInfo(stockDaysLow, stockDaysHigh, stockYearLow, stockYearHigh, stockName, stockLastTradedPriceOnly,stockChange,stockDailyPriceRange);
            return theStock;

        }

        private String getTextValue(Element entry, String tagName){

            String tagValueToReturn = null;
            NodeList nl = entry.getElementsByTagName(tagName);
            if(nl!=null && nl.getLength()>0){
                Element element = (Element) nl.item(0);
                tagValueToReturn = element.getFirstChild().getNodeValue();
            }
            return tagValueToReturn;
        }

    }






}
