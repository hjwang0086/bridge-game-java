/*
 * This class is to support the panel of TrumpFrame.
 * The support includes the drawing of strings,
 * painting current auction records.
 * 
 * For records, value = number + color
 * if pass, value = 0
 * 
 * For variable color, 1 = clubs, and so on
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;


public class TrumpPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public ArrayList<Integer> yourRecords = new ArrayList<Integer>();
	public ArrayList<Integer> leftRecords = new ArrayList<Integer>();
	public ArrayList<Integer> frontRecords = new ArrayList<Integer>();
	public ArrayList<Integer> rightRecords = new ArrayList<Integer>();
	public boolean isPrintInvalidCall = false;
	public String yourName = new String();
	public String leftName = new String();
	public String frontName = new String();
	public String rightName = new String();
	public String whichPlayerTurn = "";
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		/*  your side  */
		g.setFont(new Font(Font.DIALOG ,Font.BOLD, 14));
		g.drawString("Color", 20, 30);
		g.drawString("Number", 115, 30);
		g.drawLine(97, 17, 97, 225);  // vertical line
		g.drawLine(17, 35, 180, 35);  // horizontal line
		g.drawLine(205, 17, 205, 330);  // separated line
		if (isPrintInvalidCall) {
			g.setFont(new Font(Font.DIALOG_INPUT ,Font.BOLD, 16));
			g.setColor(Color.BLUE);
			g.drawString("Invalid Call!", 23, 260);
			g.setFont(new Font(Font.DIALOG ,Font.BOLD, 14));
			g.setColor(Color.BLACK);
		}
		
		/*  public side  */
		g.drawString("Records:", 227, 30);
		g.drawRect(225, 40, 330, 277);
		
		if (whichPlayerTurn.equals("I"))
			g.setColor(Color.RED);
		g.drawString("You", 235, 58);
		g.setColor(Color.BLACK);
		
		if (whichPlayerTurn.equals("Left"))
			g.setColor(Color.RED);
		g.drawString("Left", 310, 58);
		g.setColor(Color.BLACK);
		
		if (whichPlayerTurn.equals("Front"))
			g.setColor(Color.RED);
		g.drawString("Front", 385, 58);
		g.setColor(Color.BLACK);
		
		if (whichPlayerTurn.equals("Right"))
			g.setColor(Color.RED);
		g.drawString("Right", 470, 58);
		g.setColor(Color.BLACK);
		
		/*  TEMP  */
		g.setFont(new Font(Font.DIALOG ,Font.PLAIN, 12));
		g.drawString("[" + yourName + "]", 235, 78);
		g.drawString("[" + leftName + "]", 310, 78);
		g.drawString("[" + frontName + "]", 385, 78);
		g.drawString("[" + rightName + "]", 470, 78);
		
		paintRecords(g, yourRecords, 235);
		paintRecords(g, leftRecords, 310);
		paintRecords(g, frontRecords, 385);
		paintRecords(g, rightRecords, 470);
	}
	
	private void paintRecords(Graphics g, ArrayList<Integer> list, int bound) {
		int color;
		int number;
		
		for (int i = 0; i < list.size(); i++) {
			color = list.get(i)%10;
			number = list.get(i)/10;
			g.setColor(Color.BLACK);
			
			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
			if (number != 0)
				g.drawString(String.valueOf(number), bound+5, 104+25*i);
			switch (color) {
			case 1:
				g.setFont(new Font("TimesRoman", Font.PLAIN, 15));
				g.drawString("♣", bound+15, 105+25*i);  break;
			case 2:
				g.setFont(new Font("TimesRoman", Font.PLAIN, 15));
				g.setColor(Color.RED);
				g.drawString("♦", bound+15, 105+25*i);  break;
			case 3:
				g.setFont(new Font("TimesRoman", Font.PLAIN, 15));
				g.setColor(Color.RED);
				g.drawString("♥", bound+15, 105+25*i);  break;
			case 4:
				g.setFont(new Font("TimesRoman", Font.PLAIN, 15));
				g.drawString("♠", bound+15, 105+25*i);  break;
			case 5:
				g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
				g.drawString("NT", bound+15, 104+25*i);  break;
			default:
				g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
				g.drawString("PASS", bound+6, 104+25*i);  break;
			}
		}
	}
	
	public void setName(String yours, String left, String front, String right) {
		yourName = yours;
		leftName = left;
		frontName = front;
		rightName = right;
	}
}
