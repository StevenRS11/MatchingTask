package state;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import logic.Logic;
import state.Config.PlanDef;

public class State
{
	public enum TaskStage
	{
		LICENSE, TUTORIAL, TASK_DEMO, START, TASK_RUNNING, FINISHED, TASK_BREAK;

		private boolean paused = true;

		public void setPaused(boolean bol)
		{
			this.paused = bol;
		}

		public boolean paused()
		{
			return this.paused;
		}
	}

	public enum Selection
	{
		LEFT("fq", 1), RIGHT("pj", 2), INVALID("x", 0);

		Selection(String validChars, int numericalRep)
		{
			this.validChars = validChars;
			this.numericalRep = numericalRep;
		}

		public static Selection getSelectionForChar(char ch)
		{
			for (Selection sel : Selection.values())
			{
				for (Character compCh : sel.validChars.toCharArray())
				{
					if (ch == compCh)
					{
						return sel;
					}
				}
			}
			return INVALID;
		}

		public final String validChars;
		public final int numericalRep;
		private volatile boolean baited;

		public synchronized boolean isBaited()
		{
			return this.baited && this != INVALID;
		}

		public synchronized void setBait(boolean flag)
		{
			this.baited = flag && this != INVALID;
		}
	}

	public static final int sessionID = new Random(System.nanoTime()).nextInt(Integer.MAX_VALUE);
	public double totalPlanTime;
	public static volatile State instance;

	public static synchronized State getState()
	{
		return instance;
	}

	public State()
	{
		State.instance = this;
		this.stats = new Statistics();
		this.startTime = System.currentTimeMillis();
		
	}

	public static final int SIM_SPEED = 20;
	public static final int SUCCESS_DRAWING_TIME = 30;
	public static final String BASE_URL = "http://perelandra.dartmouth.edu/~stafford";

	public int choicePoolMax = 1;
	public long startTime;
	public double absoluteRewardProbability;
	public int choicesPerMinute;

	private LinkedList<state.ProbabilitySection> plan = new LinkedList<state.ProbabilitySection>();

	private Statistics stats;

	private int trialNumber = 0;
	public double choicePool = 0;
	private int timeElapsed = 0;
	private double timeOnCurrentSection = 0;
	private int currentScore;
	private Selection selection = Selection.INVALID;
	private int lastChoiceTime;
	private int rewardBarSize;
	public float[] breaks;
	private float changeOverPenalty;
	private String rewardMagnitude;
	private TaskStage currentStage = TaskStage.LICENSE;

	public void reset()
	{
		this.trialNumber = 0;
		this.choicePool= 0;
		this.timeElapsed = 0;
		this.timeOnCurrentSection = 0;
		this.currentScore = 0;
		this.lastChoiceTime = 0;
		this.totalPlanTime = 0;
		this.startTime = System.currentTimeMillis();
		initFromURL(new Random(System.nanoTime()));
		this.initStats();
	}
	public float getChangeoverPenalty()
	{
		return this.changeOverPenalty;
	}
	public Statistics getStats()
	{
		return this.stats;
	}

	public void initStats()
	{
		this.stats = new Statistics();
	}

	public TaskStage getTaskStage()
	{
		return this.currentStage;
	}

	public void setTaskStage(TaskStage stage)
	{
		this.currentStage = stage;
	}

	public int getRewardBarSize()
	{
		return this.rewardBarSize;
	}

	public int getCurrentTrial()
	{
		return this.trialNumber;
	}

	public void incrementTrialCount()
	{
		this.trialNumber++;
	}

	public int getLastChoiceTime()
	{
		return this.lastChoiceTime;
	}

	public void setBait(char ch, boolean bait)
	{
		Selection.getSelectionForChar(ch).setBait(bait);
	}

	public Selection getPrevSelection()
	{
		return Selection.getSelectionForChar(this.stats.getPreviousSelection());
	}

	public boolean IsRunning()
	{
		return (this.currentStage == TaskStage.TASK_RUNNING || this.currentStage == TaskStage.TASK_DEMO) && !this.currentStage.paused();
	}

	public void printStatsToFile(File file)
	{
		this.stats.printStatsToFile(file);
	}

	public void printStatsToServer()
	{
		this.stats.printStatsToServer();
	}

	public void increaseTimeElapsed(int time)
	{
		this.timeElapsed += time;
	}

	public LinkedList<ProbabilitySection> getPlan()
	{
		return this.plan;
	}

	/**
	 * returns time elapsed in milliseconds
	 * @return
	 */
	public int getTimeElapsed()
	{
		return this.timeElapsed;
	}

	public double getTimeOnCurrentSection()
	{
		return this.timeOnCurrentSection;
	}

	public double incrementTimeOnCurrentSection(int time)
	{
		return this.timeOnCurrentSection += time;
	}

	public int getCurrentScore()
	{
		return this.currentScore;
	}

	public void incrementScore()
	{
		this.currentScore++;
	}

