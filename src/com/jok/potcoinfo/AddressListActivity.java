package com.jok.potcoinfo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jok.potcoinfo.sources.Address;

public class AddressListActivity extends Activity{
	boolean previousSave;
	ListView listView;
	/*
	 * JOK!!
	 * need layout for this in setLayout();
	 * should be a list , and then 2 buttons - one to scan , and one to type
	 * 
	 * 
	 */
	   StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setLayout();
       // makePrefs();
        makeListView(getAddressList());
        
     }
	
    public void setLayout(){
    	setContentView(R.layout.activity_address_list);
    	//addLayout Needs LIST, SCAN BUTTON, and TYPE BUTTON
    	//
	}
	public void makePrefs(){
		SharedPreferences prefs = getSharedPreferences("AddressList", Context.MODE_PRIVATE);
    	Editor e = prefs.edit();
    	Address address = new Address();
    	address.address = "LEOSADDRESSDOOD";
    	address.name = "LEOSNAMEDioOOD";
    	Address[] addresses = new Address[1];
    	System.out.println("address array made - empty");
    	for(int i=0; i < addresses.length; i++){
    		addresses[i] = address; 
    		System.out.println("address array populated at " + i);
    	}
    	
    	Gson gson = new Gson();
    	String addressJson = gson.toJson(addresses);
    	System.out.println("addressJson" + addressJson);
    	e.putString("StoredList", addressJson);
    	e.commit();
    	System.out.println("Preference saved");
	}
	public Address[] getAddressList(){
		Address[] addressList={};
		SharedPreferences prefs = getSharedPreferences("AddressList", Context.MODE_PRIVATE);
		String storedList = prefs.getString("StoredList", null);
		if (storedList!=null){
			GsonBuilder gsonBuilder = new GsonBuilder();
			Gson gson = gsonBuilder.create();
			addressList = gson.fromJson(storedList, Address[].class);
			
		}
		
		return addressList;
	}
	
	public void makeListView(Address[] addresses){
		String[] stringAddresses = new String[addresses.length];
		String[] stringNames = new String[addresses.length];
		for(int i=0;i<addresses.length;i++){
			stringAddresses[i] = addresses[i].address;
			stringNames[i] = addresses[i].name;
		}
		listView = (ListView) findViewById(R.id.list);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, android.R.id.text1, stringAddresses);
        listView.setAdapter(adapter);
		
	}
	
	

	
}
