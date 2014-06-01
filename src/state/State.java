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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import state.State.Selection;

import logic.Logic;

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
	public final int demoLength = 30000;
	public long startTime;
	public double absoluteRewardProbability;
	public int choicesPerMinute;

	private LinkedList<state.ProbabilitySection> plan;

	private Statistics stats;

	private int trialNumber = 0;
	public double choicePool = 0;
	private int timeElapsed = 0;
	private double timeOnCurrentSection = 0;
	private int currentScore;
	private Selection selection = Selection.INVALID;
	private int lastChoiceTime;
	private int rewardBarSize;
	public ArrayList<Integer> breaks;

	private TaskStage currentStage = TaskStage.LICENSE;

	public void reset()
	{
		initFromURL(new Random(System.nanoTime()));
		this.initStats();
		this.trialNumber = 0;
		this.choicePool= 0;
		this.timeElapsed = 0;
		this.timeOnCurrentSection = 0;
		this.currentScore = 0;
		this.lastChoiceTime = 0;
		this.startTime = System.currentTimeMillis();
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

	public void resetTrialCount()
	{
		this.trialNumber = 0;
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

	public void resetTimeElapsed()
	{
		this.timeElapsed = 0;
	}

	public void resetStatistics()
	{
		this.stats = new Statistics();
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

	public void resetTimeOnCurrentSection()
	{
		this.timeOnCurrentSection = 0;
	}

	public int getCurrentScore()
	{
		return this.currentScore;
	}

	public void incrementScore()
	{
		this.currentScore++;
	}

	public void resetScore()
	{
		this.currentScore = 0;
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
		double absoluteRewardProb = 0;
		int trialsPerMin = 0;
		LinkedList<ProbabilitySection> planOutline = new LinkedList<ProbabilitySection>();
		String[] parsedString;
		ArrayList<Integer> breaks = new ArrayList<Integer>();

		try
		{
			String line = "start";
			line = br.readLine();

			while (line != null)
			{

				if (!line.startsWith("*"))
				{
					parsedString = line.split(":");
					if (parsedString[0].equals("Absolute reward probability"))
					{
						absoluteRewardProb = Double.parseDouble(parsedString[1]);

					} else if (parsedString[0].equals("Trials per minute"))
					{
						trialsPerMin = Integer.parseInt(parsedString[1]);
					} else if (parsedString[0].equals("Reward Bar Size"))
					{
						rewardBarSize = Integer.parseInt(parsedString[1]);
					} else if (parsedString[0].equals("Break Percentage Locations"))
					{
						for (int i = 1; i < parsedString.length; i++)
						{
							breaks.add(Integer.parseInt(parsedString[i]));
						}
						this.breaks = breaks;
					} else if (parsedString[0].equals("plan"))
					{
						parsedString = parsedString[1].split(",");

						int fragTime = Integer.parseInt(parsedString[0]) * 1000;
						int totalTime = Integer.parseInt(parsedString[1]) * 1000;

						int[][] ratios = new int[parsedString.length - 2][2];
						int count = 0;

						for (String string : parsedString)
						{
							if (string.startsWith("{"))
							{
								string = string.replace("{", "");
								string = string.replace("}", "");

								String[] ints = string.split(";");

								ratios[count][0] = Integer.parseInt(ints[0]);
								ratios[count][1] = Integer.parseInt(ints[1]);
								count++;

							}
						}

						BuildProbabilityPlan(fragTime, totalTime, ratios, planOutline, rand);
					}
				}
				line = br.readLine();
			}

			br.close();

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.absoluteRewardProbability = absoluteRewardProb;
		this.choicesPerMinute = trialsPerMin;
		this.plan = planOutline;
		long totalTime = 0;
		for (ProbabilitySection section : plan)
		{
			totalTime += section.duration;
		}
		this.totalPlanTime = totalTime;
	}

	/**
	 * generates a randomized list of probability sections, where adjacent ones
	 * switch.
	 * 
	 * @param sectionsDuration
	 * @param totalDuration
	 * @param sectionsRatios
	 * @param plan
	 * @return
	 */
	public LinkedList<ProbabilitySection> BuildProbabilityPlan(int sectionsDuration, int totalDuration, int[][] sectionsRatios, LinkedList<ProbabilitySection> plan, Random rand)
	{
		int uniqueRatios = 0;
		for (int[] ratio : sectionsRatios)
		{
			if (ratio[0] != ratio[1])
			{
				uniqueRatios++;
			}
			uniqueRatios++;
		}
		int[][] randomBalancedRatios = new int[uniqueRatios][];
		LinkedList<Integer> intPool = new LinkedList<Integer>();
		LinkedList<int[]> ratioPool = new LinkedList<int[]>();
		LinkedList<ProbabilitySection> planSection = new LinkedList<ProbabilitySection>();

		for (int i = 0; i < uniqueRatios; i++)
		{
			intPool.add(i);
		}

		for (int[] ratio : sectionsRatios)
		{
			randomBalancedRatios[(intPool.remove(rand.nextInt(intPool.size())))] = (ratio);
			if (ratio[0] != ratio[1])
			{
				randomBalancedRatios[(intPool.remove(rand.nextInt(intPool.size())))] = (new int[] { ratio[1], ratio[0] });
			}
		}

		while (totalDuration > 0)
		{
			if (ratioPool.isEmpty())
			{
				for (int i = 0; i < randomBalancedRatios.length; i++)
				{
					ratioPool.addLast(randomBalancedRatios[i]);
				}
			}

			int[] ratio = ratioPool.getFirst();

			if (plan.isEmpty() || this.validateRatioDifference(planSection.isEmpty() ? plan.getLast().rewardRatio : planSection.getLast().rewardRatio, ratio))
			{
				totalDuration -= sectionsDuration;
				ratioPool.remove(ratio);
				planSection.addLast(new ProbabilitySection(sectionsDuration, ratio));
				if (plan.isEmpty())
				{
					plan.addLast(planSection.poll());
				}
			} else if (rand.nextBoolean() && !planSection.isEmpty())
			{
				planSection.addLast(planSection.removeFirst());
			} else if (!ratioPool.isEmpty())
			{
				ratioPool.addLast(ratioPool.removeFirst());
			}
		}
		plan.addAll(planSection);
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
		return true;
	}
}
