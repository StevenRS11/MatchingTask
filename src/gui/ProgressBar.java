package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import state.State;
import utils.SoundHelper;

public class ProgressBar extends GuiShape
{
	private Rectangle2D reward;
	private Rectangle2D shape;

	private int maxBars;
	private int animTime = 0;
	private int maxAnimTime = 200;
	
	Color goldColor = new Color(255,230,60,255);
	

	public ProgressBar(Container container,Color color,int xPos, int yPos, int width, int height)
	{
		super(container ,color, xPos, yPos,width, height);
		reward = new Rectangle2D.Double(x1,y1,w,h1);
		shape = new Rectangle2D.Double();
		this.maxBars = State.getState().getRewardBarSize();

	}

	@Override
	public void draw(Graphics2D page)
	{

		page.setColor(this.color);
		((Graphics2D)page).setStroke(new BasicStroke((float)w1/80F));
		this.reward.setFrame(x1, y1, w1, h1);
		this.shape.setFrame(x1, y1, w1, h1);
		page.draw(shape);

	}

	@Override
	public void predraw(Graphics2D page)
	{
		page.setFont(new Font("TimesRoman", Font.ITALIC, (int) (w1/5))); 
		double step = w1/maxBars;
		int currentFill = State.getState().getCurrentScore()%(maxBars);
		
		
		page.setColor(goldColor);

		if(currentFill == maxBars-1)
		{

			if(this.animTime<this.maxAnimTime)
			{

				page.drawString("+25c", (int) (x1+w1/4), (int) (y1-h1/4));
				if(this.animTime==0)
				{
					SoundHelper.playMoneySound();
				}
			}
			else
			{
				page.setColor(Color.white);
			}
			
			this.animTime++;
		}
		else
		{
			this.animTime = 0;
		}
		page.fill(new Rectangle2D.Double(x1,y1,(int) (step * (currentFill+1)),h1));	

	}



	@Override
	public void postdraw(Graphics2D page)
	{
		// TODO Auto-generated method stub
		
	}

}
