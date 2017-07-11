package TwentyOneOnline.Client;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class Card extends Parent{

    private static final int CARD_WIDTH = 100;
    private static final int CARD_HEIGHT = 140;

    enum CardType {
        CLUB, DIAMOND, HeART, SPADE;

        final Image image;

        CardType() {
            this.image = new Image(Card.class.getResourceAsStream("types/".concat(name().toLowerCase()).concat(".png")),
                    32, 32, true, true);
        }
    }

    enum CardNumber {
        TWO(2), TREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10),
        JACK(2), QUEEN(3), KING(4), AS(11);

        final int cardValue;

        CardNumber(int cardValue){
            this.cardValue=cardValue;
        }

        String addCardName() {
            return ordinal() < 9 ? String.valueOf(cardValue) : name().substring(0, 1);
        }

    }

    public final CardType cardType;
    public final CardNumber cardNumber;
    public final int cardValue;

    public Card(CardType cardType, CardNumber cardNumber){
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.cardValue = cardNumber.cardValue;

        Rectangle cardBackground =  new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        cardBackground.setArcWidth(20);
        cardBackground.setArcHeight(20);
        cardBackground.setFill(Color.WHITE);

        Text text1 = new Text(cardNumber.addCardName());
        text1.setFont(Font.font(18));
        text1.setX(CARD_HEIGHT - text1.getLayoutBounds().getWidth() - 50);
        text1.setY(text1.getLayoutBounds().getHeight());

        Text text2 = new Text(text1.getText());
        text2.setFont(Font.font(18));
        text2.setX(10);
        text2.setY(CARD_WIDTH + 30);

        ImageView view = new ImageView(cardType.image);
        view.setRotate(180);
        view.setX(CARD_WIDTH - 32);
        view.setY(CARD_HEIGHT - 32);

        getChildren().addAll(cardBackground, new ImageView(cardType.image), view, text1, text2);

    }


    @Override
    public String toString(){
        return cardNumber.toString() + " of " + cardType.toString();
    }


}
