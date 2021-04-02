/*
 * This class is to handle the GUI of GameStage
 * 
 * In constructor, pathArray is the set of cards' picture path.
 */

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class GamePanel extends JPanel {
	private static final long serialVersionUID = 1L; 
	private static final int GAP = 40;
	
	private BufferedImage backImage;
	private BufferedImage backRotateImage;
	private FileInputStream finStream = null;
	
	public BufferedImage yourImage = null;
	public BufferedImage leftImage = null;
	public BufferedImage frontImage = null;
	public BufferedImage rightImage = null;
	public String whichPlayerTurn = "";
	public boolean isWaitingRestart = false;
	public int currentGameTrump;
	public int trickCount=0;
	public int maxTrickCount = 0;
	public int leftCardsStartIndex = 0;
	public int frontCardsStartIndex = 0;
	public int rightCardsStartIndex = 0;
	
	public String yourName = new String();
	public String leftName = new String();
	public String frontName = new String();
	public String rightName = new String();
	
	public GamePanel(Player player) {	
		this.requestFocus(true);
		currentGameTrump = 0;
		trickCount = 0;
		
		/*  get back side picture  */
		try {
			finStream = new FileInputStream(new File("./res/cards/back.jpg"));
			backImage = ImageIO.read(finStream);
			finStream = new FileInputStream(new File("./res/cards/back_rotate.jpg"));
			backRotateImage = ImageIO.read(finStream);
		}  catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(null, "Image Not Found", 
					"Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			System.exit(0);
		}  catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Image Loading Failed", 
					"Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			System.exit(0);
		}
		
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		this.paintCurrentTrump(g);
		this.paintTeamsTricks(g);
		
		/*  Rectangle on public desk  */
		g.drawRect(463, 400, 74, 105);
		g.drawRect(313, 300, 74, 105);
		g.drawRect(463, 200, 74, 105);
		g.drawRect(613, 300, 74, 105);
		
		/*  Player's name  */
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		g.setColor(Color.BLUE);
		if (whichPlayerTurn.equals("I"))
			g.setColor(Color.RED);
		g.drawString("You: " + yourName, 462, 535);
		
		g.setColor(Color.BLUE);
		if (whichPlayerTurn.equals("Left"))
			g.setColor(Color.RED);
		g.drawString("Left: " +leftName, 25, 50);
		
		g.setColor(Color.BLUE);
		if (whichPlayerTurn.equals("Front"))
			g.setColor(Color.RED);
		g.drawString("Front: " +frontName, 462, 175);
		
		g.setColor(Color.BLUE);
		if (whichPlayerTurn.equals("Right"))
			g.setColor(Color.RED);
		g.drawString("Right: " +rightName, 863, 645);
		g.setColor(Color.BLACK);
		
		if (isWaitingRestart) {
			g.setFont(new Font("TimesRoman", Font.PLAIN, 13));
			g.drawString("Wait for other's", 140, 685);
			g.drawString("response...", 140, 702);
			isWaitingRestart = false;
			g.setFont(new Font("TimesRoman", Font.BOLD, 13));
		}
		
		this.paintCardsImage(g);
		
	}
	
	private void paintCurrentTrump(Graphics g) {
		g.setFont(new Font("TimesRoman", Font.PLAIN, 13));
		g.drawString("Current Trump:", 30, 640);
		switch(currentGameTrump) {
		case 1:
			g.setFont(new Font("TimesRoman", Font.PLAIN, 17));
			g.drawString("♣", 135, 640);  break;
		case 2:
			g.setFont(new Font("TimesRoman", Font.PLAIN, 17));
			g.setColor(Color.RED);
			g.drawString("♦", 135, 640);
			g.setColor(Color.BLACK);  break;
		case 3:
			g.setFont(new Font("TimesRoman", Font.PLAIN, 17));
			g.setColor(Color.RED);
			g.drawString("♥", 135, 640);
			g.setColor(Color.BLACK);  break;
		case 4:
			g.setFont(new Font("TimesRoman", Font.PLAIN, 17));
			g.drawString("♠", 135, 640);  break;
		case 5:
			g.setFont(new Font("TimesRoman", Font.BOLD, 13));
			g.drawString("NT", 135, 640);  break;
		}
	}
	
	private void paintCardsImage(Graphics g) {
		int X = 0, Y = 0; 
		
		/*  draw the cards other have, that is, background  */
		
		for (int i = frontCardsStartIndex; i < 13; i++) {
			X = 463+(i-6)*GAP;
			Y = 40;
			g.drawImage(backImage, X, Y, this);
		}
		for (int i = 12; i >= leftCardsStartIndex; i--) {
			X = 25;
			Y = 345+(i-7-leftCardsStartIndex) *GAP;
			g.drawImage(backRotateImage, X, Y, this);
		}
		for (int i = rightCardsStartIndex; i < 13; i++) {
			X = 863;
			Y = 345+(i-7)*GAP;
			g.drawImage(backRotateImage, X, Y, this);
		}
		
		
		/*  the following is to handle cards on desk */
		if(yourImage != null)
			g.drawImage(yourImage, 463, 400, this);
		if(leftImage != null)
			g.drawImage(leftImage, 313, 300, this);
		if(frontImage != null)
			g.drawImage(frontImage, 463, 200, this);
		if(rightImage != null)
			g.drawImage(rightImage, 613, 300, this);
	}
	
	private void paintTeamsTricks(Graphics g) {
		g.setFont(new Font("TimesRoman", Font.PLAIN, 13));
		g.drawString("Team's Tricks: " + String.valueOf(trickCount) + "/" +  
				String.valueOf(maxTrickCount), 30, 660);
	}
	
	public void setName(String yours, String left, String front, String right) {
		yourName = yours;
		leftName = left;
		frontName = front;
		rightName = right;
	}
}
