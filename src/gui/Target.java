package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

import state.Choice;
import state.State;
import state.State.Selection;
import utils.SoundHelper;

public class Target extends GuiShape
{
	Ellipse2D rewardOutline;
	Ellipse2D outline;
	Ellipse2D shape;
	
	public final Selection targetLetter;
	int animationTime = 0;
	int trailAnimation;
	double animationMax = 40;

	public Target(Container container,Color color,int xPos, int yPos, int width, int height, Selection target)
	{
		super(container ,color, xPos, yPos,width, height);
		shape = new Ellipse2D.Double(x1,y1,(w1+h1)/2,(w1+h1)/2);
		outline = new Ellipse2D.Double(x1,y1,(w1+h1)/2,(w1+h1)/2);
		rewardOutline = new Ellipse2D.Double(x1,y1,(w1+h1)/2,(w1+h1)/2);
		this.targetLetter = target;

	}

	@Override
	public void draw(Graphics2D page)
	{
		Font font = new Font("serif", Font.BOLD, (int) (w1/2));
		page.setColor(this.color);
		((Ellipse2D) shape).setFrame(x1,y1,(w1+h1)/2,(w1+h1)/2);
		page.fill(this.shape);

		if(State.getState().getCurrentSelection() == targetLetter)
		{
			double outlineOffset = ((w1+h1)/2)/7;
			page.setColor(Color.BLACK);
			Stroke oldStroke = page.getStroke();
			page.setStroke(new BasicStroke((float)w1/15F));
			rewardOutline.setFrameFromDiagonal(shape.getMinX()-outlineOffset, shape.getMinY()-outlineOffset, shape.getMaxX()+outlineOffset, shape.getMaxY()+outlineOffset);
			page.draw(rewardOutline);
			page.setStroke(oldStroke);
		}
	

		
	}

	@Override
	public void postdraw(Graphics2D page)
	{
		
	
	}

	@Override
	public void predraw(Graphics2D page)
	{
		if(State.getState().getPrevSelection() == this.targetLetter && State.getState().getCurrentTrial() != this.trailAnimation)
		{
			this.trailAnimation = State.getState().getCurrentTrial();
			this.animationTime = 0;
		}
		
		this.animate(page);

	}
	
	public void animate(Graphics2D page)
	{
		if(this.animationTime<this.animationMax)
		{
			this.animationTime++;
			double outlineOffset = ((w1+h1)/2)/7;
			double multi = 1;
			Choice choice = State.getState().getStats().getLastChoice();
			
			if(choice!=null&&choice.pick==this.targetLetter.numericalRep&&choice.success == 1)
			{		
				multi = (((this.animationTime/ this.animationMax)));
				if(this.animationTime==1)
				{
					SoundHelper.playWinSound();
				}
			}
			else 
			{
				multi=((-(this.animationTime/ this.animationMax)));
				if(this.animationTime==1)
				{
					SoundHelper.playLooseSound();
				}
			}
			
			Stroke oldStroke = page.getStroke();
			page.setStroke(new BasicStroke((float) (w1/15F+multi*outlineOffset/2)));
			outlineOffset+=multi*outlineOffset;
			page.setColor(this.getColor(multi));
			rewardOutline.setFrameFromDiagonal(shape.getMinX()-outlineOffset, shape.getMinY()-outlineOffset, shape.getMaxX()+outlineOffset, shape.getMaxY()+outlineOffset);
			page.draw(rewardOutline);
			page.setStroke(oldStroke);
		
		}
	}
	
	public Color getColor(double multi)
	{
		boolean win = multi >=0;
		multi = Math.abs(multi);
		int[] goldColor ={255,230,60,255};
		
		if(win)
		{
			return new Color((int) (multi*goldColor[0]),(int) (multi*goldColor[1]),(int) (multi*goldColor[2]),(int) (255-Math.log(multi*goldColor[3])));
		}
		return new Color(0,0,0,(int) (255-multi*goldColor[3]));

	}


}
