package com.jok.potcoinfo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
        setContentView(R.layout.activity_address_list);
        listView = (ListView) findViewById(R.id.list);
        setLayout();
        SharedPreferences prefs = this.getSharedPreferences("AddressList",Context.MODE_PRIVATE);
        String storedList = prefs.getString("StoredList", null);
        String[] addresses = {};
        if (storedList!=null){
	        Address[] addressList = getAddressList();
	        for(int i=0;i<addressList.length;i++){
	        	Address address = addressList[i];
	        	
	        	addresses[i] = address.address;
	        }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
              android.R.layout.simple_list_item_1, android.R.id.text1, addresses);
            listView.setAdapter(adapter);
            
            
	        
        }
        else {
        	//Do Nothing
        }
     }
	
    public void setLayout(){
    	//addLayout Needs LIST, SCAN BUTTON, and TYPE BUTTON
    	//
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
	
	public void addItemToListView(Address address){
		String name;
		name = address.name;
		
		
	}
	
	

	
}
