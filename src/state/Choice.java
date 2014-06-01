package state;

public class Choice
{
	public static final String OUTPUT_KEY = "success, choice,trial start time,time of choice,reward status"+ ProbabilitySection.OUTPUT_KEY;
	public final int trialNumber;
	public final int pick;
	public final int success;
	public final int time;
	public final int timeOfChoice;
	public final ProbabilitySection plan;
	public final int rewardStatus;
		
	public Choice(int trialNumber, int pick, boolean success, int time,int timeofChoice, ProbabilitySection plan, int rewardStaus)
	{
		this.pick = pick; 
		
		this.trialNumber = trialNumber;
		this.timeOfChoice = timeofChoice;
			//this.success=success;
		if(success)
		{
			this.success = 1;
		}
		else
		{
			this.success = 0;
		}
		this.plan=plan;
		this.time=time;
		this.rewardStatus = rewardStaus;

	}
		
	public String toString()
	{
		return this.success+","+this.pick+","+time+","+timeOfChoice+","+plan.toString()+","+rewardStatus;
	}
}