/*
 * This class is the data structure of a card
 */


public class Card {
	public int color;
	public int number;
	public String imagePath;
	
	public Card() {
		imagePath = new String();
	}
	
	public void setImagePath() {
		imagePath = "./res/cards";
		
		switch (this.color) {
			case 1:
				imagePath  += "/clubs/club";  break;
			case 2:
				imagePath  += "/diamonds/diamond";  break;
			case 3:
				imagePath  += "/hearts/heart";  break;
			case 4:
				imagePath += "/spades/spade";
		}
		
		imagePath  += String.valueOf(this.number);
		imagePath  += ".jpg";
	}
}
