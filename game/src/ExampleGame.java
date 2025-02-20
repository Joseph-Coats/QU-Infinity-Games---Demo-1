	/**************************************************************
	 * Joseph Coats
	 * QU - Infinity Games
	 * Example 1: "Pong" - based on Atari PONG (1972)
	 * Resources: 
	 * 		YouTube, BrandonioProductions, "Making a Platformer With Java"
	 **************************************************************/	

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;



public class ExampleGame 
extends Canvas
implements KeyListener, Runnable
{
	//These booleans toggle extra modes!
	private boolean AIopponent = true, AIplayer = false, fastBall = false;
	
	private Graphics bufferGraphics = null;
	private BufferStrategy bufferStrategy = null;
	
	private Thread thread;
	
	private boolean running, paused, pausedForReset;
	
	private Rectangle player;
	private Rectangle ball;
	private Rectangle opponent;
	private Rectangle courtTable;
	
	private Character key1 = '\0', key2 = '\0';
	private int opponentSpeed = 16, playerSpeed = 16, ballVelocityX = 0, ballVelocityY = 0, scoreP = 0, scoreO = 0;
	private int ballOriginX, ballOriginY, timer = 150;
	private int ballOldPos[][] = new int[5][2];
	Dimension court;
	
	private BufferedImage ballI, paddleLeft, paddleRight, pausedTitle;
	
	Clip clipBoing, clipFail;
	AudioInputStream audioStream;

	/**************************************************************
	 * This constructor and paint method initialize the program 
	 * and its buffers.
	 **************************************************************/	
	public ExampleGame(Dimension size)
	{
		this.setPreferredSize(size);
		this.addKeyListener(this);

		this.thread = new Thread(this);
		running = true;
		paused = false;
		
		player = new Rectangle(30, (size.height/2)-47, 25, 75);
		opponent = new Rectangle((size.width-70), (size.height/2)-47, 25, 75);
		courtTable = new Rectangle(30, 30, size.width-75, size.height-75);
		
		ball = new Rectangle((size.width/2), (size.height/2), 25, 25);
		ballOriginX = ball.x-ball.width/2;
		ballOriginY = ball.y-ball.height/2;

		court = size;	
		
		// Attempting to load the image files.
		try {
			ballI = ImageIO.read(getClass().getResource("/ball.png"));
		} catch(IOException e) {
			e.printStackTrace();
		}
		try {
			paddleLeft = ImageIO.read(getClass().getResource("/paddle.png"));
		} catch(IOException e) {
			e.printStackTrace();
		}
		try {
			paddleRight = ImageIO.read(getClass().getResource("/paddle.png"));
		} catch(IOException e) {
			e.printStackTrace();
		}
		try {
			pausedTitle = ImageIO.read(getClass().getResource("/pause.png"));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		File boing = new File("pong.wav");
		try {
			audioStream = AudioSystem.getAudioInputStream(boing);
			clipBoing = AudioSystem.getClip();
			clipBoing.open(audioStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		File fail = new File("pongFail.wav");
		try {
			audioStream = AudioSystem.getAudioInputStream(fail);
			clipFail = AudioSystem.getClip();
			clipFail.open(audioStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		pausedForReset = true;
	}
	
	public void paint(Graphics g)
	{
		if(bufferStrategy == null)
		{
			this.createBufferStrategy(2);
			bufferStrategy = this.getBufferStrategy();
			bufferGraphics = bufferStrategy.getDrawGraphics();
			this.thread.start();
		}
	}
	/**************************************************************
	 * This method calls DoLogic, Draw, & DrawBackbufferToScreen 
	 * for every frame.
	 **************************************************************/	
	@Override
	public void run() 
	{
		while(running)
		{
			if(pausedForReset && !paused)
			{ 
				timer--;
				if(timer <= 0)
				{
					pausedForReset = false;
					timer = 100;
				}
			}
			
			if(!paused && !pausedForReset)
			{
				DoLogic();
			} // End of Paused
			
			Draw();
			DrawBackbufferToScreen();

			Thread.currentThread();
			try
			{
				Thread.sleep(10);
			} catch (Exception e) {
				e.printStackTrace();
			}
				
		} // End of Running
	}
	
	/**************************************************************
	 * This method runs the logic of the program for every frame.
	 **************************************************************/
	public void DoLogic()
	{
		///////////////////////////////////////////////////////////////
 		// PLAYER LOGIC

		 //System.out.println(KeyEvent.KEY_PRESSED);
		
 		if(AIplayer)
 		{
 			int locationPlayer = (player.y+player.height/2);
 			int locationBall = (ball.y+ball.height/2);
 		
 			if (locationPlayer < locationBall-playerSpeed/3)
 				player.setLocation(player.x, player.y+playerSpeed/3);
			if (locationPlayer > locationBall+playerSpeed/3)
				player.setLocation(player.x, player.y-playerSpeed/3);
 		} else {
		if(key1 == 's')
			player.setLocation(player.x, player.y +playerSpeed);
		if (key1 == 'w')
			player.setLocation(player.x, player.y -playerSpeed);
 		}
 		
		///////////////////////////////////////////////////////////////
 		// OPPONENT LOGIC
 		if(AIopponent)
 		{
 			int locationOpponent = (opponent.y+opponent.height/2);
 			int locationBall = (ball.y+ball.height/2);
 		
 			if (locationOpponent < locationBall-opponentSpeed/3)
				opponent.setLocation(opponent.x, opponent.y+opponentSpeed/3);
			if (locationOpponent > locationBall+opponentSpeed/3)
				opponent.setLocation(opponent.x, opponent.y-opponentSpeed/3);
 		} else {
 			if(key2 == 's')
 				opponent.setLocation(opponent.x, opponent.y +opponentSpeed);
 			if (key2 == 'w')
 				opponent.setLocation(opponent.x, opponent.y -opponentSpeed);
 		}
		
		///////////////////////////////////////////////////////////////
 		// BALL LOGIC
		
		if(ballVelocityX == 0) // Initial velocity if ball is not moving.
		{
			ballVelocityX = 6;
			if(this.getWidth() > 800)
				ballVelocityX +=(this.getWidth()-800)/100;
			if(fastBall)
				ballVelocityX *=2;
			ballVelocityY = (int)(Math.random()*5)-2;
		}
		
		if(ball.y <= 30 || ball.y >= courtTable.height)
		{ // If the ball gets caught in the bottom/top of the court.
			ballVelocityY *= -1;
			if(ball.y >= courtTable.height)
				ball.y -= 2;
			if(ball.y <= 30)
				ball.y += 2;
			try {
				clipBoing.setMicrosecondPosition(0);
				clipBoing.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if	(
				(ball.x >= opponent.x-opponent.width && ball.x <= opponent.x-opponent.width+ballVelocityX && ball.y+ball.height >= opponent.y && ball.y <= opponent.y+opponent.height) || 
				(ball.x <= player.x+player.width && ball.x >= player.x+player.width+ballVelocityX && ball.y+ball.height >= player.y && ball.y <= player.y+player.height)
			) // Collides with either paddles. 
		{
			ballVelocityX *= -1;
			ballVelocityY = (int)(Math.random()*7)-3;			
			System.out.println(" " + ballVelocityY + " ");
			try {
				clipBoing.setMicrosecondPosition(0);
				clipBoing.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			//if(AIplayer) playerSpeed += (int)(Math.random()*2.0) < 1? -4 : 4;
			//if(AIopponent) opponentSpeed += (int)(Math.random()*2.0) < 1? -4 : 4;
		}
		
		
		ball.setLocation(ball.x + ballVelocityX, ball.y + ballVelocityY);
		
		///////////////////////////////////////////////////////////////
		// Player barriers.
		
		if(player.y < 30)
			player.y = 30;
		if(player.y > courtTable.height-45)
			player.y = courtTable.height-45;
		
		if(opponent.y < 30)
			opponent.y = 30;
		if(opponent.y > courtTable.height-45)
			opponent.y = courtTable.height-45;
		
		///////////////////////////////////////////////////////////////
		// Scoring system.
		
		if(ball.x < courtTable.x)
		{
			scoreO++;
			ball.x = ballOriginX;
			ball.y = ballOriginY;
			ballVelocityX = 0;
			player.y = ballOriginY;
			opponent.y = ballOriginY;
			pausedForReset = true;
			
			try {
				clipFail.setMicrosecondPosition(0);
				clipFail.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(ball.x > (courtTable.x+courtTable.width-ball.width))
		{
			scoreP++;
			ball.x = ballOriginX;
			ball.y = ballOriginY;
			ballVelocityX = 0;
			player.y = ballOriginY;
			opponent.y = ballOriginY;
			pausedForReset = true;
			try {
				clipFail.setMicrosecondPosition(0);
				clipFail.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**************************************************************
	 * The first method draws the objects to the back buffer,
	 * once rendering is finished, it is then displayed to the 
	 * primary buffer.
	 **************************************************************/
	public void Draw()
	{
		bufferGraphics = bufferStrategy.getDrawGraphics();
		try
		{
			this.setBackground(Color.DARK_GRAY);
			this.setForeground(Color.WHITE);
			bufferGraphics.clearRect(0, 0, this.getSize().width, this.getSize().height);
		
			// SHAPES are DRAWN
			Graphics2D bufferGraphics2D = (Graphics2D)bufferGraphics;
			//bufferGraphics2D.draw(player);
			//bufferGraphics2D.draw(ball);
			//bufferGraphics2D.draw(opponent);
			//bufferGraphics2D.draw(courtTable);
			
			bufferGraphics2D.drawString("Left Player: " + scoreP + " || Right Player: " + scoreO, 30, 25);
			
			bufferGraphics2D.setColor(Color.BLACK);
			bufferGraphics2D.fill(courtTable);
			
			if(ball.x%10 == 0)
			{
				ballOldPos[0][0] = ball.x; // X Pos
				ballOldPos[0][1] = ball.y; // Y Pos
				for(int sub = 3; sub >= 0; sub--)
				{
					ballOldPos[sub+1][0] = ballOldPos[sub][0]; // X Pos
					ballOldPos[sub+1][1] = ballOldPos[sub][1]; // Y Pos
					//System.out.println(ballOldPos[sub][0] + " " + ballOldPos[sub][1]); // Debug
				}
				//System.out.println("\n\n"); // Debug
			}
			bufferGraphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
			float opacity = 0.1f;
			for(int sub = 4; sub > 0; sub--)
			{
				bufferGraphics2D.drawImage(ballI, ballOldPos[sub][0], ballOldPos[sub][1], 25, 25, null);
				opacity += 0.1f;					
				bufferGraphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				//System.out.print(sub); // Debug
			}
			bufferGraphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

			
			if(!pausedForReset || (pausedForReset && timer % 10 < 5))
			{ // If the level, resets, pause for a moment and flash the objects to get attention.
				bufferGraphics2D.drawImage(ballI, ball.x, ball.y, 25, 25, null);				
				bufferGraphics2D.drawImage(paddleLeft, player.x, player.y, 25, 75, null);
				bufferGraphics2D.drawImage(paddleRight, opponent.x, opponent.y, 25, 75, null);
			}
			
			if(paused)
				bufferGraphics2D.drawImage(pausedTitle, 
						courtTable.x + courtTable.width/2 - 256, 
						courtTable.y + courtTable.height/2 - 64, 
						128*4, 32*4, null);
			
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			bufferGraphics.dispose();
		}
	}
	public void DrawBackbufferToScreen()
	{
		bufferStrategy.show();
		Toolkit.getDefaultToolkit().sync();
	}
	
	/**************************************************************
	 * These methods are triggered by key events.
	 * keyTyped is unused in this example.
	 **************************************************************/
	@Override
	public void keyPressed(KeyEvent arg0) 
	{
	 if (validKey(arg0.getKeyChar()))
	 {
		key1 = arg0.getKeyChar();
		if (key1.isUpperCase(key1))
			key1 = key1.toLowerCase(key1);

	 }	
	 
	if (arg0.getKeyCode() == KeyEvent.VK_UP )
	{
				key2 = 'w';
	}
	if (arg0.getKeyCode() == KeyEvent.VK_DOWN)
	{
				key2 = 's';
	}
		
		if(arg0.getKeyChar() == ' ')
		{
			paused = !paused;
			System.out.println("Paused: " + paused);
		}
		}
	@Override
	public void keyReleased(KeyEvent arg0)  // Does not count shift
	{
		Character input = arg0.getKeyChar();
	if (input.isUpperCase(input))
		input = key1.toLowerCase(input);
		
	 if (validKey(arg0.getKeyChar()))
	 {
		if(key1 == input)
			key1 = '\0'; 
	 }
	 
	 if(arg0.getKeyCode() == KeyEvent.VK_DOWN)
		if(key2 == 's')
			key2 = '\0'; 
	if(arg0.getKeyCode() == KeyEvent.VK_UP)
		if(key2 == 'w')
			key2 = '\0'; 

	 
	}
	@Override
	public void keyTyped(KeyEvent arg0) 
	{

	}

	/**************************************************************
	 * The following method determines if the pressed key is valid
	 * for key1 and key2.
	 **************************************************************/
	private boolean validKey(Character myChar)
	{
		 if (
			myChar == 'w' || myChar == 's' ||
			myChar == 'W' || myChar == 'S' 
			)
			 return true;
		 return false;
	}
	
}
