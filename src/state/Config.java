package state;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class Config
{
	public Config(JsonElement element)
	{
		this.handle(element, null);
	}

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
		public PlanDef(JsonElement element)
		{
			for (Entry<String, JsonElement> ele : ((JsonObject) element).entrySet())
			{
				if(ele.getKey().equals("block_time"))
				{
					this.block_time = ele.getValue().getAsInt();
				}
				else if(ele.getKey().equals("blocks_per_section"))
				{
					this.blocks_per_section = ele.getValue().getAsInt();

				}
				else if(ele.getKey().equals("ratios"))
				{
					this.ratios = handleIntArrayNest(ele.getValue());
				}
			}
		}

		public int block_time;
		public int blocks_per_section;
		public int[][] ratios;
	}

	public static Config read(Reader reader)
	{

		JsonElement ele = new JsonParser().parse(reader);
		return new Config(ele);
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

	public void handleObject(JsonElement element, String name)
	{
		if(name == null)
		{
			JsonObject obj = element.getAsJsonObject();

			for (Entry<String, JsonElement> ele : obj.entrySet())
			{
				handle(ele.getValue(), ele.getKey());
			}
			return;
			
		}
		if(name.equals("demo"))
		{
			this.demo = new PlanDef(element);
			return;
		}
		

		

	}

	public JsonElement handlePrimitive(JsonElement element,String name)
	{
		JsonPrimitive prim = element.getAsJsonPrimitive();
		
		if(name.equals("reward_bar_size"))
		{
			reward_bar_size = prim.getAsInt();
		}
		else if(name.equals("absolute_reward_probability"))
		{
			absolute_reward_probability = prim.getAsFloat();
		}
		else if(name.equals("reward_magnitude"))
		{
			reward_magnitude = prim.getAsString();
		}
		else if(name.equals("trials_per_minute"))
		{
			trials_per_minute = prim.getAsInt();
		}
		else if(name.equals("changeover_penalty"))
		{
			changeover_penalty = prim.getAsFloat();
		}
		
		return element;
	}

	public JsonElement handleArray(JsonElement element, String name)
	{
		JsonArray array = element.getAsJsonArray();
		if(name.equals("break_percentage_locations"))
		{
			ArrayList<Float> list = new ArrayList<Float>();
			int count = 0;
			for (JsonElement ele : array)
			{
				count++;
			}
			
			break_percentage_locations = new float[count];
			count=0;
			for (JsonElement ele : array)
			{
				break_percentage_locations[count]=ele.getAsFloat();
				count++;
			}

		}
		else if(name.equals("plan"))
		{
			ArrayList<PlanDef> defs = new ArrayList<PlanDef>();
			for(JsonElement ele : array)
			{
				defs.add(new PlanDef(ele));
			}
			
			this.plan = defs.toArray(new PlanDef[defs.size()]);
		}
		
		
		return array;
	}
	
	public int[][] handleIntArrayNest(JsonElement element)
	{
		ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
		JsonArray array = element.getAsJsonArray();

		for (JsonElement ele : array)
		{
			ArrayList<Integer> innerArray = new ArrayList<Integer>();
			for(JsonElement ele2 : ele.getAsJsonArray())
			{
				innerArray.add(ele2.getAsInt());
			}
			list.add(innerArray);
		}
		
		int[][] intArray= new int[list.size()][2];
		
		for(int i = 0; i < list.size(); i++)
		{
			for(int c = 0; c < 2; c++)
			{
				intArray[i][c] = list.get(i).get(c);
			}
		}
		
		return intArray;
	}

	public void handle(JsonElement element, String name)
	{
		if (element.isJsonArray())
		{
			this.handleArray(element,name);
		} 
		else if (element.isJsonObject())
		{
			this.handleObject(element,name);
		} 
		else if (element.isJsonPrimitive())
		{
			this.handlePrimitive(element, name);
		}
	}
}
