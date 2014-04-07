package com.jok.potcoinfo.sources;

import com.google.gson.annotations.SerializedName;


public class Coinmarketcap {
	
	public String position;
	
	public String marketCap;
	
	@SerializedName("change24")
	public String hrChange;
	
	public double price;
		

}
