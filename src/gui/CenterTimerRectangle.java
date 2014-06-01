package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import state.State;
import state.State.TaskStage;

public class CenterTimerRectangle extends GuiShape
{
	Rectangle2D rect;
	Rectangle2D shape;
	public CenterTimerRectangle(Container container,Color color,int xPos, int yPos, int width, int height)
	{
		super(container ,color, xPos, yPos,width, height);
		this.shape = new Rectangle2D.Double(x1,y1,w1,h1);
		rect = new Rectangle2D.Double(x1,y1,w1,h1);
	}

	@Override
	public void draw(Graphics2D page)
	{
		Stroke oldStroke = page.getStroke();
		page.setStroke(new BasicStroke((float)w1/80F));
		rect.setRect(x1,y1,w1,h1);

		page.setColor(this.color);
		page.draw(rect);
		page.setStroke(oldStroke);
	}

	@Override
	public void predraw(Graphics2D page)
	{
		State state = State.getState();
		if(state.IsRunning())
		{
			page.setColor(getColor((state.choicePool/(state.choicePoolMax))));
			double fillHeight =  (h1*(state.choicePool/state.choicePoolMax));
			((Rectangle2D) this.shape).setRect(x1, (int) (y1+1 + h1-fillHeight),w1,(int)fillHeight);
			page.fill(this.shape);
		}
		else
		{
			if(state.getTaskStage() == TaskStage.TASK_BREAK)
			{
				page.setColor(this.color);
				page.setFont(new Font("TimesRoman", Font.BOLD, (int) (w1/3))); 
				page.drawString("Your break has started, press 'Start' when\n you are ready to continue", (int) (x1+w1/4), (int) (y1-h1/4));
			}
			else
			{
				page.setColor(this.color);
				page.setFont(new Font("TimesRoman", Font.ITALIC, (int) (w1/5))); 
				page.drawString("PAUSED", (int) (x1+w1/4), (int) (y1-h1/4));
			}
		
		}
		
		if(State.getState().getTaskStage()== TaskStage.TASK_DEMO)
		{
			page.setColor(this.color);
			page.setFont(new Font("TimesRoman", Font.BOLD, (int) (w1/1.2))); 
			page.drawString("DEMO", (int) (x1-w1/1.43), (int) ((y1+h1*1.2)));
		}
	}
	
	
	
	

	
	public Color getColor(double power)
	{
		return new Color((int) (255*power),0,0);
	}

	@Override
	public void postdraw(Graphics2D page)
	{
		// TODO Auto-generated method stub
		
	}

}
