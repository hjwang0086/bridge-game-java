/*
 * This class is to maintain a single player's game stage.
 * 
 */


import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


public class GameStage extends JFrame implements ActionListener, MouseListener {
	private static final long serialVersionUID = 1L; 
	private static final int GAP = 40;
	
	private final JTextField copyRightText;
	private final JButton restartButton;
	private final JLabel[] cardsLabel = new JLabel[13];
	
	private Player player = new Player();
	
	public GamePanel gamePanel;
	public boolean isMyTurn = false;
	public boolean isRestartQuest = false;  // ask Client to restart
	public int currentPaidCard = 0;
	public int currentRoundTrump = 0;
	
	public GameStage() {
		super("Contract Bridge v1.1");
		this.setSize(1000, 740);
			
		this.requestFocus(true);
		this.setLayout(null);
		this.setVisible(true);
		
		restartButton = new JButton("Restart");
		restartButton.addActionListener(this);
		restartButton.setBounds(30, 675, 100, 30);
		restartButton.setEnabled(false);
		restartButton.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
		restartButton.setIgnoreRepaint(true);
		
		for (int i = 0; i < 13; i++) {
			cardsLabel[i] = new JLabel();
			cardsLabel[i].addMouseListener(this);
			cardsLabel[i].setBounds(463+(i-6)*GAP, 553, 75, 107);
			cardsLabel[i].setEnabled(true);
		}
		
		copyRightText = new JTextField("Game Maker: Hjwang");
		copyRightText.setBounds(825, 680, 173, 30);
		copyRightText.setBackground(Color.LIGHT_GRAY);
		copyRightText.setEditable(false);
		copyRightText.setForeground(Color.BLUE);
		copyRightText.setFont(new Font("TimesRoman", Font.BOLD, 14));
		copyRightText.setIgnoreRepaint(true);
		
		gamePanel = new GamePanel(player);
		gamePanel.setBounds(0, 0, 1000, 740);
		
		this.add(copyRightText);
		this.add(restartButton);
	}
	
	public void start() {
		setCardsImagePath();
		
		try {
			for (int i = 12; i >= 0; i--) {
				cardsLabel[i].setIcon(new ImageIcon(player.cards[i].imagePath));
				this.add(cardsLabel[i]);
			}
		}  catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Image Not Found", 
					"Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			System.exit(0);
		}
		
		this.add(gamePanel);  // or it would cover cardsLabel
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setSize(1000, 740);
		this.setVisible(true);
		this.repaint();
		
		System.gc();
	}
	

	private void setCardsImagePath() {
		for (int i = 0; i < 13; i++)
			player.cards[i].setImagePath();
	}
	
	private boolean isLegalCard(int index) {
		if (currentRoundTrump == 0)   {// first time
			System.out.println("First Time");
			return true;
		}	
		if (currentRoundTrump == player.cards[index].color) {
			System.out.println("same color");
			return true;
		}
		else for (int i = 0; i < 13; i++)
			if (player.cards[i].color == currentRoundTrump && cardsLabel[i].isVisible())
				return false;
		System.out.println("remain");
		return true;
	}
	
	public void setPlayer(Player p) {
		player = p;
	}
	
	public void restart() {
		this.remove(gamePanel);
		for (int i = 0; i < 13; i++) {
			cardsLabel[i].setBounds(463+(i-6)*GAP, 553, 75, 107);
			cardsLabel[i].setVisible(true);
			restartButton.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
		}
		this.start();
	}
	
	public void setName(String yours, String left, String front, String right) {
		gamePanel.setName(yours, left, front, right);
	}
	
	public void setCurrentTrump(int trump) {
		gamePanel.currentGameTrump = trump;
	}
	
	public void incTrickCount() {
		gamePanel.trickCount++;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == restartButton) {
			isRestartQuest = true;
			restartButton.setEnabled(false);
			gamePanel.isWaitingRestart = true;
			gamePanel.repaint();
			this.repaint();
		}  
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		/* Confirm to click something  */
		for (int i = 0; i < 13; i++) {
			if (event.getSource() == cardsLabel[i] && isLegalCard(i) && isMyTurn) {
				currentPaidCard = player.cards[i].color*100 + player.cards[i].number;
				cardsLabel[i].setVisible(false);
				try {
					gamePanel.yourImage = ImageIO.read(
							new FileInputStream(new File(player.cards[i].imagePath)));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				for (int j = i+1; j < 13; j++)
					cardsLabel[j].setLocation(cardsLabel[j].getX()-GAP, cardsLabel[j].getY());
					
				isMyTurn = false;
				this.repaint();
			}
		}
	}
	
	public void clearPanelImage() {
		gamePanel.yourImage = null;
		gamePanel.leftImage = null;
		gamePanel.frontImage = null;
		gamePanel.rightImage = null;
	}
	
	public void setRestartButtonEnabled(boolean b) {
		restartButton.setEnabled(b);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		/*  Press something  */
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		/* Not confirm to click something  */
	}

	
	@Override
	public void mouseEntered (MouseEvent event) {
		/*  Mouse pass-by  */
		for (int i = 0; i < 13; i++) {
			if (event.getSource() == cardsLabel[i])
				cardsLabel[i].setLocation(cardsLabel[i].getX(), cardsLabel[i].getY()-30);
		}
	}

	@Override
	public void mouseExited(MouseEvent event) {
		/*  Mouse leaves  */
		for (int i = 0; i < 13; i++) {
			if (event.getSource() == cardsLabel[i])
				cardsLabel[i].setLocation(cardsLabel[i].getX(), cardsLabel[i].getY()+30);
		}
	}

}
