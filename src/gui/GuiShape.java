package gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.ArrayList;

import state.State;

public abstract class GuiShape
{
	public static final int STEP = 100;

	public GuiShape(Container container,Color color, int xPos, int yPos,int width, int height)
	{
		h = height;
		w = width;
		x = xPos;
		y = yPos;
		this.container = container;
		this.color = color;
		this.updatePos();

		
	}
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -599730022628198031L;
	
	double h,w,x,y;
	double x1, y1, h1, w1;
	Container container;
	
	
	Color color;
	
	public abstract void predraw(Graphics2D page);
	public abstract void draw(Graphics2D page);
	public abstract void postdraw(Graphics2D page);
	
	public void render(Graphics2D page)
	{
		updatePos();
		predraw(page);
		draw(page);
		postdraw(page);


	}
	
	public void updatePos()
	{
		int stepX =  (this.container.getWidth()/STEP);
		int stepY =  (this.container.getHeight()/STEP);
		//int step = (stepY+stepX)/2;
		
		
		x1 =  ((x)-(w/2));
		y1 = ((y)-(h/2));
		w1=  (w);
		h1 =  (h);
		
		x1 =   (x1*stepX);
		y1 =  (y1*stepY);
		h1 =  (h1*stepY);
		w1 =  (w1*stepX);

	}
	
		
}
