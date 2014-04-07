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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.jok.potcoinfo.intents.IntentIntegrator;
import com.jok.potcoinfo.intents.IntentResult;
import com.jok.potcoinfo.sources.BitcoinAverage;
import com.jok.potcoinfo.sources.Mintpal;
public class ScannerActivity extends Activity {
	public String ADDRESS;
	int firstRun=1;
   StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("");
        setContentView(R.layout.activity_scanner);
        StrictMode.setThreadPolicy(policy);
        getActionBar().setDisplayUseLogoEnabled(true);
        getActionBar().setLogo(R.drawable.logo);
        
        IntentIntegrator intentIntegrator = new IntentIntegrator(ScannerActivity.this);
		intentIntegrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);	
		
		Typeface chunk = Typeface.createFromAsset(getAssets(),"fonts/Chunkfive.ttf");
		final TextView pullText = (TextView)findViewById(R.id.pull_to_refresh_text);
        pullText.setTypeface(chunk);
        pullText.setTextSize(25);
        PullToRefreshScrollView pullToRefreshView = (PullToRefreshScrollView) findViewById(R.id.scrollView1);
        pullToRefreshView.setBackgroundResource(R.drawable.refresh_background);
        pullToRefreshView.setRefreshing(true);
        pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				new DownloadImageTask((ImageView) findViewById(R.id.imageView1))
                .execute("http://zxing.org/w/chart?cht=qr&chs=450x450&chld=L&choe=UTF-8&chl="+ADDRESS);
				new LoadAddress().execute("");
				
			}
        });
    
        
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap qrimage = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                qrimage = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return qrimage;
        }
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();   
        finish();

    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    		if(resultCode!=RESULT_CANCELED){
    			String contents = scanResult.getContents();
    			ADDRESS=contents;
    			new LoadAddress().execute("");
    			new DownloadImageTask((ImageView) findViewById(R.id.imageView1))
                .execute("http://zxing.org/w/chart?cht=qr&chs=450x450&chld=L&choe=UTF-8&chl="+ADDRESS);
    			}
    		else{
    			onBackPressed();
    		}
		}
    
    public String getAccountBalance(){
    	InputStream source = retrieveStream("http://potchain.aprikos.net/chain/Potcoin/q/addressbalance/"+ADDRESS);
    	Reader reader = new InputStreamReader(source);
    	Gson gson = new Gson();
    	String acctBalance = gson.fromJson(reader, String.class);
    	return acctBalance;
     }
    public double getBitcoinPrice(){
    	InputStream source = retrieveStream("https://api.bitcoinaverage.com/ticker/global/USD/");
    	Reader reader = new InputStreamReader(source);
    	Gson gson = new Gson();
    	BitcoinAverage bitavg = gson.fromJson(reader, BitcoinAverage.class);
    	return bitavg.bitcoin_price;
    	
     }
    public Mintpal getMintpalStats(){
    	InputStream source = retrieveStream("https://api.mintpal.com/market/stats/POT/BTC");
    	Reader reader = new InputStreamReader(source);
    	Gson gson = new Gson();
    	Mintpal pal[] =gson.fromJson(reader,Mintpal[].class);
    	return pal[0];
    	
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




    private class LoadAddress extends AsyncTask<String, Void, String> {
    	
    	String acctBalance;
    	String acctBalanceUSD;
    	String acctBalanceBTC;
    	double bitPrice;
    	double potPrice;
    	

        @Override
        protected String doInBackground(String... params) {
            String highlowlast ="";
            acctBalance = getAccountBalance();
            bitPrice=getBitcoinPrice();
            potPrice=getMintpalStats().getLast_price();
            acctBalanceUSD = Double.toString((Double.parseDouble(acctBalance)*(bitPrice*potPrice)));
            acctBalanceBTC = Double.toString(potPrice*Double.parseDouble(acctBalance));
            return highlowlast;
        }

        @Override
        protected void onPostExecute(String result) {
        	
        	ImageView qrimage = (ImageView)findViewById(R.id.imageView1);
        	qrimage.setAlpha(1000);
        	
            Typeface chunk = Typeface.createFromAsset(getAssets(),"fonts/Chunkfive.ttf");
            TextView scanner_value = (TextView)findViewById(R.id.address);
			scanner_value.setTypeface(chunk);
		    scanner_value.setText(ADDRESS);
		    scanner_value.setTextColor(Color.parseColor("#f1f1f1"));
		    
            TextView scanner_balance = (TextView)findViewById(R.id.scanner_balance);
			scanner_balance.setTypeface(chunk);
			DecimalFormat def = new DecimalFormat("#,###.########");
            acctBalance = def.format(Double.parseDouble(acctBalance));
		    scanner_balance.setText(acctBalance);
		    
		    TextView in_usd = (TextView)findViewById(R.id.in_usd);
		    in_usd.setTypeface(chunk);
		    DecimalFormat def2 = new DecimalFormat("#,###.#####");
            acctBalanceUSD = def2.format(Double.parseDouble(acctBalanceUSD));
		    in_usd.setText(acctBalanceUSD);
		    
		    TextView in_btc = (TextView)findViewById(R.id.in_btc);
		    in_btc.setTypeface(chunk);
		    DecimalFormat def3 = new DecimalFormat("#,###.#####");
            acctBalanceBTC = def3.format(Double.parseDouble(acctBalanceBTC));
		    in_btc.setText(acctBalanceBTC);
		    
		    ImageView leaf = (ImageView)findViewById(R.id.leaf);
		    leaf.setAlpha(1000);
		    ImageView dollar = (ImageView)findViewById(R.id.dollar);
		    dollar.setAlpha(1000);
		    ImageView btc = (ImageView)findViewById(R.id.btc);
		    btc.setAlpha(1000);
		    
		    
		    
		    TextView pullText = (TextView)findViewById(R.id.pull_to_refresh_text);
       	    pullText.setTextColor(Color.parseColor("#f1f1f1"));
            PullToRefreshScrollView pullToRefreshView = (PullToRefreshScrollView) findViewById(R.id.scrollView1);
            pullToRefreshView.onRefreshComplete();
        }
        @Override
        protected void onPreExecute() {
        	TextView pullText = (TextView)findViewById(R.id.pull_to_refresh_text);
        	pullText.setTextColor(Color.parseColor("#3aa748"));
        	Typeface chunk = Typeface.createFromAsset(getAssets(),"fonts/Chunkfive.ttf");
           if(firstRun==1){
        	TextView scanner_balance = (TextView)findViewById(R.id.scanner_balance);
        	scanner_balance.setTypeface(chunk);
        	scanner_balance.setTextSize(35);
   		    scanner_balance.setText("Loading...");
   		    firstRun=0;
           }else{
        	   
        	    TextView scanner_balance = (TextView)findViewById(R.id.scanner_balance);
        	    scanner_balance.setTextSize(29);
   		    	scanner_balance.setText("");
           }
		    
		    TextView in_btc = (TextView)findViewById(R.id.in_btc);
		    in_btc.setText("");
		    TextView in_usd = (TextView)findViewById(R.id.in_usd);
		    in_usd.setText("");
		    
		    ImageView leaf = (ImageView)findViewById(R.id.leaf);
		    leaf.setAlpha(0);
		    ImageView dollar = (ImageView)findViewById(R.id.dollar);
		    dollar.setAlpha(0);
		    ImageView btc = (ImageView)findViewById(R.id.btc);
		    btc.setAlpha(0);
		    
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    
    


    
}


}
    
   
   
 

