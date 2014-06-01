package state;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;

import logic.Logic;

public class Statistics
{
	public static final int PORT = 3332;
	public static final int BUFFER_SIZE = 100;

	private ProbabilitySection lastPlan;

	private int lastRateSwitchTime;

	private int lastChoiceTime;

	public Statistics()
	{
		subjectTrials = new LinkedList<Choice>();
	}

	LinkedList<Choice> subjectTrials;

	public LinkedList<Choice> getChoices()
	{
		return this.subjectTrials;
	}
	
	public Choice getLastChoice()
	{
		return this.subjectTrials.isEmpty()?null:this.subjectTrials.getLast();
	}

	public void onTrial(int pick, int time, boolean success, ProbabilitySection plan, int lastChoiceTime, int rewardStatus)
	{
		Choice prevChoice = subjectTrials.isEmpty() ? new Choice(0, pick, success, 0, 0, plan, 0) : subjectTrials.getLast();
		int trialNumber = prevChoice.trialNumber + 1;

		// todo- calulate duration of each trial
		this.subjectTrials.addLast(new Choice(trialNumber, pick, success, (int) ((time-State.getState().startTime))-(60000/State.getState().choicesPerMinute),
				(int) (lastChoiceTime-State.getState().startTime), plan, rewardStatus));
	}

	public void onPlanSwitch(int time, ProbabilitySection plan)
	{
		if (this.lastPlan != null && plan.duration != this.lastPlan.duration)
		{
			this.lastRateSwitchTime = time;
		}

		this.lastPlan = plan;
	}

	public void printStats(PrintWriter writer)
	{
		writer.println(State.getState().sessionID);
		// writer.println(Choice.OUTPUT_KEY);
		for (Choice choice : this.subjectTrials)
		{
			writer.println(choice.toString());
		}
		writer.close();

	}

	public void printStatsToFile(File jarFile)
	{
		try
		{
			PrintWriter writer = new PrintWriter(jarFile);
			for (Choice choice : this.subjectTrials)
			{
				writer.println(choice.toString());
			}
			writer.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void printStatsToServer()
	{
		try
		{
			Socket echoSocket = new Socket("129.170.18.40", 3332);
			PrintWriter writer = new PrintWriter(echoSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			this.printStats(writer);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public char getPreviousSelection()
	{
		if (this.subjectTrials.isEmpty())
		{
			return 'x';
		}
		int numChoice = this.subjectTrials.getLast().pick;
		if (numChoice == 1)
		{
			return 'q';
		} else if (numChoice == 2)
		{
			return 'p';
		}
		return 'x';
	}
}
