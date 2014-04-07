package com.jok.potcoinfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.jok.potcoinfo.sources.BitcoinAverage;
import com.jok.potcoinfo.sources.Coinmarketcap;
import com.jok.potcoinfo.sources.Mintpal;
import com.jok.potcoinfo.sources.Network;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    int firstRun=1;
    public Vibrator v;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("");
        setContentView(R.layout.activity_main_menu);
        StrictMode.setThreadPolicy(policy);
        getActionBar().setDisplayUseLogoEnabled(true);
        getActionBar().setLogo(R.drawable.logo);
        Typeface chunk = Typeface.createFromAsset(getAssets(),"fonts/Chunkfive.ttf");
    	
    	TextView market_value = (TextView)findViewById(R.id.market_value);
        market_value.setTypeface(chunk);
        market_value.setText("Loading...");
        ImageView networkIcon = (ImageView)findViewById(R.id.network_logo);
        networkIcon.setImageAlpha(0);
        ImageView marketIcon = (ImageView)findViewById(R.id.market_logo);
        marketIcon.setImageAlpha(0);
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        
        //HEY LEE
        
        
        
        final TextView pullText = (TextView)findViewById(R.id.pull_to_refresh_text);
        pullText.setTypeface(chunk);
        pullText.setTextSize(25);
        PullToRefreshScrollView pullToRefreshView = (PullToRefreshScrollView) findViewById(R.id.scrollView1);
        pullToRefreshView.setBackgroundResource(R.drawable.refresh_background);
        pullToRefreshView.setRefreshing(true);
        pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				new loadStats().execute("");
				
			}
        });
        new loadStats().execute("");
    }
    
    public double getBitcoinPrice(){
    	InputStream source = retrieveStream("https://api.bitcoinaverage.com/ticker/global/USD/");
    	Reader reader = new InputStreamReader(source);
    	Gson gson = new Gson();
    	BitcoinAverage bitavg = gson.fromJson(reader, BitcoinAverage.class);
    	return bitavg.bitcoin_price;
    }
    
    public Coinmarketcap getCoinmarketcapStats(){
    	InputStream source = retrieveStream("http://coinmarketcap.northpole.ro/api/pot.json");
    	Reader reader = new InputStreamReader(source);
    	Gson gson = new Gson();
    	Coinmarketcap response = gson.fromJson(reader, Coinmarketcap.class);
    	return response;
    	
    }
    
    public Network getNetworkStats(){
    	InputStream source = retrieveStream("http://pot.leetpools.com/index.php?page=api&action=public");
    	Reader reader = new InputStreamReader(source);
    	Gson gson = new Gson();
    	Network response = gson.fromJson(reader, Network.class);
    	return response;
    	
    }
    
    public Mintpal getMintpalStats(){
    	InputStream source = retrieveStream("https://api.mintpal.com/market/stats/POT/BTC");
    	Reader reader = new InputStreamReader(source);
    	Gson gson = new Gson();
    	Mintpal pal[] =gson.fromJson(reader,Mintpal[].class);
    	return pal[0];
    	
    }
    	
    public List getBlockData(){
    	InputStream source = retrieveStream("http://potchain.aprikos.net/chain/Potcoin/q/nethash/-100000?format=json");
    	Reader reader = new InputStreamReader(source);
    	Gson gson = new Gson();
    	java.lang.reflect.Type type = new TypeToken<List<List>>(){}.getType();
        List<List> list =  gson.fromJson(reader, type);
        return  list.get(0);
    }
   
    public String getCoins(){
        	InputStream source = retrieveStream("http://potchain.aprikos.net/chain/Potcoin/q/totalbc");
        	Reader reader = new InputStreamReader(source);
        	Gson gson = new Gson();
        	String response = gson.fromJson(reader, String.class);
        	return response;
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    public void refresh(){
    	 new loadStats().execute("");
    }
    
   
    
    private class loadStats extends AsyncTask<String, Void, String> {
    	
    	String change;
    	String mktcap;
    	double price;
    	double priceUsd;
    	double hashrate;
    	String difficulty;
    	String blockNumber;
    	String position;
    	String coins;
        @Override
        protected String doInBackground(String... params) {
            price=getMintpalStats().getLast_price();
            priceUsd=getBitcoinPrice(); 
            change=getCoinmarketcapStats().hrChange;
            mktcap=getCoinmarketcapStats().marketCap;
            position=getCoinmarketcapStats().position;
            coins=getCoins();
            hashrate=getNetworkStats().getNetwork_hashrate();
            difficulty=getBlockData().get(4).toString();
            blockNumber=getBlockData().get(0).toString();
            
            String highlowlast ="Last $"+price+"\n"+"\n"+"High $"+change+"\n"+"\n"+"Low $"+mktcap;
			return highlowlast;
            
        }

        @Override
        protected void onPostExecute(String result) {
        	v.vibrate(45);
            Typeface chunk = Typeface.createFromAsset(getAssets(),"fonts/Chunkfive.ttf");
        	
        	TextView market_value = (TextView)findViewById(R.id.market_value);
            market_value.setTypeface(chunk);
            double toSatoshi = price*100000000;
            DecimalFormat satoshi = new DecimalFormat("####");
            market_value.setText(satoshi.format(toSatoshi)+" Satoshi");
            market_value.setTextColor(Color.parseColor("#3aa748"));
            
            TextView price_usd = (TextView)findViewById(R.id.price_usd);
            price_usd.setTypeface(chunk);
            DecimalFormat usd = new DecimalFormat("0.####");
            price_usd.setText("$"+usd.format(priceUsd*price));
            price_usd.setTextColor(Color.parseColor("#ba973a"));
            
            TextView market_change = (TextView)findViewById(R.id.market_change);
            market_change.setTypeface(chunk);
            market_change.setText("24Hr Change "+change);
            market_change.setTextColor(Color.parseColor("#f6f6f6"));
            
            TextView market_cap = (TextView)findViewById(R.id.market_cap);
            market_cap.setTypeface(chunk);
            market_cap.setText("MktCap $"+mktcap);
            market_cap.setTextColor(Color.parseColor("#f6f6f6"));
            
            TextView market_position = (TextView)findViewById(R.id.market_position);
            market_position.setTypeface(chunk);
            market_position.setText("Rank "+"#"+position);
            market_position.setTextColor(Color.parseColor("#f6f6f6"));
            
            
            TextView network_hashrate = (TextView)findViewById(R.id.network_hashrate);
            network_hashrate.setTypeface(chunk);
            double hashrateInGHZ = (hashrate/1000000000);
            DecimalFormat df = new DecimalFormat("#.00");
            network_hashrate.setText(df.format(hashrateInGHZ)+" Gh/s");
            network_hashrate.setTextColor(Color.parseColor("#2677af"));
            
            TextView difficultyTV = (TextView)findViewById(R.id.network_diff);
            difficultyTV.setTypeface(chunk);
            difficultyTV.setText("Diff "+difficulty);
            difficultyTV.setTextColor(Color.parseColor("#b73d3d"));
            
            TextView blocks = (TextView)findViewById(R.id.network_blocks);
            blocks.setTypeface(chunk);
            DecimalFormat def = new DecimalFormat("#,###");
            blockNumber = def.format(Double.parseDouble(blockNumber));
            blocks.setText("Blocks Found "+blockNumber);
            blocks.setTextColor(Color.parseColor("#f6f6f6"));
            
            
            TextView coinsTV = (TextView)findViewById(R.id.network_coins);
            coinsTV.setTypeface(chunk);
            coins = def.format(Double.parseDouble(coins));
            coinsTV.setText("Coins Mined "+coins);
            coinsTV.setTextColor(Color.parseColor("#f6f6f6"));
            
            ImageView networkIcon = (ImageView)findViewById(R.id.network_logo);
            networkIcon.setImageAlpha(1000);
            ImageView marketIcon = (ImageView)findViewById(R.id.market_logo);
            marketIcon.setImageAlpha(1000);
            
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
            TextView price_usd = (TextView)findViewById(R.id.price_usd);
            price_usd.setTextColor(Color.parseColor("#a6a6a6"));
            TextView network_hashrate = (TextView)findViewById(R.id.network_hashrate);
            network_hashrate.setTextColor(Color.parseColor("#a6a6a6"));
            TextView coinsTV = (TextView)findViewById(R.id.network_coins);
            coinsTV.setTextColor(Color.parseColor("#777777"));
            TextView blocks = (TextView)findViewById(R.id.network_blocks);
            blocks.setTextColor(Color.parseColor("#777777"));
            TextView difficultyTV = (TextView)findViewById(R.id.network_diff);
            difficultyTV.setTextColor(Color.parseColor("#a6a6a6"));
            TextView market_change = (TextView)findViewById(R.id.market_change);
            market_change.setTextColor(Color.parseColor("#777777"));
            TextView market_cap = (TextView)findViewById(R.id.market_cap);
            market_cap.setTextColor(Color.parseColor("#777777"));
            TextView market_position = (TextView)findViewById(R.id.market_position);
            market_position.setTextColor(Color.parseColor("#777777"));
            TextView pullText=(TextView)findViewById(R.id.pull_to_refresh_text);
            pullText.setTextColor(Color.parseColor("#3aa748"));
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
          	  startActivity(new Intent(MainActivity.this, ScannerActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    
}
