package gui;



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.Timer;

import state.Choice;
import state.State;
import state.State.Selection;
import state.State.TaskStage;
import utils.SoundHelper;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Random;


import logic.Logic;
import logic.MatchingTask;


public class Gui extends JApplet implements ActionListener
{
	private static final long serialVersionUID = 1L;
	//the time per tick

	private final int APPLET_WIDTH = 1200, APPLET_HEIGHT = 760;

	private final Dimension size = new Dimension(1200,760);
	public LinkedList<GuiShape> shapes = new LinkedList<GuiShape>();

	private Image offImage;
	private Graphics offGraphics;
	private SoundHelper soundHelper;
	GuiShape qTarget;
	GuiShape pTarget;
	GuiShape centerRect;
	GuiShape bottomBar;
	char lastChoice;
	
	
	JButton startButton;
	JButton continueButton;

	JCheckBox licenseAgreement;
	
	TaskStage guiDisplayStage;
	
	Gui getThis()
	{
		return this;
	}
	Gui gui;
	
	Container mainGuiPane;
	Container license;
	Container tutorial;
	Container finished;
	Container starting;

	public void init()
	{
		State state;
		Logic logic;

		try
		{
			state = new State();
			state.initFromURL(new Random(System.nanoTime()));
			
			javax.swing.SwingUtilities.invokeAndWait(new Runnable()
			{
				public void run()
				{
					gui = startGui();
				}
				
			});
			
			logic = new Logic();
			logic.run();
			soundHelper = new SoundHelper();
			soundHelper.loadSounds(this);
			new Timer(40,gui).start();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}
	

	/**
	 * Set up the buttons and canvas and register the listeners.
	 * @return 
	 */
	public Gui startGui()
	{
		setFocusable(true);
		addKeyListener(new KeyboardListener());
		setSize(APPLET_WIDTH, APPLET_HEIGHT);
		setVisible(true);
		//this.setAlwaysOnTop(true);
		//this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		offImage = createImage(size.width, size.height);
		offGraphics = offImage.getGraphics();
		
		this.license = this.setupLicense(new Container());
		this.tutorial = this.setupTutorial(new Container());
		this.mainGuiPane = this.setupTaskGui(new Container());
		this.finished = this.setupEnding(new Container());
		this.starting = this.setupStarting(new Container());

		new Timer(5, this).start();
		
		return this;
	}

	public void setGuiForStage(TaskStage stage)
	{
		switch(stage)
		{
			case LICENSE:
			this.setContentPane(license);
		break;
			case TUTORIAL:
			this.setContentPane(tutorial);
		break;
			case FINISHED:
			this.setContentPane(finished);
		break;
			case TASK_DEMO:
			this.setContentPane(mainGuiPane);
		break;
			case TASK_RUNNING:
			this.setContentPane(mainGuiPane);
		break;
			case TASK_BREAK:
		break;
			case START:
			this.setContentPane(starting);
		break;
		
		}
		this.validate();
		this.guiDisplayStage = stage;
		
	}
	
	public Container setupTaskGui(Container cp)
	{
		cp.setLayout(new BorderLayout());
		CanvasPanel canvasPanel = new CanvasPanel();
		canvasPanel.setBackground(Color.white);

		// Make JButton objects for all the command buttons.
		startButton = new JButton("Start");
		startButton.addActionListener(new StartButtonListener());

		// The command buttons will be arranged in 3 rows. Each row will appear in its own JPanel, and the 3 JPanels will be stacked vertically.
		JPanel controlPanel = new JPanel(); // starts and stops the program
		controlPanel.setLayout(new FlowLayout());
		startButton.setBackground(Color.yellow);
		controlPanel.add(startButton);

		// Use a grid layout to stack the button panels vertically. Also, give them a cyan background.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 1));
		controlPanel.setBackground(Color.cyan);
		//buttonPanel.add(statusPanel);
		buttonPanel.add(controlPanel);
		//buttonPanel.add(scorePanel);

		// Now we have two panels: buttonPanel and canvasPanel. We want buttonPanel to appear above canvasPanel, and canvasPanel should grow with the applet.
		
		cp.setLayout(new BorderLayout());
		cp.add(buttonPanel, BorderLayout.NORTH);
		cp.add(canvasPanel, BorderLayout.CENTER);
		
		this.shapes.add(this.centerRect = new CenterTimerRectangle(cp,Color.BLACK,50,53,8,64));
		this.shapes.add(this.qTarget = new Target(cp,Color.BLUE,20,50,10,10,Selection.LEFT));
		this.shapes.add(this.pTarget = new Target(cp,Color.GREEN,80,50,10,10, Selection.RIGHT));
		this.shapes.add(this.bottomBar = new ProgressBar(cp,new Color(224,224,224), 50,15,33,6));
		cp.validate();

		return cp;
	}

	public Container setupStarting(Container cp)
	{
		cp.setLayout(new BorderLayout());
		JPanel panel = new JPanel(); 
		JEditorPane tutorial = new JEditorPane();
		tutorial.setEditable(false);
	    JScrollPane scrollPane = new JScrollPane(tutorial);      

		try
		{
			tutorial.setPage(State.BASE_URL+"/starting.html");
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JTextField session = new JTextField();
		session.setEditable(false);
		session.setText(String.valueOf(State.getState().sessionID));
		JButton continueButton = new JButton("Continue");
		continueButton.setEnabled(true);
		continueButton.addActionListener(new ContinueButtonListener());
		panel.add(continueButton ,BorderLayout.LINE_START);
		panel.add(new JLabel("Your unique session id="));

		panel.add(session);
		
		cp.add(scrollPane, BorderLayout.CENTER);
		cp.add(panel,BorderLayout.PAGE_END );
		cp.validate();

		return cp;
	}

	public Container setupEnding(Container cp)
	{
		cp.setLayout(new BorderLayout());
		JPanel panel = new JPanel(); 
		JEditorPane tutorial = new JEditorPane();
		tutorial.setEditable(false);
	    JScrollPane scrollPane = new JScrollPane(tutorial);      

		try
		{
			tutorial.setPage(State.BASE_URL+"/ending.html");
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JTextField session = new JTextField();
		session.setEditable(false);
		session.setText(String.valueOf(State.getState().sessionID));
		panel.add(new JLabel("Your unique session id="));

		panel.add(session);
		cp.add(scrollPane, BorderLayout.CENTER);
		cp.add(panel,BorderLayout.PAGE_END );
		cp.validate();

		return cp;
	}
	public Container setupLicense(Container cp)
	{
		cp.setLayout(new BorderLayout());
		JPanel panel = new JPanel(); 
		JEditorPane license = new JEditorPane();
	    JScrollPane scrollPane = new JScrollPane(license);      

		license.setEditable(false);
		try
		{
			license.setPage(State.BASE_URL+"/license.html");
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		licenseAgreement = new JCheckBox();
		licenseAgreement.addActionListener(new CheckBoxListener());
		continueButton = new JButton("Continue");
		continueButton.setEnabled(false);
		continueButton.addActionListener(new ContinueButtonListener());
		panel.add(continueButton,BorderLayout.LINE_END);
		panel.add(licenseAgreement,BorderLayout.CENTER);
		panel.add(new JLabel("Check box to agree and continue"));
		

		cp.add(scrollPane, BorderLayout.CENTER);
		cp.add(panel,BorderLayout.PAGE_END );
		cp.validate();

		return cp;
	}
	public Container setupTutorial(Container cp)
	{
		cp.setLayout(new BorderLayout());
		JPanel panel = new JPanel(); 
		JEditorPane tutorial = new JEditorPane();
		tutorial.setEditable(false);
	    JScrollPane scrollPane = new JScrollPane(tutorial);      

		try
		{
			tutorial.setPage(State.BASE_URL+"/tutorial.html");
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JButton startButton = new JButton("Continue");
		startButton.addActionListener(new ContinueButtonListener());
		panel.add(startButton);
		
		JTextField session = new JTextField();
		session.setEditable(false);
		session.setText(String.valueOf(State.getState().sessionID));
		panel.add(new JLabel("Your unique session id="));

		panel.add(session);
		cp.add(scrollPane, BorderLayout.CENTER);
		cp.add(panel,BorderLayout.PAGE_END );
		cp.validate();

		return cp;
	}

	private class StartButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			TaskStage stage = State.getState().getTaskStage();
			if(stage.paused())
			{
				State.getState().getTaskStage().setPaused(false);
			}
			startButton.setEnabled(false);
			
			requestFocusInWindow();
		}
	}
	private class ContinueButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			TaskStage stage = State.getState().getTaskStage();
			
			if(stage == TaskStage.LICENSE)
			{
				State.getState().setTaskStage(TaskStage.TUTORIAL);
			}
			else if(stage == TaskStage.START)
			{
				State.getState().setTaskStage(TaskStage.TASK_RUNNING);

			}
			else if(stage == TaskStage.TUTORIAL)
			{
				State.getState().setTaskStage(TaskStage.TASK_DEMO);

			}
			else if(stage == TaskStage.TASK_BREAK)
			{
				State.getState().setTaskStage(TaskStage.TASK_RUNNING);

			}
			
		}
	}
	private class CheckBoxListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			continueButton.setEnabled(!continueButton.isEnabled());	

		}
	}
	private class KeyboardListener implements KeyListener
	{

		public void keyTyped(KeyEvent e)
		{
			char ch = (Character.toLowerCase(e.getKeyChar()));
			
			
				State.getState().select(Selection.getSelectionForChar(ch), (int)System.currentTimeMillis());
			
		}

		public void keyPressed(KeyEvent e)
		{
			if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				State.getState().getTaskStage().setPaused(true);
				
				JFrame frame = new JFrame();
				StringBuilder writer = new StringBuilder();
				for (Choice choice : State.getState().getStats().getChoices())
				{
					writer.append(choice.toString()+"\n");
				}
			    JTextArea texPane = new JTextArea();

			    JScrollPane scrollPane = new JScrollPane(texPane);      
			    texPane.setEditable(true);
			    texPane.setText(writer.toString());
			    
			    
			    frame.add(scrollPane);
			    frame.pack();
			    frame.validate();
			    frame.setVisible(true);
				startButton.setEnabled(true);

			}
		}

		public void keyReleased(KeyEvent e)
		{
			// TODO Auto-generated method stub
		}

	}
	/**
	 * CanvasPanel is the class upon which we actually draw. It listens for mouse events and calls the appropriate method of the current command.
	 */
	private class CanvasPanel extends JPanel
	{
		private static final long serialVersionUID = 0;

		/**
		 * Paint the whole drawing
		 * @page the Graphics object to draw on
		 */
		public void paintComponent(Graphics page)
		{
			State state = State.getState();
			super.paintComponent(page); // execute the paint method of JPanel
	
			((Graphics2D) page).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			((Graphics2D) page).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			
			for(GuiShape shape : shapes)
			{
				shape.render((Graphics2D) page);
			}
		}
	}
	public Reader getReaderFromURL(String address)
	{
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
		return br;
	}

	public void actionPerformed(ActionEvent e)
	{
		if(!State.getState().IsRunning())
		{
			if(!(startButton == null))
			{
				startButton.setEnabled(true);
			}
		}
		else
		{
			if(!(startButton == null))
			{
				startButton.setEnabled(false);
			}
		}
		
		if(this.guiDisplayStage != State.getState().getTaskStage())
		{
			this.setGuiForStage(State.getState().getTaskStage());
		}
		
		this.repaint();		
	}
	@Override
	public void update(Graphics g)
	{
		paint(g);
	}
	@Override
	public void paint(Graphics g) 
	{
		super.paint(this.offGraphics);
		g.drawImage(offImage, 0, 0, this);
    }

}