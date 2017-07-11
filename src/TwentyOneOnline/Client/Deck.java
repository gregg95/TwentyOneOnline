package TwentyOneOnline.Client;


public class Deck {

    private Card[] cards_array = new Card[52];

    public Deck(){
        refillDeck();
    }

    public final void refillDeck(){
        int i = 0;
        for (Card.CardType cardType: Card.CardType.values()){
            for (Card.CardNumber cardNumber: Card.CardNumber.values()){
                cards_array[i++] = new Card(cardType, cardNumber);
            }
        }
    }

    public Card createAnCard(int index){
        Card card;
        card = cards_array[index];
        return card;
    }
}
