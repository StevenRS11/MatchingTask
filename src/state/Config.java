package state;

import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;


public class Config 
{
	public String reward_magnitude;
	public int reward_bar_size;
	public float absolute_reward_probability;
	public int trials_per_minute;
	public float changeover_penalty;
	
	public float[] break_percentage_locations;
	public PlanDef[] plan;
	public PlanDef demo;
	
	public class PlanDef
	{
		public int block_time;
		public int blocks_per_section;
		public int[][] ratios;
	}
	
	public static Config read(Reader reader)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.serializeNulls();
		Gson gson = builder.create();
		return gson.fromJson(reader, Config.class);
	}
	
	public String write()
	{
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.serializeNulls();
		Gson gson = builder.create();

		JsonElement ele = gson.toJsonTree(this);
		
		return gson.toJson(ele);
	}
}
