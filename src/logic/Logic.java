package logic;

import gui.Gui;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingWorker;

import state.State;
import state.State.Selection;
import state.State.TaskStage;
import state.Statistics;
import utils.SoundHelper;

public class Logic implements Runnable
{
	private Random rand;

	public Logic()
	{
		this.rand = new Random();
	}

	public void run()
	{
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new AccurateTimer(), State.SIM_SPEED * 50, State.SIM_SPEED);
	}

	public boolean Finish()
	{
		if (!State.getState().IsRunning())
		{
			return false;
		}
		State.getState().setTaskStage(TaskStage.FINISHED);
		State.getState().printStatsToServer();
		return true;
	}

	public void tick()
	{
		State state = State.getState();

		if (state.IsRunning())
		{
			state.increaseTimeElapsed(State.SIM_SPEED);
			state.incrementTimeOnCurrentSection(State.SIM_SPEED);

			double growth = ((double) state.choicesPerMinute / 60000D) * State.SIM_SPEED;
			if (state.choicePool <= state.choicePoolMax)
			{
				state.choicePool += growth;
			}

			if (this.rand.nextDouble() <= state.getPlan().getFirst().getQRatio() * growth * state.absoluteRewardProbability)
			{
				state.setBait('q',true);
			}

			if (this.rand.nextDouble() <= state.getPlan().getFirst().getPRatio() * growth * state.absoluteRewardProbability)
			{
				state.setBait('p',true);
			}

			boolean success = false;
			
			if (state.choicePool >= state.choicePoolMax)
			{
				state.incrementTrialCount();
				Selection currentSelection = state.getCurrentSelection();
				int rewardStatus = 0;
				if(Selection.LEFT.isBaited())
				{
					rewardStatus++;
				}
				if(Selection.RIGHT.isBaited())
				{
					rewardStatus+=2;
				}
				if (state.getPrevSelection() == currentSelection || this.rand.nextFloat()>State.getState().getChangeoverPenalty()||state.getPrevSelection() == Selection.INVALID)
				{
					if (currentSelection.isBaited())
					{
						success = true;
						currentSelection.setBait(false);
					}
				}

				if (success)
				{
					state.incrementScore();

				}
				
				state.getStats().onTrial(currentSelection.numericalRep, (int)System.currentTimeMillis(), success, state.getPlan().getFirst(), state.getLastChoiceTime(),rewardStatus);
				state.choicePool--;
				state.setSelection(Selection.INVALID);
				this.shouldBreak();
				
				int duration = state.getPlan().getFirst().duration;
				int currentTaskTime = (int) (state.getTimeOnCurrentSection()/1000F);
				
				if (state.getPlan().getFirst().duration <= state.getTimeOnCurrentSection()/1000)
				{
					if(state.getTaskStage() == TaskStage.TASK_DEMO)
					{
						State.getState().reset();
						state.getPlan().removeFirst();
						state.setTaskStage(TaskStage.START);
					}
					
					state.incrementTimeOnCurrentSection(-state.getPlan().getFirst().duration);
					state.getPlan().removeFirst();
				}

				if (state.getPlan().isEmpty())
				{
					Finish();
				}
			}

		}
	}
	
	public void shouldBreak()
	{
		if(State.getState().getTaskStage() != TaskStage.TASK_DEMO)
		{
			float[] breaks = State.getState().breaks;
			for(int i = 0; i < breaks.length; i++)
			{
				
				double timeElapsedFrac =  (State.getState().getTimeElapsed()/((State.getState().totalPlanTime))/1000);
				if(timeElapsedFrac>breaks[i])
				{
					breaks[i]=2;
					State.getState().setTaskStage(TaskStage.TASK_BREAK);
				}
			}
		}
	}

	/**
	 * if(!this.isRunning) { return; } if(this.choicePool<1) { return; }
	 * 
	 * if(ch!='q'&&ch!='p') { return; }
	 * 
	 * boolean success = false;
	 * 
	 * if(ch=='q') { if(this.qChoicePool>=1) { success = true;
	 * this.qChoicePool-- ; } } else if(ch=='p') { if(this.pChoicePool>=1) {
	 * success = true; this.pChoicePool--; } }
	 * if(rand.nextDouble()>this.absoluteRewardProbability) { success=false; }
	 * if(success) { Gui.instance.targets.drawSuccess(ch); } stats.onTrial(ch,
	 * this.timeElapsed, success, this.plan.getFirst()); choicePool--;
	 **/

	public class AccurateTimer extends TimerTask
	{

		@Override
		public void run()
		{
			tick();
		}

	}
}
