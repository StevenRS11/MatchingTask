package utils;

import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JApplet;

import state.State;

public class SoundHelper
{
	private static AudioClip winSound;
	private static AudioClip looseSound;
	private static AudioClip moneySound;
	
	public void loadSounds(JApplet applet)
	{
		try
		{
			winSound = applet.getAudioClip(new URL(State.BASE_URL+"/sounds/"),"winSound.wav");
			looseSound = applet.getAudioClip(new URL(State.BASE_URL+"/sounds/"),"looseSound.wav");
			moneySound = applet.getAudioClip(new URL(State.BASE_URL+"/sounds/"),"moneySound.wav");

			
		} catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void playWinSound()
	{
		winSound.play();
	}
	public static void playLooseSound()
	{
		looseSound.play();
	}
	public static void playMoneySound()
	{
		moneySound.play();
	}
}
