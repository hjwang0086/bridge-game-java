/*
 * This class is the Client side.
 * 
 * Instead of DataOutputStream as toServer, use PrintWriter,
 * because the DataOutputStream would send message to server unstoppable, 
 * while PrintWriter's "flush" function causes the data would only be sent once,
 * which is the model's desire.
 * 
 * During the waiting time to get message "GameStart", 
 * client should use Thread.sleep method instead of just do nothing,
 * because the client's I/O would be stuck in the infinite loop.
 * 
 * There are many flags instead of implementing code on listenerThread,
 * because GUI cannot be synchronized(without using invokeLater method).
 */

/*
 * TODO ex.printStackTrace() implements File I/O
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Client extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final int MAX_CLIENT_NUM = 4;
	private static final int REFRESH_TIME = 10;
	
	private final int myID;
	
	private BufferedReader fromServer;
	private Card[] playerCards;
	private GameStage gameStage;
	private Player player;
	private PrintWriter toServer;
	private Socket client;
	private String rcvData;
	private String trumpStringToRefresh = "";
	private TrumpFrame trumpFrame;
	private boolean hasACKedToRestart = false;
	private boolean isSetCardsOK = false;
	private boolean isMyTurnToCallTrump = false;
	private boolean isToRefreshTrumpRecord = false;
	private boolean isFirstTimeInRound = true;  // In a single round, is it first time?
	private int currentAuctionCaller = -1;
	private int currentAuctionPlayer;
	private int currentAuctionValue;
	private int round = 13;
	
	public String name = "";
	public String leftName = "LEFT";
	public String frontName = "FRONT";
	public String rightName = "RIGHT";
	
	public Client() throws Exception {
		ClientThread listenerThread = new ClientThread();
		player = new Player();
		playerCards = new Card[13];
		trumpFrame = new TrumpFrame();
		
		for (int i = 0; i < 13; i++)
			playerCards[i] = new Card();
		
		Login login = new Login();
		
		try {
			while (login.IPAddr.isEmpty() || login.PortNum.isEmpty())
				Thread.sleep(1000);
			client = new Socket(login.IPAddr, Integer.valueOf(login.PortNum));
		}  catch (ConnectException ex) {
			JOptionPane.showMessageDialog(null, "Server Not Found", 
					"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}  catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Illegal Input Format", 
					"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
		
		login.loginSuccess = true;
		login.buttonLogin.setEnabled(false);
		login.IPField.setEditable(false);
		login.PortField.setEditable(false);
		login.repaint();
		
		fromServer = new BufferedReader
				(new InputStreamReader(client.getInputStream()));
		toServer = new PrintWriter
				(new OutputStreamWriter(client.getOutputStream()));
		
		try {
			while (name.isEmpty())
				name = JOptionPane.showInputDialog(null, "Enter Your Name:", 
						"Login Success", JOptionPane.INFORMATION_MESSAGE);
		}  catch (NullPointerException ex) {  // means the client cancel to login
			System.exit(0);
		}
		toServer.println(name);
		toServer.flush();
		
		rcvData = fromServer.readLine();
		myID = Integer.valueOf(rcvData);
		System.out.println("Name = " + name + ", ID = " + myID);
		toServer.println("ACK");
		toServer.flush();
		
		while (!rcvData.equals("SetNameAll")) {
			rcvData = fromServer.readLine();
		}
		ackToServer();
			
		for (int i = 0; i < MAX_CLIENT_NUM; i++) {
			rcvData = fromServer.readLine();
			setName(i, rcvData);
			ackToServer();
		}
			
		rcvData = "";
		
		while (!rcvData.equals("GameStart")) {
			rcvData = fromServer.readLine();
			System.out.println("GET " + rcvData);
		}
		
		login.setVisible(false);
		login = null;
		listenerThread.start();  // handle rcvData
	}
	
	public void gameStart() throws Exception {
		while (isSetCardsOK != true)
			Thread.sleep(REFRESH_TIME);
		isSetCardsOK = false;
		System.out.println("Set Cards Done");
		
		player = new Player(playerCards);
		player.sortCardsOrder();
		
		gameStage = new GameStage();
		gameStage.setPlayer(player);
		gameStage.setName(name, leftName, frontName, rightName);
		gameStage.start();
		
		auctionHandling();
		
		gameStage.setCurrentTrump(trumpFrame.currentBoundedValue%10);
		gameStage.gamePanel.trickCount = 0;
		if ((myID+currentAuctionCaller)%2 ==0)  // out team got the call
			gameStage.gamePanel.maxTrickCount = 
				trumpFrame.currentBoundedValue/10+6;
		else {  // enemy's team got the call
			gameStage.gamePanel.maxTrickCount = 
				14 - (trumpFrame.currentBoundedValue/10+6);	
		}
		gameStage.repaint();  // for trump has ensured
		
		/*  show game result message  */
		while(round != 0)
			Thread.sleep(1000);
		if (gameStage.gamePanel.trickCount < gameStage.gamePanel.maxTrickCount)
			JOptionPane.showMessageDialog(null, "You Lose!", 
					"Game Result", JOptionPane.WARNING_MESSAGE);
		else
			JOptionPane.showMessageDialog(null, "You Win!", 
					"Game Result", JOptionPane.WARNING_MESSAGE);
		
		/*  wait for restart  */
		while(gameStage.isRestartQuest != true)
			Thread.sleep(1000);
		
		gameStage.isRestartQuest = false;
		toServer.println("Restart");
		toServer.flush();
		
		while (!hasACKedToRestart)  // wait for server's reply
			Thread.sleep(REFRESH_TIME);
		hasACKedToRestart = false;
		
		System.out.println("New Game");
		round = 13;
		gameStage.setVisible(false);
		trumpFrame = null;
		gameStage = null;
		this.gameStart();
	}

	private void ackToServer(){
		toServer.println("ACK");
		toServer.flush();
	}
	
	private void auctionHandling() throws Exception {
		trumpFrame = new TrumpFrame();
		trumpFrame.buttonCall.setEnabled(false);
		trumpFrame.buttonPass.setEnabled(false);
		trumpFrame.setName(name, leftName, frontName, rightName);
		
		trumpFrame.start();
		while (trumpFrame.isFinished != true) {
			refreshTrumpByChangingTurn(Integer.valueOf(trumpStringToRefresh));
			if (isMyTurnToCallTrump) {
				trumpFrame.buttonCall.setEnabled(true);
				trumpFrame.buttonPass.setEnabled(true);
				isMyTurnToCallTrump = false;
			}
			
			if (isToRefreshTrumpRecord) {
				refreshTrumpRecord(currentAuctionPlayer, currentAuctionValue);
				if (currentAuctionValue != 0)
					trumpFrame.currentBoundedValue = currentAuctionValue;
				trumpFrame.repaint();
				isToRefreshTrumpRecord = false;
			}
			
			if (trumpFrame.isTriggeredByCall) {
				toServer.println(String.valueOf(trumpFrame.currentBoundedValue));
				toServer.flush();
				
				trumpFrame.buttonCall.setEnabled(false);
				trumpFrame.buttonPass.setEnabled(false);
				trumpFrame.isTriggeredByCall = false;
			}  else if (trumpFrame.isTriggeredByPass) {
				toServer.println(0);
				toServer.flush();
				
				trumpFrame.buttonCall.setEnabled(false);
				trumpFrame.buttonPass.setEnabled(false);
				trumpFrame.isTriggeredByPass = false;
			}
			
			Thread.sleep(REFRESH_TIME*10);
		}
		
		trumpFrame.setVisible(false);
	}
	
	private void setName(int playerID, String playerName) {
		if (playerID == myID)
			return;
		switch (myID) {
		case 0:
			switch (playerID) {
			case 1:
				rightName = playerName;  break;
			case 2:
				frontName = playerName;  break;
			case 3:
				leftName = playerName;  break;
			}  break;
		case 1:
			switch (playerID) {
			case 0:
				leftName = playerName;  break;
			case 2:
				rightName = playerName;  break;
			case 3:
				frontName = playerName;  break;
			}  break;
		case 2:
			switch (playerID) {
			case 0:
				frontName = playerName;  break;
			case 1:
				leftName = playerName;  break;
			case 3:
				rightName = playerName;  break;
			}  break;
		case 3:
			switch (playerID) {
			case 0:
				rightName = playerName;  break;
			case 1:
				frontName = playerName;  break;
			case 2:
				leftName = playerName;  break;
			}  break;
		}
	}
	
	private void refreshTrumpRecord(int playerID, int value) {
		switch (myID) {
		case 0:
			switch (playerID) {
			case 1:
				trumpFrame.trumpPanel.rightRecords.add(value);  break;
			case 2:
				trumpFrame.trumpPanel.frontRecords.add(value);  break;
			case 3:
				trumpFrame.trumpPanel.leftRecords.add(value);  break;
			}  break;
		case 1:
			switch (playerID) {
			case 0:
				trumpFrame.trumpPanel.leftRecords.add(value);  break;
			case 2:
				trumpFrame.trumpPanel.rightRecords.add(value);  break;
			case 3:
				trumpFrame.trumpPanel.frontRecords.add(value);  break;
			}  break;
		case 2:
			switch (playerID) {
			case 0:
				trumpFrame.trumpPanel.frontRecords.add(value);  break;
			case 1:
				trumpFrame.trumpPanel.leftRecords.add(value);  break;
			case 3:
				trumpFrame.trumpPanel.rightRecords.add(value);  break;
			}  break;
		case 3:
			switch (playerID) {
			case 0:
				trumpFrame.trumpPanel.rightRecords.add(value);  break;
			case 1:
				trumpFrame.trumpPanel.frontRecords.add(value);  break;
			case 2:
				trumpFrame.trumpPanel.leftRecords.add(value);  break;
			}  break;
		}
	}
	
	private void refreshTrumpByChangingTurn(int playerID) {
		if (playerID == myID)
			trumpFrame.trumpPanel.whichPlayerTurn = "I";
		else switch (myID) {
		case 0:
			switch (playerID) {
			case 1:
				trumpFrame.trumpPanel.whichPlayerTurn = "Right";  break;
			case 2:
				trumpFrame.trumpPanel.whichPlayerTurn = "Front";   break;
			case 3:
				trumpFrame.trumpPanel.whichPlayerTurn = "Left";   break;
			}  break;
		case 1:
			switch (playerID) {
			case 0:
				trumpFrame.trumpPanel.whichPlayerTurn = "Left";   break;
			case 2:
				trumpFrame.trumpPanel.whichPlayerTurn = "Right";  break;
			case 3:
				trumpFrame.trumpPanel.whichPlayerTurn = "Front";   break;
			}  break;
		case 2:
			switch (playerID) {
			case 0:
				trumpFrame.trumpPanel.whichPlayerTurn = "Front"; break;
			case 1:
				trumpFrame.trumpPanel.whichPlayerTurn = "Left";  break;
			case 3:
				trumpFrame.trumpPanel.whichPlayerTurn = "Right";   break;
			}  break;
		case 3:
			switch (playerID) {
			case 0:
				trumpFrame.trumpPanel.whichPlayerTurn = "Right";   break;
			case 1:
				trumpFrame.trumpPanel.whichPlayerTurn = "Front";   break;
			case 2:
				trumpFrame.trumpPanel.whichPlayerTurn = "Left";   break;
			}  break;
		}
		
		trumpFrame.repaint();
	}
	
	private void refreshPanelByChangingTurn(int playerID) {
		if (playerID == myID)
			gameStage.gamePanel.whichPlayerTurn = "I";
		else switch (myID) {
		case 0:
			switch (playerID) {
			case 1:
				gameStage.gamePanel.whichPlayerTurn = "Right";  break;
			case 2:
				gameStage.gamePanel.whichPlayerTurn = "Front";   break;
			case 3:
				gameStage.gamePanel.whichPlayerTurn = "Left";   break;
			}  break;
		case 1:
			switch (playerID) {
			case 0:
				gameStage.gamePanel.whichPlayerTurn = "Left";   break;
			case 2:
				gameStage.gamePanel.whichPlayerTurn = "Right";  break;
			case 3:
				gameStage.gamePanel.whichPlayerTurn = "Front";   break;
			}  break;
		case 2:
			switch (playerID) {
			case 0:
				gameStage.gamePanel.whichPlayerTurn = "Front"; break;
			case 1:
				gameStage.gamePanel.whichPlayerTurn = "Left";  break;
			case 3:
				gameStage.gamePanel.whichPlayerTurn = "Right";   break;
			}  break;
		case 3:
			switch (playerID) {
			case 0:
				gameStage.gamePanel.whichPlayerTurn = "Right";   break;
			case 1:
				gameStage.gamePanel.whichPlayerTurn = "Front";   break;
			case 2:
				gameStage.gamePanel.whichPlayerTurn = "Left";   break;
			}  break;
		}
		
		gameStage.repaint();
	}
	
	private void refreshPanelByAddingCard(int playerID, int cardValue) throws Exception {
		Card card = new Card();
		card.color = cardValue/100;
		card.number = cardValue%100;
		card.setImagePath();
		
		if (playerID == myID)
			return;
		switch (myID) {
		case 0:
			switch (playerID) {
			case 1:
				gameStage.gamePanel.rightCardsStartIndex++;
				gameStage.gamePanel.rightImage = ImageIO.read(
						new FileInputStream(new File(card.imagePath)));  break;
			case 2:
				gameStage.gamePanel.frontCardsStartIndex++;
				gameStage.gamePanel.frontImage = ImageIO.read(
						new FileInputStream(new File(card.imagePath)));   break;
			case 3:
				gameStage.gamePanel.leftCardsStartIndex++;
				gameStage.gamePanel.leftImage = ImageIO.read(
						new FileInputStream(new File(card.imagePath)));   break;
			}  break;
		case 1:
			switch (playerID) {
			case 0:
				gameStage.gamePanel.leftCardsStartIndex++;
				gameStage.gamePanel.leftImage = ImageIO.read(
						new FileInputStream(new File(card.imagePath)));   break;
			case 2:
				gameStage.gamePanel.rightCardsStartIndex++;
				gameStage.gamePanel.rightImage = ImageIO.read(
						new FileInputStream(new File(card.imagePath)));  break;
			case 3:
				gameStage.gamePanel.frontCardsStartIndex++;
				gameStage.gamePanel.frontImage = ImageIO.read(
						new FileInputStream(new File(card.imagePath)));   break;
			}  break;
		case 2:
			switch (playerID) {
			case 0:
				gameStage.gamePanel.frontCardsStartIndex++;
				gameStage.gamePanel.frontImage = ImageIO.read(
						new FileInputStream(new File(card.imagePath)));   break;
			case 1:
				gameStage.gamePanel.leftCardsStartIndex++;
				gameStage.gamePanel.leftImage = ImageIO.read(
						new FileInputStream(new File(card.imagePath)));   break;
			case 3:
				gameStage.gamePanel.rightCardsStartIndex++;
				gameStage.gamePanel.rightImage = ImageIO.read(
						new FileInputStream(new File(card.imagePath)));   break;
			}  break;
		case 3:
			switch (playerID) {
			case 0:
				gameStage.gamePanel.rightCardsStartIndex++;
				gameStage.gamePanel.rightImage = ImageIO.read(
						new FileInputStream(new File(card.imagePath)));   break;
			case 1:
				gameStage.gamePanel.frontCardsStartIndex++;
				gameStage.gamePanel.frontImage = ImageIO.read(
						new FileInputStream(new File(card.imagePath)));   break;
			case 2:
				gameStage.gamePanel.leftCardsStartIndex++;
				gameStage.gamePanel.leftImage = ImageIO.read(
						new FileInputStream(new File(card.imagePath)));   break;
			}  break;
		}
		
		gameStage.repaint();
	}
	
	public void close() throws Exception {
		client.close();
	}
	
	/*  This thread is to read input from server  */
	class ClientThread extends Thread {	
		@Override
		public void run() {
			
			
			while (true) {
				try {
					rcvData = fromServer.readLine();
					System.out.println("GET " + rcvData);
					if (rcvData == null)
						throw new NullPointerException();
					
					if (rcvData.equals("SetPlayer")) {
						ackToServer();
					}  else if (rcvData.equals("SetCards")) {
						ackToServer();
						for (int i = 0; i < 13; i++) {
							System.out.print("READY, card " + i + ": ");
							rcvData = fromServer.readLine();
							System.out.print("GET color " + rcvData + " ");
							playerCards[i].color = Integer.valueOf(rcvData);
							ackToServer();
							
							rcvData = fromServer.readLine();
							System.out.println("GET number " + rcvData);
							playerCards[i].number = Integer.valueOf(rcvData);
							ackToServer();
						}
						
						isSetCardsOK = true;
					}  else if (rcvData.equals("RestartOK")) {
						hasACKedToRestart = true;
					}  else if (rcvData.equals("AuctionTurnForWhom")) {
						ackToServer();
						
						rcvData = fromServer.readLine();
						
						if (Integer.valueOf(rcvData) == myID) {
							isMyTurnToCallTrump = true;	
						}
						else
							isMyTurnToCallTrump = false;
						
						//  not repaint here due to inconsistent of GUI
						trumpStringToRefresh = rcvData;
						ackToServer();
					}  else if (rcvData.equals("AuctionCalled")) {
						ackToServer();
						
						/*  ID who has right  */
						rcvData = fromServer.readLine();
						currentAuctionPlayer = Integer.valueOf(rcvData);
						ackToServer();
						
						rcvData = fromServer.readLine();
						currentAuctionValue = Integer.valueOf(rcvData);
						if (currentAuctionValue != 0)
							currentAuctionCaller = currentAuctionPlayer;
						ackToServer();
						
						
						isToRefreshTrumpRecord = true;
						while (isToRefreshTrumpRecord)  // not prepared yet
							Thread.sleep(REFRESH_TIME);

					}  else if (rcvData.equals("AuctionDone")) {
						trumpFrame.isFinished = true;
						ackToServer();
					}  else if (rcvData.equals("TurnToPay")) {
						ackToServer();
						
						rcvData = fromServer.readLine();
						refreshPanelByChangingTurn(Integer.valueOf(rcvData));
						
						if (Integer.valueOf(rcvData) == myID) {
							gameStage.isMyTurn = true;
							ackToServer();
							
							while (gameStage.currentPaidCard == 0)
								Thread.sleep(REFRESH_TIME);
							toServer.println(String.valueOf(gameStage.currentPaidCard));
							toServer.flush();
							gameStage.currentPaidCard = 0;
						}  else if (Integer.valueOf(rcvData) != myID) {
							ackToServer();
						}
					}  else if (rcvData.equals("OtherHasPaid")) {
						ackToServer();
						
						int ID;
						int cardValue;
						rcvData = fromServer.readLine();
						ID = Integer.valueOf(rcvData);
						ackToServer();
						
						rcvData = fromServer.readLine();
						cardValue = Integer.valueOf(rcvData);
						
						if (isFirstTimeInRound) {
							isFirstTimeInRound = false;
							gameStage.currentRoundTrump = cardValue/100;
						}
							
						if (ID != myID)
							refreshPanelByAddingCard(ID, cardValue);
						
						ackToServer();
					}  else if (rcvData.equals("AnnounceResult")) {
						ackToServer();
						Thread.sleep(1000);
						
						rcvData = fromServer.readLine();
						if (Integer.valueOf(rcvData) == myID || 
								Integer.valueOf(rcvData) == (myID+2)%4)
							gameStage.incTrickCount();
						
						round--;
						isFirstTimeInRound = true;
						gameStage.currentRoundTrump = 0;
						gameStage.clearPanelImage();
						gameStage.repaint();
						ackToServer();
					}  else if (rcvData.equals("RestartButtonEnabled")) {
						gameStage.setRestartButtonEnabled(true);
						gameStage.repaint();
						ackToServer();
					}
					
					Thread.sleep(REFRESH_TIME);
				}  catch (NullPointerException ex) {
					JOptionPane.showMessageDialog(null, 
							"Disconnected From Server (NullPointerException)", 
							"Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
					System.exit(0);
				}   catch (SocketException ex) {
					JOptionPane.showMessageDialog(null, 
							"Disconnected From Server (SocketException)", 
							"Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
					System.exit(0);
				}  catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(null, 
							"Disconnected From Server (NumberFormatException)", 
							"Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
					System.exit(0);
				}  catch (Exception ex) {
					JOptionPane.showMessageDialog(null, 
							"Disconnected From Server (UnknownReasons)", 
							"Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
					System.exit(0);
				}
				
			}  // end while
		}  // end run
	}

}
