package state;

import java.util.Random;

public class ProbabilitySection
{
	public static final String OUTPUT_KEY = "duration, Q reward rate, P reward rate";
	public final int duration;
	//0 is q, and 1 is p
	final int[] rewardRatio;

	public ProbabilitySection(int duration, int[] rewardRatios)
	{
		this.duration = duration;
		this.rewardRatio = rewardRatios;
	}
	
	/**
	 * Always assumes that the player clicked q, return inverse for p.
	 * @return
	 */
	public boolean Click(Random rand)
	{
		double ratio;
		if(this.rewardRatio[0]>this.rewardRatio[1])
		{
			ratio = (double)this.rewardRatio[1]/((double)this.rewardRatio[0]+(double)this.rewardRatio[1]);
			return !(rand.nextDouble()<=ratio);
		}
		ratio = (double)this.rewardRatio[0]/((double)this.rewardRatio[0]+(double)this.rewardRatio[1]);
		return (rand.nextDouble()<=ratio);
	}
	
	public double getQRatio()
	{
		double ratio;
		ratio = (double)this.rewardRatio[0]/((double)this.rewardRatio[0]+(double)this.rewardRatio[1]);
		return ratio;		
	}
	
	public double getPRatio()
	{
		return (double)this.rewardRatio[1]/((double)this.rewardRatio[0]+(double)this.rewardRatio[1]);
	}
	
	@Override
	public String toString()
	{
		return this.duration+","+getQRatio()*State.getState().absoluteRewardProbability+","+getPRatio()*State.getState().absoluteRewardProbability;
	}
}
