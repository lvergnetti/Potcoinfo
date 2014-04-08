package com.jok.potcoinfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.jok.potcoinfo.sources.BitcoinAverage;
import com.jok.potcoinfo.sources.Mintpal;

public class ConverterActivity extends Activity{
	
	//SPINNER GETS VALUES 1 2 OR 3
	//MULTIPLIER IS WHATEVER IS ENTERED
	

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    int firstRun=1;
    public Vibrator v;
	
	public Mintpal getMintpalStats(){
		InputStream source = retrieveStream("https://api.mintpal.com/market/stats/POT/BTC");
		Reader reader = new InputStreamReader(source);
		Gson gson = new Gson();
		Mintpal pal[] =gson.fromJson(reader,Mintpal[].class);
		return pal[0];
	}
	
	public double getBitcoinPrice(){
		InputStream source = retrieveStream("https://api.bitcoinaverage.com/ticker/global/USD/");
		Reader reader = new InputStreamReader(source);
		Gson gson = new Gson();
		BitcoinAverage bitavg = gson.fromJson(reader, BitcoinAverage.class);
		return bitavg.bitcoin_price;
	}

		
	

	private InputStream retrieveStream(String url) {
        
        DefaultHttpClient client = new DefaultHttpClient(); 
        HttpGet getRequest = new HttpGet(url);
          
        try {
           
           HttpResponse getResponse = client.execute(getRequest);
           final int statusCode = getResponse.getStatusLine().getStatusCode();
           
           if (statusCode != HttpStatus.SC_OK) { 
              Log.w(getClass().getSimpleName(),"Error " + statusCode + " for URL " + url); 
              return null;
           }
           HttpEntity getResponseEntity = getResponse.getEntity();
           return getResponseEntity.getContent();
        } 
        catch (IOException e) {
           getRequest.abort();
           Log.w(getClass().getSimpleName(), "Error for URL " + url, e);
        }
        
        return null;
        
     }

	 
    private class loadStats extends AsyncTask<String, Void, String> {
    	double multiplier;
    	double price;
    	double priceUsd;
    	int currency;  //1 = POT  2 = BTC  3 = USD
    	@Override
        protected String doInBackground(String... params) {
        	//INITIALIZE multiplier
        	//INITILZIE currency
        	price=getMintpalStats().getLast_price();
            priceUsd=getBitcoinPrice(); 
            
            String highlowlast ="";
			return highlowlast;
            
        }
       

        @Override
        protected void onPostExecute(String result) {
        	v.vibrate(45);
        	
            switch (currency){
			case 1:
				//POT
				//GET USD
				//output (multiplier*(price * priceUsd))
				//GET BTC
				//output (multiplier*price);
				break;
				
				
			case 2:
				//BTC
				//GET POT
				//output (multiplier*(1/price))
				//GET USD
				//output (multiplier*(priceUsd))
				break;
				
			case 3:
				//USD
				//GET POT 
				//output (multiplier*(1/(price * priceUsd)))
				//GET BTC
				//output (multiplier*(1/priceUsd))
				break;
		}
		
	
            TextView pullText = (TextView)findViewById(R.id.pull_to_refresh_text);
       	    pullText.setTextColor(Color.parseColor("#f1f1f1"));
            PullToRefreshScrollView pullToRefreshView = (PullToRefreshScrollView) findViewById(R.id.scrollView1);
            pullToRefreshView.onRefreshComplete();
        }
        @Override
        protected void onPreExecute() {
        
        	
        	TextView market_value = (TextView)findViewById(R.id.market_value);
        	if(firstRun==1){
        		market_value.setTextColor(Color.parseColor("#3aa748"));
        	}else{
        		market_value.setTextColor(Color.parseColor("#a6a6a6"));
        	}
          
            firstRun=0;
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
           
            case R.id.qr:
          	  startActivity(new Intent(ConverterActivity.this, ScannerActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    
}

	

