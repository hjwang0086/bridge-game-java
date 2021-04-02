/*
 * This class is the Server side.
 * 
 * Instead of DataOutputStream as toServer, use PrintWriter,
 * because the DataOutputStream would send message to clients unstoppable, 
 * while PrintWriter's "flush" function causes the data would only be sent once,
 * which is the model's desire.
 * 
 * After GameStart, data transfer would use ACK,
 * which means it costs one round-trip-time for each communication.
 * 
 * For paidCard[i] digits' meaning: 4 digits ABCD
 * A = is currentGameTrump or not
 * B = card color
 * CD = card number
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;


public class Server {
	private static final int MAX_CLIENT_NUM = 4;
	private static final int REFRESH_TIME = 10;
	private static final int PORT_NUM = 8383;
	
	private ArrayList<Socket> clientList = new ArrayList<Socket>();
	private ArrayList<String> clientNameList = new ArrayList<String>();
	private ArrayList<BufferedReader> fromClientList = new ArrayList<BufferedReader>();
	private ArrayList<PrintWriter> toClientList = new ArrayList<PrintWriter>();
	private BufferedReader fromClient;
	private Card[] cardsFromDeck;
	private Player[] clientPlayer;
	private PrintWriter toClient;
	private Socket currentLoginClient;
	private ServerSocket server;
	private String buffer = "";
	private String rcvData = "";
	private int currentAuctionValue;
	private int currentClientCount;
	private int currentFirstPlayerID;  // the player enables to pay card
	private int currentGameTrump;  // fixed every "game"
	private int currentRoundTrump;  // fixed every "round"
	private int passCount;  // client's pass count
	private int round = 13;
	private int[] paidCard = new int[4];  // player's paid card fixed every round
	private boolean isAuctionDone = false;
	
	public Server() throws Exception {
		//ServerThread listenerThread = new ServerThread();
		
		try {
			server = new ServerSocket(PORT_NUM);
		} catch (IOException ex) {
			System.out.println("Socket Fail");
			System.exit(0);
		}
		System.out.println("Socket Builded, Port " + server.getLocalPort());
		
		while (true) {
			currentLoginClient = server.accept();
			currentClientCount++;
			
			fromClient = new BufferedReader
					(new InputStreamReader(currentLoginClient.getInputStream()));
			toClient = new PrintWriter
					(new OutputStreamWriter(currentLoginClient.getOutputStream()));
			
			clientNameList.add(fromClient.readLine());
			clientList.add(currentLoginClient);
			fromClientList.add(fromClient);
			toClientList.add(toClient);
			
			System.out.println("Client #" + currentClientCount 
					+ " Login, Name = " + clientNameList.get(currentClientCount-1));
			
			toClient.println(currentClientCount-1);  // give ID to client
			toClient.flush();
			while (!rcvData.equals("ACK"))
				rcvData = fromClient.readLine();
			rcvData = "";
			
			if (currentClientCount >= MAX_CLIENT_NUM)  break;
		}
		
		System.out.println("broadcast SetNameAll");
		broadcast("SetNameAll");
		waitForACKs();
		for (int i = 0; i < currentClientCount; i++) {
			broadcast(clientNameList.get(i));
			waitForACKs();
		}
		
		System.out.println("broadcast GameStart");
		broadcast("GameStart");
		
		//listenerThread.start();
		
	}
	
	public void gameStart() throws Exception {
		clientPlayer = new Player[currentClientCount];
		
		initCardsFromDeck();
		shuffle(cardsFromDeck);
		
		giveCardsToPlayers();
		setPlayersToClients();
		
		auctionHandling();
		
		/*  gameStage  */
		while (round != 0) {
			/*  get card from clients  */
			for (int i = 0; i < currentClientCount; i++) {  // "i" is shift value
				int currentPlayerID = (currentFirstPlayerID+i)%currentClientCount;
				broadcast("TurnToPay");
				waitForACKs();
				broadcast(String.valueOf(currentPlayerID));
				waitForACKs();
				
				rcvData = fromClientList.get(currentPlayerID).readLine();
				paidCard[currentPlayerID] = Integer.valueOf(rcvData);
				
				System.out.println("ID = " + currentPlayerID + ", original paidCard = "
						+ paidCard[currentPlayerID]);
				
				/*  refresh client's panel */
				broadcast("OtherHasPaid");
				waitForACKs();
				broadcast(String.valueOf(currentPlayerID));
				waitForACKs();
				broadcast(String.valueOf(paidCard[currentPlayerID]));
				waitForACKs();
				
				if (i == 0)  // first time paying
					currentRoundTrump = paidCard[currentPlayerID]/100;

				System.out.println("CurrentGameTrump = " + currentGameTrump + 
						", currentRoundTrump = " + currentRoundTrump);
				
				if (paidCard[currentPlayerID]/100 == currentGameTrump)
					paidCard[currentPlayerID] += 1000;
				else if (paidCard[currentPlayerID]/100 != currentRoundTrump)
					paidCard[currentPlayerID] = 0;
				
				System.out.println("ID = " + currentPlayerID + ", fixed paidCard = "
						+ paidCard[currentPlayerID]);
			}
			
			System.out.println("JUDGE");
			for (int i = 0; i < currentClientCount; i++) {
				System.out.println(i + ": " + paidCard[i]);
			}
			
			/*  judge cards  */
			int maxID = 0;
			for (int i = 1; i < currentClientCount; i++) {
				if (paidCard[i] > paidCard[maxID]) {
					System.out.println("padiCard[" + (i) + "] = " + paidCard[i]
							 + " > paidCard[" + (maxID) + "] = " + paidCard[i-1]);
					maxID = i;
				}
			}
			currentFirstPlayerID = maxID;  // set for next round
			broadcast("AnnounceResult");
			waitForACKs();
			broadcast(String.valueOf(maxID));
			waitForACKs();
			
			/*  reset  */
			for (int i = 0; i < currentClientCount; i++)
				paidCard[i] = 0;
			round--;
		}  // end while gameStage is running
		
		broadcast("RestartButtonEnabled");
		waitForACKs();

		/*  wait for restart  */
		for (int i = 0; i < currentClientCount; i++) {
			while (!rcvData.equals("Restart")) {
				rcvData = fromClientList.get(i).readLine();
			}
			rcvData = "";
		}
		broadcast("RestartOK");
		
		System.out.println("New Game");
		round = 13;
		isAuctionDone = false;
		this.gameStart();
			
	}
	
	private void broadcast(String s) throws IOException {
		for (int i = 0; i < currentClientCount; i++) {
			toClientList.get(i).println(s);
			toClientList.get(i).flush();
		}
	}
	
	private void waitForACKs() throws IOException, InterruptedException {
		for (int i = 0; i < currentClientCount; i++) {
			rcvData = "";
			while (!rcvData.equals("ACK")) {
				rcvData = fromClientList.get(i).readLine();
				Thread.sleep(REFRESH_TIME );
			}
		}
		
	}
	
	private void setPlayersToClients() throws Exception {
		System.out.println("broadcast SetPlayer");
		broadcast("SetPlayer");
		waitForACKs();
		
		/*  send cards' message  */
		System.out.println("broadcast SetCards");
		broadcast("SetCards");
		waitForACKs();
		
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < currentClientCount; j++) {
				buffer = String.valueOf(clientPlayer[j].cards[i].color);
				toClientList.get(j).println(buffer);
				toClientList.get(j).flush();
			}
			waitForACKs();
			
			for (int j = 0; j < currentClientCount; j++) {		
				buffer = String.valueOf(clientPlayer[j].cards[i].number);
				toClientList.get(j).println(buffer);
				toClientList.get(j).flush();
			}
			waitForACKs();
		}
		
	}
	

	private void initCardsFromDeck() {
		cardsFromDeck = new Card[52];
		int k = 0;
		
		for (int i = 1; i <= 4; i++) {
			for (int j = 2; j <= 14; j++) {		
				cardsFromDeck[k] = new Card();
				cardsFromDeck[k].color = i;
				cardsFromDeck[k].number = j;
				k++;
			}
		}
	}
	
	private void shuffle(Card[] c) {
		Random random = new Random();
		Card temp;
		int index;
		
		for (int i = 0; i < c.length; i++) {
			index = random.nextInt(c.length);
			temp = c[index];
			c[index] = c[i];
			c[i] = temp;
		}
	}
	
	private void giveCardsToPlayers() {
		for (int i = 0; i < clientPlayer.length; i++) {
			clientPlayer[i] = new Player();
			clientPlayer[i].cards = new Card[13];
			
			for (int j = 0; j < 13; j++) {  // initialize
				clientPlayer[i].cards[j] = new Card();
				clientPlayer[i].cards[j] = cardsFromDeck[i*13+j];
			}
		}
	}
	
	private void auctionHandling() throws Exception {
		Random random = new Random();
		int firstCallerID = random.nextInt(currentClientCount);
		boolean isFirstNotPass = false;  // avoid everybody pass
		
		while (isAuctionDone != true) {
			for (int i = firstCallerID; i < currentClientCount; i++) {
				/*  give the specified client the right to modify  */
				broadcast("AuctionTurnForWhom");
				waitForACKs();
				broadcast(String.valueOf(i));
				waitForACKs();
				
				rcvData = fromClientList.get(i).readLine();
				System.out.println("GET " + rcvData + " from " + clientNameList.get(i));
				currentAuctionValue = Integer.valueOf(rcvData);
				if (currentAuctionValue != 0) {
					currentGameTrump = currentAuctionValue%10;
					currentFirstPlayerID = (i+1)%currentClientCount;
					isFirstNotPass = true;
				}
				
				if (Integer.valueOf(rcvData) == 0 && isFirstNotPass)
					passCount++;
				else passCount = 0;
				
				System.out.println("broadcast AuctionCalled");
				broadcast("AuctionCalled");
				waitForACKs();
				
				broadcast(String.valueOf(i));  // who
				waitForACKs();
				
				broadcast(String.valueOf(currentAuctionValue));  // what value
				waitForACKs();
				
				if (passCount >= currentClientCount-1) {
					System.out.println("broadcast AuctionDone");
					broadcast("AuctionDone");
					waitForACKs();
					
					isAuctionDone = true;
					break;
				}
			}
			firstCallerID = 0;
		}
	}

	/*  This thread is to refresh every player's panel each round  */
	class ServerThread extends Thread {
		
		@Override
		public void run() {
			while (true) {
				for (int i = 0; i < currentClientCount; i++) {
					try {
						rcvData = fromClientList.get(i).readLine(); 
						System.out.println("GET " + rcvData);
					
						Thread.sleep(REFRESH_TIME);
					}  catch (Exception ex) {
						ex.printStackTrace();
						System.exit(0);
					}
				}  // end for
				
				try {
					Thread.sleep(REFRESH_TIME*100);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
					System.exit(0);
				}
				
			}  // end while
		}
	}

}
