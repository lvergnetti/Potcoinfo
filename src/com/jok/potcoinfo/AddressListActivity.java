package com.jok.potcoinfo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jok.potcoinfo.sources.Address;

public class AddressListActivity extends Activity{
	
	/*
	 * JOK!!
	 * need layout for this in setLayout();
	 * should be a list , and then 2 buttons - one to scan , and one to type
	 * 
	 * 
	 */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout();
        Address[] addressList = getAddressList();
        for(int i=0;i<addressList.length;i++){
        	Address address = addressList[i];
        	addItemToListView(address);
        }
     }
	
    public void setLayout(){
    	//addLayout Needs LIST, SCAN BUTTON, and TYPE BUTTON
    	//
	}
	
	public Address[] getAddressList(){
		Address[] addressList=null;
		SharedPreferences prefs = this.getSharedPreferences("AddressList", Context.MODE_PRIVATE);
		String storedList = prefs.getString("StoredList", "None");
		if (storedList!="None"){
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
