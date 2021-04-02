/*
 * This class is to handle the window of auction stage.
 * The frame will close automatically until the stage is finished.
 * 
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;


public class TrumpFrame extends JFrame implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1L; 
	
	private ButtonGroup colorGroup;
	private ButtonGroup numberGroup;
	private JButton buttonClear;
	private  JRadioButton[] colorRadioButton;
	private  JRadioButton[] numberRadioButton;
	private int currentColor;
	private int currentNumber;
	
	public JButton buttonCall;
	public JButton buttonPass;
	public TrumpPanel trumpPanel;
	public boolean isFinished;
	public boolean isTriggeredByCall;  // whether the buttonCall is clicked;
	public boolean isTriggeredByPass;  // whether the buttonPass is clicked;
	public int currentBoundedValue;  // number*10 + color, refresh every player's call
	
	public TrumpFrame() {
		 super("Auction");
		 this.setSize(575, 350);
		 this.setLayout(null);
		 
		 colorGroup = new ButtonGroup();
		 numberGroup = new ButtonGroup();
		 trumpPanel = new TrumpPanel();
		 buttonCall = new JButton("Call");
		 buttonClear = new JButton("Clear");
		 buttonPass = new JButton("Pass");
		 colorRadioButton = new JRadioButton[5];
		 numberRadioButton = new JRadioButton[8];
		 currentBoundedValue = 0;
		 isFinished = false;
		 isTriggeredByCall = false;
		 
		 colorRadioButton[0] = new JRadioButton("♣");
		 colorRadioButton[1] = new JRadioButton("♦");
		 colorRadioButton[2] = new JRadioButton("♥");
		 colorRadioButton[3] = new JRadioButton("♠");
		 colorRadioButton[4] = new JRadioButton("NT");

		 for (int i = 0; i < 7; i++)
			 numberRadioButton[i] = new JRadioButton(String.valueOf(i+1));
		 
	}
	
	public void start() {
		/*  set button  */
		buttonCall.addActionListener(this);
		buttonCall.setBounds(20, 285, 65, 25);
		buttonCall.setFont(new Font("TimesRoman", Font.BOLD, 12));

		buttonPass.addActionListener(this);
		buttonPass.setBounds(115, 285, 65, 25);
		buttonPass.setFont(new Font("TimesRoman", Font.BOLD, 12));
		
		buttonClear.addActionListener(this);
		buttonClear.setBounds(481, 10, 75, 25);
		buttonClear.setFont(new Font("TimesRoman", Font.BOLD, 12));
		
		this.add(buttonCall);
		this.add(buttonPass);
		this.add(buttonClear);
		
		/*  set radio-button  */
		for (int i = 0; i < 5; i++) {
			colorRadioButton[i].addItemListener(this);
			colorRadioButton[i].setBounds(25, 42+i*25, 50, 30);
			colorRadioButton[i].setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
			colorGroup.add(colorRadioButton[i]);
			this.add(colorRadioButton[i]);
		}
		colorRadioButton[0].setSelected(true);
		colorRadioButton[1].setForeground(Color.RED);
		colorRadioButton[2].setForeground(Color.RED);
		colorRadioButton[4].setFont(new Font(Font.DIALOG, Font.BOLD, 14));  // resize
		
		for (int i = 0; i < 7; i++) {
			numberRadioButton[i].addItemListener(this);
			numberRadioButton[i].setBounds(120, 42+i*25, 50, 30);
			numberRadioButton[i].setFont(new Font("TimesRoman", Font.BOLD, 14));
			numberGroup.add(numberRadioButton[i]);
			this.add(numberRadioButton[i]);
		}
		numberRadioButton[0].setSelected(true);
		
		/*  set panel  */
		trumpPanel.setBounds(0, 0, 575, 350);
		
		
		this.add(trumpPanel);
		
		
		/*  set frame size  */
		this.setResizable(false);
		this.setSize(575, 350);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	private String getColorIcon(int color) {
		switch (color) {
		case 1:
			return "♣";
		case 2:
			return "♦"; 
		case 3:
			return "♥";
		case 4:
			return "♠";
		default:
			return "NT";
		}
	}
	
	public void setName(String yours, String left, String front, String right) {
		trumpPanel.setName(yours, left, front, right);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == buttonCall) {
			if (currentNumber*10 + currentColor <= currentBoundedValue) {
				trumpPanel.isPrintInvalidCall = true;
				this.repaint();
			}  else {
				int sureToCallValue = 0;  // default value is 0
				int tempValue = 0;
				
				/*  warn client if the number is not his expected  */
				for (int i = 1; i <= currentNumber-1; i++) {
					tempValue = i*10 + currentColor;
					if (tempValue > currentBoundedValue) {
						sureToCallValue = JOptionPane.showConfirmDialog(null,
								"Sure to call " + currentNumber 
								+ getColorIcon(currentColor) + " instead of " + i 
								+ getColorIcon(currentColor) + "?",
								"Warning", JOptionPane.YES_NO_OPTION  ,
								JOptionPane.WARNING_MESSAGE);
						break;
					}
				}
				
				if (sureToCallValue == 0) {  // confirm
					isTriggeredByCall = true;
					trumpPanel.isPrintInvalidCall = false;
					currentBoundedValue = currentNumber*10 + currentColor;
					trumpPanel.yourRecords.add(currentBoundedValue);
					this.repaint();
				}
			}
		}  else if (event.getSource() == buttonPass) {
			isTriggeredByPass = true;
			trumpPanel.isPrintInvalidCall = false;
			trumpPanel.yourRecords.add(0);
			this.repaint();
		}  else if (event.getSource() == buttonClear) {
			trumpPanel.isPrintInvalidCall = false;
			trumpPanel.yourRecords.clear();
			trumpPanel.leftRecords.clear();
			trumpPanel.frontRecords.clear();
			trumpPanel.rightRecords.clear();
			this.repaint();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent event) {
		for (int i = 0; i < 5; i++)
			if (event.getSource() == colorRadioButton[i])
				currentColor = i+1;
		for (int i = 0; i < 7; i++)
			if (event.getSource() == numberRadioButton[i])
				currentNumber = i+1;
	}
}