	public Selection getCurrentSelection()
	{
		return this.selection;
	}

	public void setSelection(Selection ch)
	{
		this.selection = ch;
	}

	public void select(Selection ch, int time)
	{
		this.selection = ch;
		this.lastChoiceTime = time;
	}

	public void initFromURL(Random rand)
	{
		String address = BASE_URL + "/config.txt";
		URL url = null;
		InputStream stream = null;

		try
		{
			url = new URL(address);
		} catch (MalformedURLException e)
		{
			e.printStackTrace();
		}

		try
		{
			stream = url.openStream();
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		InputStreamReader reader = new InputStreamReader(stream);

		BufferedReader br = new BufferedReader(reader);
		initState(br, rand);
	}

	public void initFromConfig(Random rand)
	{
		BufferedReader br = null;
		File jarFile = null;
		try
		{
			jarFile = new File(Logic.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
		} catch (URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			br = new BufferedReader(new FileReader(jarFile.getAbsolutePath() + "/config.txt"));
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initState(br, rand);

	}

	public void initState(BufferedReader br, Random rand)
	{
		plan = new LinkedList<ProbabilitySection>();
		Config config = null;
		try
		{

			config = Config.read(br);
			BuildProbabilityPlan(config.demo.block_time, config.demo.blocks_per_section, config.demo.ratios, plan, rand);

			for(int i = 0; i < config.plan.length; i++)
			{	
				PlanDef planDef = config.plan[i];
				BuildProbabilityPlan(planDef.block_time, planDef.blocks_per_section, planDef.ratios, plan, rand);
				this.totalPlanTime += planDef.block_time*planDef.blocks_per_section;

			}
			br.close();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.rewardMagnitude=config.reward_magnitude;
		this.rewardBarSize = config.reward_bar_size;
		this.absoluteRewardProbability = config.absolute_reward_probability;
		this.breaks = config.break_percentage_locations;
		this.choicesPerMinute = config.trials_per_minute;
		this.changeOverPenalty = config.changeover_penalty;
	}

	/**
	 * generates a randomized list of probability sections, where adjacent ones
	 * switch.
	 * 
	 * @param blockDuration
	 * @param blocksPerSection
	 * @param sectionsRatios
	 * @param plan
	 * @return
	 */
	public LinkedList<ProbabilitySection> BuildProbabilityPlan(int blockDuration, int blocksPerSection, int[][] sectionsRatios, LinkedList<ProbabilitySection> plan, Random rand)
	{

		ArrayList<int[]> ratios = new ArrayList<int[]>();
		for (int i = 0; ratios.size() < blocksPerSection; i++)
		{
			int[] ratio = sectionsRatios[i%sectionsRatios.length];
			if (ratio[0] != ratio[1])
			{
				ratios.add(new int[]{ratio[1],ratio[0]});
			}
			ratios.add(ratio);
		}
		
		int[][] planRatios = new int[blocksPerSection][2];
		
		int i = 0;
		planRatios[i] = ratios.get(rand.nextInt(ratios.size()));
		ratios.remove(planRatios[i]);
		i++;
		
		while(!ratios.isEmpty())
		{
			int[] canidate = ratios.get(rand.nextInt(ratios.size()));
			if(validateRatioDifference(planRatios[i-1],canidate))
			{
				planRatios[i] = canidate;
				ratios.remove(canidate);
				i++;
			}
			else
			{
				int count = rand.nextInt(i);

				while(count<i && i != 0)
				{
					int topIndex = count;
					int bottomIndex = (count+1);
					
					int[] topRatio = planRatios[topIndex];
					int[] bottomRatio = planRatios[bottomIndex];

					if(validateRatioDifference(topRatio,canidate)&&validateRatioDifference(bottomRatio,canidate))
					{
						int[][] top = Arrays.copyOfRange(planRatios, 0, topIndex+1);
						int[][] bottom =  Arrays.copyOfRange(planRatios, bottomIndex,planRatios.length);
						
						planRatios[topIndex+1] = canidate;
						System.arraycopy(bottom, 0, planRatios, bottomIndex+1, bottom.length-1);
						ratios.remove(canidate);
						i++;
						break;

					}
					count = (count+1);
				}
			}
		}
		
		for (int index = 0; index < blocksPerSection; index++)
		{
			plan.add(new ProbabilitySection(blockDuration, planRatios[index]));
		}
		
		return plan;
	}

	public boolean validateRatioDifference(int[] ratio1, int[] ratio2)
	{
		if ((ratio1[0] - ratio1[1]) * (ratio2[0] - ratio2[1]) > 0)
		{
			return false;
		}
		if ((ratio1[0] - ratio1[1]) == (ratio2[0] - ratio2[1]))
		{
			return false;
		}
		if(ratio1[1]==0||ratio1[0]==0||ratio2[1]==0||ratio2[0]==0)
		{
			return false;
		}
		return true;
	}

	public String getRewardMagnitude()
	{
		return rewardMagnitude;
	}
}
