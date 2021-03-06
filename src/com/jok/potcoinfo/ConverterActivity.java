package com.jok.potcoinfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.jok.potcoinfo.sources.BitcoinAverage;
import com.jok.potcoinfo.sources.Mintpal;

public class ConverterActivity extends Activity{
	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    
    	int firstRun=1;
    	public Vibrator v;
    	int currency=1; //1 = POT  2 = BTC  3 = USD
    	double multiplier; 
    	double priceInBTC; 
    	double btcInUSD;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout();
        changeCurrency();
       
        }
        public void changeCurrency(){
        	Typeface chunk = Typeface.createFromAsset(getAssets(),"fonts/Chunkfive.ttf");
        	final TextView conText1=(TextView)findViewById(R.id.converted_text_1);
        	final TextView conText2=(TextView)findViewById(R.id.converted_text_2);
        	final EditText multi_text =(EditText)findViewById(R.id.multiplyer);
        	 final ImageView currencyImg = (ImageView) findViewById(R.id.current_currency);
        	 final ImageView convertedImg1 = (ImageView) findViewById(R.id.converted_img_1);
        	 final ImageView convertedImg2 = (ImageView) findViewById(R.id.converted_img_2);
        	 
        	 conText1.setTypeface(chunk);
        	 conText2.setTypeface(chunk);
             currencyImg.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                	 multi_text.setText("");
                	 conText1.setText("0.00");
                	 conText2.setText("0.00");
                    if(currency==1){
                 	   currency=2;
                 	   currencyImg.setImageResource(R.drawable.btc);
                 	   convertedImg1.setImageResource(R.drawable.usd);
                 	   convertedImg2.setImageResource(R.drawable.leaf);
                 	   multi_text.setHint("BTC");
                 	   multi_text.setTextColor(Color.parseColor("#f1f1f1"));
                 	   conText1.setTextColor(Color.parseColor("#d5c12b"));
                 	   conText2.setTextColor(Color.parseColor("#3ca748"));
                    }
                    else if(currency==2){
                 	   currency=3;
                 	   currencyImg.setImageResource(R.drawable.usd);
                 	  convertedImg1.setImageResource(R.drawable.leaf);
                 	 convertedImg2.setImageResource(R.drawable.btc);
                 	   multi_text.setHint("USD");
                 	   multi_text.setTextColor(Color.parseColor("#d5c12b"));
                 	   conText1.setTextColor(Color.parseColor("#3ca748"));
                	   conText2.setTextColor(Color.parseColor("#f1f1f1"));
                    }
                    else if(currency==3){
                 	   currency=1;
                 	   currencyImg.setImageResource(R.drawable.leaf);
                 	  convertedImg1.setImageResource(R.drawable.btc);
                 	 convertedImg2.setImageResource(R.drawable.usd);
                 	   multi_text.setHint("POT");
                 	   multi_text.setTextColor(Color.parseColor("#3ca748"));
                 	   conText1.setTextColor(Color.parseColor("#f1f1f1"));
                	   conText2.setTextColor(Color.parseColor("#d5c12b"));
                    }
                 }
             });
        }
    
    	public void setLayout(){
    		Typeface chunk = Typeface.createFromAsset(getAssets(),"fonts/Chunkfive.ttf");
            getActionBar().setTitle("");
            setContentView(R.layout.activity_converter);
            StrictMode.setThreadPolicy(policy);
            getActionBar().setDisplayUseLogoEnabled(true);
            getActionBar().setLogo(R.drawable.logo);
            
            
            EditText multi_text =(EditText)findViewById(R.id.multiplyer);
            multi_text.setTextColor(Color.parseColor("#3ca748"));
            multi_text.setBackgroundResource(R.drawable.actionbar_background);
            multi_text.setTypeface(chunk);
            
            
            
            TextView conText1=(TextView)findViewById(R.id.converted_text_1);
        	TextView conText2=(TextView)findViewById(R.id.converted_text_2);
        	conText1.setText("0.00");
        	conText2.setText("0.00");
        	
    		TextView pullText = (TextView)findViewById(R.id.pull_to_refresh_text);
            pullText.setTypeface(chunk);
            pullText.setTextSize(25);
            pullText.setText("Pull to convert");
            
            PullToRefreshScrollView pullToRefreshView = (PullToRefreshScrollView) findViewById(R.id.converterScrollView1);
            pullToRefreshView.setBackgroundResource(R.drawable.refresh_background);
            pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

    			@Override
    			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
    				new loadStats().execute("");
    			}
            });
    	}
    
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
    	double multiplier;  //1 = POT  2 = BTC  3 = USD
    	double converted1;
    	double converted2;  
    	Typeface chunk = Typeface.createFromAsset(getAssets(),"fonts/Chunkfive.ttf");
    	@Override
        protected String doInBackground(String... params) {
        	if(firstRun==1){
        		//INITILZIE currency on first run.
            	priceInBTC=getMintpalStats().getLast_price();
                btcInUSD=getBitcoinPrice(); 
        	}
          
            firstRun=0;
            //GET multiplier from edittext.
            EditText multi_text =(EditText)findViewById(R.id.multiplyer);
            multiplier=Double.parseDouble(multi_text.getText().toString());
        	
            String highlowlast ="";
			return highlowlast;
            
        }
       

        @Override
        protected void onPostExecute(String result) {
        	
        	//POT
        	if(currency==1){
        		converted1=multiplier*priceInBTC;
    			TextView conText1 =(TextView)findViewById(R.id.converted_text_1);
    			conText1.setText(Double.toString(converted1));
    			conText1.setTypeface(chunk);
    			conText1.setTextColor(Color.parseColor("#f1f1f1"));
    			
    			DecimalFormat defusd = new DecimalFormat("#,###.000");
    			converted2=multiplier*(priceInBTC*btcInUSD);
    			TextView conText2 =(TextView)findViewById(R.id.converted_text_2);
    			conText2.setText(defusd.format(converted2));
    			conText2.setTypeface(chunk);
    			conText2.setTextColor(Color.parseColor("#d5c12b"));
        	}
        	//BTC
        	else if(currency==2){
        		
        	}
        	//USD
        	else if(currency==3){
        		
        	}
        	
            switch (currency){
			case 1:
				//converted1=multiplier*priceBTC;
				//TextView conText1 =(TextView)findViewById(R.id.converted_text_1);
				//conText1.setText(Double.toString(converted1));
				//output (multiplier*price);
				//GET USD
				//output (multiplier*(price * priceUsd))
				
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
            PullToRefreshScrollView pullToRefreshView = (PullToRefreshScrollView) findViewById(R.id.converterScrollView1);
            pullToRefreshView.onRefreshComplete();
        }
        @Override
        protected void onPreExecute() {
        	TextView pullText = (TextView)findViewById(R.id.pull_to_refresh_text);
        	pullText.setTextColor(Color.parseColor("#3aa748"));
        	
        	
        	
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

	

