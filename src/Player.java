import java.util.Arrays;

/*
 * This is the class for handling a single player's status.
 * 
 * Notice that 'A' represents #14,
 * so #1 does not use, except "isCaseBase1".
 * 
 * Declaring variable cardValue has two reasons:
 * 1. Original card sequence of player won't be replaced.
 * 2. Arrays.sort(), using integer array is more convenient.
 */


public class Player {
	public Card[] cards;
	
	public Player() {
		cards = new Card[13];
		
		for (int i = 0; i < 13; i++)
			cards[i] = new Card();
	}
	
	public Player(Card[] cardsIn) {
		cards = new Card[cardsIn.length];
		
		for (int i = 0; i < cardsIn.length; i++)
			cards[i] = new Card();
		
		cards = cardsIn;
	}
	
	public void sortCardsOrder() {
		int[] cardValue = new int[13];
		Card[] sortedCards = new Card[13];
			
		for (int i = 0; i < 13; i++) 
			cardValue[i] = cards[i].color*100 + cards[i].number;
			
		Arrays.sort(cardValue);
		
		for (int i = 0; i < 13; i++) {
			sortedCards[i] = new Card();
			sortedCards[i].color = cardValue[i]/100;
			sortedCards[i].number = cardValue[i]%100;
		}
		
		cards = sortedCards;
	}
	/*  used when needed
	 * private String getStringWithJQKA(int value) {
		String s = new String();
		switch (value) {
			case 1:
				s = "A";  break;
			case 11:
				s = "J";  break;
			case 12:
				s = "Q";  break;
			case 13:
				s = "K";  break;
			case 14:
				s = "A";  break;
			default:
				s = String.valueOf(value);
		}
		
		return s;
	}*/
}