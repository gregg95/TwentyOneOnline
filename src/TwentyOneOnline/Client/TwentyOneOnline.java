package TwentyOneOnline.Client;

import TwentyOneOnline.Protocol.Protocol;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.*;


import static com.sun.javafx.application.PlatformImpl.tkExit;

public class TwentyOneOnline extends Application {



    private Text txtInformation = new Text();
    private SimpleBooleanProperty gameStatus = new SimpleBooleanProperty(false);
    private HBox hbOpponentCards = new HBox(20);
    private HBox hbPlayerCards = new HBox(20);
    private int pool = 0;
    private int bid = 0;
    private int coins = 0;
    private Text txtMoney = new Text("Money: " + coins);
    private Text txtPool = new Text("Pool: " + pool);
    private Text txtBid = new Text("Bid: " + bid);
    private Button btnBet5 = new Button("5");
    private Button btnBet10 = new Button("10");
    private Button btnBet25 = new Button("25");
    private Button btnBet100 = new Button("100");
    private Button btnBet250 = new Button("250");
    private Button btnBet500 = new Button("500");
    private Button btnBet = new Button("Bet");
    private Button btnDeal = new Button("Deal");
    private boolean bankerRole = false;
    private int roundCounter = 0;
    private int playerPoints = 0;
    private int opponentPoints = 0;
    private Socket socket;
    private Button btnConnect, btnDisconnect;
    private TextField txtHost, txtPort;
    private String serverName = "localhost";
    private int portNumber = 2345;
    private boolean connected = false;
    private Text txtOpponentPoints = new Text();
    private Text txtPlayerPoints = new Text();
    private Klient clientThread;
    private String opponentNick = "";
    private String playerNick = "";
    private int[] cardID = new int[52];
    private int opponentCard = 0;
    private ArrayList < Integer > playerRolledCards = new ArrayList < > ();
    private Button btnTwoRounds = new Button("Two rounds");
    private int twoRoundsCount = 3;
    private boolean isTwoRoundsStarded = false;
    private Stage stage = new Stage();
    private Button btnTakeCard = new Button("Take card");
    private Button btnStand = new Button("Stand");



    private void uzupelnijTabliceNumerow() {
        for (int i = 0; i < 52; i++) {
            cardID[i] = i;
        }
    }


    public Parent utworzPanelGry() {

        hbOpponentCards.setPrefHeight(150);
        hbPlayerCards.setPrefHeight(150);

        Platform.setImplicitExit(false);

        txtInformation.setText("Welcome! To start the game connect to the server.");



        btnDeal.setDisable(true);
        btnBet500.setDisable(true);
        btnBet250.setDisable(true);
        btnBet100.setDisable(true);
        btnBet25.setDisable(true);
        btnBet10.setDisable(true);
        btnBet5.setDisable(true);
        btnBet.setDisable(true);
        btnTakeCard.setDisable(true);
        btnStand.setDisable(true);
        btnTwoRounds.setDisable(true);


        txtInformation.setStyle("-fx-font-size: 16;");
        txtPlayerPoints.setStyle("-fx-font-size: 13");
        txtOpponentPoints.setStyle("-fx-font-size: 13");
        Pane root = new Pane();
        root.setPrefSize(1150, 650);

        Region background = new Region();
        background.setPrefSize(1150, 650);
        background.setStyle("-fx-background-color: rgba(0, 0, 0, 1)");


        HBox rootLayout = new HBox(5);
        rootLayout.setPadding(new Insets(5, 5, 5, 5));

        //Panel u góry aplikacji
        Rectangle backgroundCenter = new Rectangle(1130, 460);
        backgroundCenter.setArcWidth(50);
        backgroundCenter.setArcHeight(50);
        backgroundCenter.setFill(Color.GREEN);

        VBox topVBox = new VBox(10);
        topVBox.setPadding(new Insets(10));
        topVBox.setAlignment(Pos.TOP_CENTER);
        topVBox.getChildren().addAll(txtOpponentPoints, hbOpponentCards, txtInformation, hbPlayerCards, txtPlayerPoints);

        //Panel na dole aplikacji
        Rectangle backgroundBottom = new Rectangle(1130, 100);
        backgroundBottom.setArcWidth(50);
        backgroundBottom.setArcHeight(50);
        backgroundBottom.setFill(Color.ORANGE);

        HBox bottomHBox = new HBox(20);
        bottomHBox.setAlignment(Pos.CENTER_LEFT);


        HBox przyciskiRozgrywki = new HBox(15, btnDeal, btnBet, btnTakeCard, btnStand, btnTwoRounds);
        przyciskiRozgrywki.setAlignment(Pos.CENTER_RIGHT);

        HBox informacje = new HBox(15, txtPool, txtBid, txtMoney);
        informacje.setPadding(new Insets(10));

        HBox stawkiHBox = new HBox(btnBet5, btnBet10, btnBet25, btnBet100, btnBet250, btnBet500);
        stawkiHBox.setPadding(new Insets(10));
        bottomHBox.getChildren().addAll(stawkiHBox, przyciskiRozgrywki, informacje);

        rootLayout.getChildren().addAll(new BorderPane(new StackPane(backgroundCenter, topVBox), null, null, new StackPane(backgroundBottom, bottomHBox), null));
        root.getChildren().addAll(background, rootLayout);

        return root;
    }

    public Parent utworzOkno() throws IOException {

        HBox laczenieHBox = new HBox(5);
        laczenieHBox.setPadding(new Insets(10));

        txtHost = new TextField();
        txtHost.setText(serverName);
        txtPort = new TextField((new Integer(portNumber)).toString());
        btnConnect = new Button("Connect");
        btnDisconnect = new Button("Disconnect");
        btnDisconnect.setDisable(true);

        laczenieHBox.getChildren().addAll(txtHost, txtPort, btnConnect, btnDisconnect);

        btnConnect.setOnAction(event -> {
            try {
                socket = new Socket(serverName, portNumber);
                connected = true;
                btnConnect.setDisable(true);
                btnDisconnect.setDisable(false);
                txtHost.setDisable(true);
                txtPort.setDisable(true);
                clientThread = new Klient(socket);
                clientThread.start();
            } catch (ConnectException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to connect.", ButtonType.OK);
                alert.showAndWait();
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to connect.", ButtonType.OK);
                alert.showAndWait();

            }


        });



        Pane root = new Pane();
        Region background = new Region();
        background.setPrefSize(1145, 650);
        background.setStyle("-fx-background-color: rgba(0, 0, 0, 1)");
        HBox rootLayout = new HBox(5);
        rootLayout.setPadding(new Insets(5, 5, 5, 5));
        rootLayout.getChildren().addAll(new BorderPane(utworzPanelGry(), laczenieHBox, null, null, null));
        root.getChildren().addAll(rootLayout);

        root.setStyle("-fx-background-color: #000000");
        return root;
    }




    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        stage.setScene(new Scene(utworzOkno()));
        stage.setWidth(1150);
        stage.setHeight(650);
        stage.setResizable(false);
        stage.setTitle("21Online");
        stage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }

    private class Klient extends Thread {

        private Scanner in ;
        private PrintWriter out;
        private String line;
        private Socket socket;
        Deck deck = new Deck();

        public Klient(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            stage.setOnCloseRequest(new EventHandler < WindowEvent > () {
                @Override
                public void handle(WindowEvent event) {
                    Platform.setImplicitExit(false);
                    send(Protocol.C32_LOGOUT, "Opponent has left the game.");
                    System.exit(0);
                    Platform.exit();
                    tkExit();
                    Thread.currentThread().interrupt();
                }
            });

            btnDisconnect.setOnAction(event -> {
                connected = false;

                btnDisconnect.setDisable(true);
                btnConnect.setDisable(false);
                txtHost.setDisable(false);
                txtPort.setDisable(false);
                // System.exit(0);

                turnOffBetButtons();
                btnTwoRounds.setDisable(true);
                btnTakeCard.setDisable(true);
                btnBet.setDisable(true);
                btnStand.setDisable(true);
                btnDeal.setDisable(true);

                roundCounter = 0;
                txtInformation.setText("Disconnected");


                send(Protocol.C28_ADD_MONEY_OPPONENT, pool + bid);

                resetAllNumbers();

                txtOpponentPoints.setText("- - - - - - - -");

                txtPlayerPoints.setText("- - - - - - - -");

                send(Protocol.C32_LOGOUT, "Opponent has left the game. ");

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        hbOpponentCards.getChildren().clear();
                        hbPlayerCards.getChildren().clear();

                    }
                });


            });

            btnBet5.setOnAction(event -> {
                btnBet.setDisable(false);
                bet(5);
            });

            btnBet10.setOnAction(event -> {
                btnBet.setDisable(false);
                bet(10);
            });

            btnBet25.setOnAction(event -> {
                btnBet.setDisable(false);
                bet(25);
            });

            btnBet100.setOnAction(event -> {
                btnBet.setDisable(false);
                bet(100);
            });

            btnBet250.setOnAction(event -> {
                btnBet.setDisable(false);
                bet(250);
            });

            btnBet500.setOnAction(event -> {
                btnBet.setDisable(false);
                bet(500);
            });

            btnBet.setOnAction(event -> {

                if (bankerRole) {
                    btnBet.setDisable(true);
                    turnOffBetButtons();

                    txtInformation.setText("Opponent is setting and bid up.");
                    send(Protocol.C10_INFORM_OPPONENT_ABOUT_SET_BID);

                } else {
                    roundCounter++;

                    send(Protocol.C12_INFORM_OPPONENT_ABOUT_ACCEPTED_BID);

                    turnOffBetButtons();
                    btnBet.setDisable(true);
                    btnTakeCard.setDisable(false);
                    btnStand.setDisable(false);

                    txtInformation.setText("Ok! Now draw cards and stand.");
                }

            });

            btnTwoRounds.setOnAction(event -> {
                txtInformation.setText(txtInformation.getText() + "\nOpponent was informed about two deals.");
                isTwoRoundsStarded = true;
                twoRoundsCount = 3;

                send(Protocol.C30_TWO_DEALS, twoRoundsCount);


                btnTwoRounds.setDisable(true);
            });

            btnTakeCard.setOnAction(event -> {
                send(Protocol.C15_TAKE_AN_CARD, playerNick);
                send(Protocol.C14_CHECK_POINTS, playerPoints);
            });



            btnStand.setOnAction(event -> {

                if (bankerRole) {
                    btnTakeCard.setDisable(true);
                    btnStand.setDisable(true);

                    send(Protocol.C26_CARDS_AT_TABLE);

                } else {

                    btnTakeCard.setDisable(true);
                    btnStand.setDisable(true);
                    txtInformation.setText("Now opponent is drawing cards.");

                    send(Protocol.C21_LET_TO_TAKE_CARD_BANKER);
                }

            });



            btnDeal.setOnAction(event -> {
                playerRolledCards.clear();

                if (bankerRole) {
                    if (roundCounter == 0) {
                        roundCounter++;
                        txtInformation.setText("New game. Set an pool up.");

                        deck.refillDeck();
                        uzupelnijTabliceNumerow();

                        checkPocket();

                        send(Protocol.C8_DEAL_ONE_CARD_TO_EACH_PLAYER);

                        btnDeal.setDisable(true);

                    } else {
                        roundCounter++;

                        if (pool > 0) {
                            txtInformation.setText("Opponent is setting an bid up. ");

                            deck.refillDeck();
                            uzupelnijTabliceNumerow();

                            send(Protocol.C8_DEAL_ONE_CARD_TO_EACH_PLAYER);
                            send(Protocol.C10_INFORM_OPPONENT_ABOUT_SET_BID);

                            btnDeal.setDisable(true);
                        } else {


                        }
                    }
                }
            });



            try { in = new Scanner(socket.getInputStream());
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                send(Protocol.C1_LOGIN);


                while (connected) {
                    if (in .hasNextLine()) {

                        line = in.nextLine();

                        Protocol command = Protocol.valueOf(line.split(";")[0]);

                        switch (command) {
                            case C1_LOGIN:
                                playerNick = JOptionPane.showInputDialog(null, getParameteres(line));
                                send(Protocol.C2_SET_NICK, playerNick);
                                txtPlayerPoints.setText(playerNick + ": ");
                                break;

                            case C3_SYNCHRINIZE_NICKS:
                                opponentNick = getParameteres(line);
                                txtOpponentPoints.setText(opponentNick + ": ");
                                break;

                            case C4_SET_INITIAL_ROLE:
                                if (getParameteres(line).equals("banker")) {
                                    txtInformation.setText("Starting game! You are an banker!");
                                    bankerRole = true;
                                    btnDeal.setDisable(false);
                                    btnTwoRounds.setDisable(false);
                                } else {
                                    txtInformation.setText("Starting game! Your opponent is an banker and he will deal cards.");
                                    bankerRole = false;
                                }
                                break;

                            case C5_COINS_UPDATE:
                                coins = Integer.parseInt(getParameteres(line));
                                txtMoney.setText("Money: " + coins);
                                break;

                            case C6_POOL_UPDATE:
                                pool = Integer.parseInt(getParameteres(line));
                                txtPool.setText("Pool: " + pool);
                                break;

                            case C7_BID_UPDATE:
                                bid = Integer.parseInt(getParameteres(line));
                                txtBid.setText("Bid: " + bid);
                                break;

                            case C9_GET_DEALED_CARDS:
                                String[] cards = getParameteres(line).split(",");

                                playerPoints = 0;
                                opponentPoints = 0;

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        hbPlayerCards.getChildren().clear();
                                        playerRolledCards.add(Integer.valueOf(cards[0]));
                                        hbPlayerCards.getChildren().add(deck.createAnCard(Integer.valueOf(cards[0])));

                                        hbOpponentCards.getChildren().clear();
                                        hbOpponentCards.getChildren().add(deck.createAnCard(Integer.valueOf(cards[1])));
                                    }
                                });

                                playerPoints += deck.createAnCard(Integer.valueOf(cards[0])).cardValue;
                                updatePlayerPoints(playerPoints);

                                opponentPoints += deck.createAnCard(Integer.valueOf(cards[1])).cardValue;
                                updateOpponentPoints(opponentPoints);


                                if (!bankerRole) {
                                    if (roundCounter == 0) {
                                        txtInformation.setText("Your opponent is setting an pool.");
                                    } else {
                                        txtInformation.setText("Set an bid.");
                                    }
                                }
                                break;

                            case C11_SET_BID:
                                txtInformation.setText("Cards dealed. Now set an bid.");
                                checkIfPlayerCanUseThisNumber();
                                break;

                            case C13_BID_ACCEPTED:
                                txtInformation.setText("Opponent did accept an bid and now is drawing cards.");
                                break;

                            case C16_SHOW_ROLLED_CARD:
                                int n = Integer.parseInt(getParameteres(line));

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        System.out.println("Me " + playerNick + " got nr " + n + " which is: " + deck.createAnCard(n).toString());
                                        playerRolledCards.add(n);
                                        hbPlayerCards.getChildren().add(deck.createAnCard(n));
                                    }
                                });

                                playerPoints += deck.createAnCard(n).cardValue;
                                updatePlayerPoints(playerPoints);

                                send(Protocol.C14_CHECK_POINTS, playerPoints);
                                break;

                            case C17_SHOW_GREYED_CARD:

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        hbOpponentCards.getChildren().add(zaslonKarte());
                                    }
                                });
                                break;

                            case C18_LOSS:
                                if (bankerRole) {

                                    txtInformation.setText("LOST! Bid id deducted from the pool.");

                                    pool -= bid;

                                    send(Protocol.C6_POOL_UPDATE, pool);
                                    send(Protocol.C28_ADD_MONEY_OPPONENT, (bid * 2));
                                    bid = 0;
                                    send(Protocol.C7_BID_UPDATE, bid);

                                    checkPool();
                                    btnTakeCard.setDisable(true);
                                    btnStand.setDisable(true);
                                    btnDeal.setDisable(false);

                                    playerRolledCards.clear();

                                    twoDeals();
                                } else {

                                    txtInformation.setText("LOST! Bid is added to pool\n" +
                                            "Banker is drawing cards.");

                                    pool += bid;
                                    bid = 0;
                                    send(Protocol.C6_POOL_UPDATE, pool);
                                    send(Protocol.C7_BID_UPDATE, bid);
                                    btnTakeCard.setDisable(true);
                                    btnStand.setDisable(true);
                                    playerRolledCards.clear();
                                }
                                break;

                            case C19_POINT:
                                if (bankerRole) {

                                    txtInformation.setText("You hit 21! Bid is added to pool.\n" +
                                            "Start an new deal.");

                                    pool += bid;
                                    send(Protocol.C6_POOL_UPDATE, pool);
                                    bid = 0;
                                    send(Protocol.C7_BID_UPDATE, bid);


                                    playerRolledCards.clear();

                                    btnTakeCard.setDisable(true);
                                    btnStand.setDisable(true);
                                    btnDeal.setDisable(false);

                                    twoDeals();
                                } else {
                                    txtInformation.setText("You hit 21! You win double of bid.");

                                    playerRolledCards.clear();
                                    coins += 2 * bid;
                                    pool -= bid;
                                    bid = 0;

                                    checkPool();
                                    send(Protocol.C5_COINS_UPDATE, coins);
                                    send(Protocol.C6_POOL_UPDATE, pool);
                                    send(Protocol.C7_BID_UPDATE, bid);
                                    btnTakeCard.setDisable(true);
                                    btnStand.setDisable(true);

                                }

                                break;

                            case C20_SHOW_ALL_CARDS:

                                if (bankerRole) {
                                    String s = getParameteres(line);

                                    String[] parts = s.split(";");

                                    String on = parts[0].substring(1, parts[0].length() - 1).replaceAll("\\s+", "");

                                    if (parts[1].equals("fura")) {
                                        txtInformation.setText("Opponent Lost. Bid is added to pool. \n" +
                                                "Start new deal.");
                                    } else {
                                        txtInformation.setText("Opponent hit 21. Equivalent of bis is lost form pool. \n" +
                                                "Start new deal.");
                                    }

                                    String[] cardsNumbers = on.split(",");


                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            hbOpponentCards.getChildren().clear();
                                            opponentPoints = 0;

                                            for (String i: cardsNumbers) {
                                                hbOpponentCards.getChildren().addAll(deck.createAnCard(Integer.parseInt(i)));
                                                opponentPoints += deck.createAnCard(Integer.parseInt(i)).cardValue;
                                            }

                                            updateOpponentPoints(opponentPoints);
                                        }
                                    });

                                    btnDeal.setDisable(false);
                                    btnTakeCard.setDisable(true);
                                    btnStand.setDisable(true);
                                } else {
                                    String s = getParameteres(line);

                                    String[] parts = s.split(";");


                                    String on = parts[0].substring(1, parts[0].length() - 1).replaceAll("\\s+", "");

                                    if (parts[1].equals("fura")) {
                                        txtInformation.setText("Opponent lost. You win double of bid. \n" +
                                                "Opponent is dealing cards.");
                                    } else {
                                        txtInformation.setText("Opponent hit 21. Bid is added to pool. \n" +
                                                "Opponent is dealing cards.");
                                    }

                                    String[] cardsNumbers = on.split(",");


                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            hbOpponentCards.getChildren().clear();
                                            opponentPoints = 0;

                                            for (String i: cardsNumbers) {
                                                hbOpponentCards.getChildren().addAll(deck.createAnCard(Integer.parseInt(i)));
                                                opponentPoints += deck.createAnCard(Integer.parseInt(i)).cardValue;
                                            }

                                            updateOpponentPoints(opponentPoints);
                                        }
                                    });


                                }

                                break;

                            case C22_TAKE_CARD_BANKER:

                                txtInformation.setText("Opponent is ready to show cards. Do you want to take card or stay?");
                                btnTakeCard.setDisable(false);
                                btnStand.setDisable(false);

                                break;

                            case C24_WIN:
                                if (bankerRole) {
                                    txtInformation.setText("Win! Bid is added to the pool." +
                                            "\nStart new deal.");
                                    pool += bid;
                                    send(Protocol.C6_POOL_UPDATE, pool);
                                    bid = 0;
                                    send(Protocol.C7_BID_UPDATE, bid);

                                    twoDeals();

                                } else {
                                    txtInformation.setText("Win! You win double of bid." +
                                            "\nOpponent is dealing cards.");
                                    coins += bid * 2;
                                    send(Protocol.C5_COINS_UPDATE, coins);

                                    pool -= bid;
                                    send(Protocol.C6_POOL_UPDATE, pool);
                                    bid = 0;
                                    send(Protocol.C7_BID_UPDATE, bid);
                                    checkPool();
                                }
                                break;

                            case C25_LOST:
                                if (bankerRole) {
                                    txtInformation.setText("Lost. Equivalent of bid is deducted from pool." +
                                            "\nStart new deal.");

                                    twoDeals();
                                } else {
                                    txtInformation.setText("Lost. Bid is added to the pool." +
                                            "\nOpponent is dealing cards.");

                                    if (coins < 5) {
                                        endOfMoney();
                                    }
                                }
                                break;

                            case C26_CARDS_AT_TABLE:

                                if (bankerRole) {
                                    String arrString = getParameteres(line).substring(1, getParameteres(line).length() - 1).replaceAll("\\s+", "");

                                    String[] cardsNumbers = arrString.split(",");

                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {


                                            hbOpponentCards.getChildren().clear();
                                            opponentPoints = 0;

                                            for (String i: cardsNumbers) {
                                                hbOpponentCards.getChildren().addAll(deck.createAnCard(Integer.parseInt(i)));
                                                opponentPoints += deck.createAnCard(Integer.parseInt(i)).cardValue;
                                            }


                                            System.out.println(playerPoints + " i " + opponentPoints);

                                            send(Protocol.C23_WHOO_WINS, playerPoints + "," + opponentPoints);
                                            updateOpponentPoints(opponentPoints);
                                        }
                                    });

                                    btnDeal.setDisable(false);

                                } else {
                                    String arrString = getParameteres(line).
                                            substring(1, getParameteres(line).length() - 1).
                                            replaceAll("\\s+", "");


                                    String[] cardsNumbers = arrString.split(",");



                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            hbOpponentCards.getChildren().clear();
                                            opponentPoints = 0;

                                            for (String i: cardsNumbers) {
                                                hbOpponentCards.getChildren().addAll(deck.createAnCard(Integer.parseInt(i)));
                                                opponentPoints += deck.createAnCard(Integer.parseInt(i)).cardValue;
                                            }

                                            updateOpponentPoints(opponentPoints);
                                        }
                                    });

                                }
                                break;

                            case C28_ADD_MONEY_OPPONENT:
                                coins += Integer.parseInt(getParameteres(line));
                                send(Protocol.C5_COINS_UPDATE, coins);
                                break;

                            case C27_CHANGE_ROLE:
                                if (bankerRole) {

                                    bankerRole = !bankerRole;

                                    if (coins < 5) {
                                        endOfMoney();
                                    }

                                    txtInformation.setText("CHANGE OF ROLE. Opponent is an banker. Wait for deal.");

                                    btnDeal.setDisable(true);
                                    btnTwoRounds.setDisable(true);
                                    twoRoundsCount = 3;
                                    roundCounter = 0;
                                } else {

                                    if (coins < 5) {
                                        endOfMoney();
                                    }

                                    roundCounter = 0;
                                    bankerRole = !bankerRole;
                                    twoRoundsCount = 3;
                                    turnOffBetButtons();
                                    btnDeal.setDisable(false);
                                    btnTwoRounds.setDisable(false);
                                    txtInformation.setText("CHANGE OF ROLE. You are now banker." +
                                            "\nDeal the cards.");
                                }
                                break;

                            case C30_TWO_DEALS:
                                twoRoundsCount = Integer.parseInt(getParameteres(line));


                                if (twoRoundsCount == 0) {
                                    isTwoRoundsStarded = false;
                                    send(Protocol.C27_CHANGE_ROLE);
                                } else if (twoRoundsCount == 3) {
                                    txtInformation.setText(txtInformation.getText() + "\nOpponent announces about two deals.");
                                } else {

                                    txtInformation.setText(txtInformation.getText() + "\nDeals left: " + twoRoundsCount);
                                }

                                break;

                            case C31_END_OF_GAME:
                                String info = getParameteres(line);

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        Alert alert = new Alert(Alert.AlertType.INFORMATION, info + "\nDo you want to leave?",
                                                ButtonType.YES, ButtonType.NO);
                                        alert.showAndWait();

                                        if (alert.getResult().getText().equals("No")) {
                                            send(Protocol.C29_STAYING_IN_GAME);
                                            turnOffBetButtons();
                                            btnTwoRounds.setDisable(true);
                                            btnTakeCard.setDisable(true);
                                            btnBet.setDisable(true);
                                            btnStand.setDisable(true);
                                            btnDeal.setDisable(true);

                                            txtInformation.setText("Waiting for an opponent");

                                            send(Protocol.C7_BID_UPDATE, 0);
                                            send(Protocol.C6_POOL_UPDATE, 0);

                                            updatePlayerPoints(0);
                                            txtOpponentPoints.setText("- - - - - - - -");
                                            roundCounter = 0;

                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {

                                                    hbOpponentCards.getChildren().clear();
                                                    hbPlayerCards.getChildren().clear();

                                                }
                                            });

                                        } else {
                                            send(Protocol.C32_LOGOUT);
                                            System.exit(0);
                                        }
                                    }
                                });

                                break;

                            case C32_LOGOUT:

                                connected = false;
                                btnDisconnect.setDisable(true);
                                btnConnect.setDisable(false);
                                txtHost.setDisable(false);
                                txtPort.setDisable(false);

                                try {
                                    socket.close();
                                } catch (IOException ew) {}

                                break;
                        }
                    }
                }

            } catch (IOException ex) {
                System.out.println("?");
            }

        }


        void endOfMoney() {
            send(Protocol.C32_LOGOUT, "Opponent have no more money. ");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "You have no more money. Game over.", ButtonType.OK);
                    alert.showAndWait();
                    System.exit(0);
                }
            });
        }

        Rectangle zaslonKarte() {
            Rectangle zaslonietaKarta = new Rectangle(100, 140);
            zaslonietaKarta.setArcWidth(20);
            zaslonietaKarta.setArcHeight(20);
            zaslonietaKarta.setFill(Color.GRAY);
            return zaslonietaKarta;
        }

        void twoDeals() {
            if (isTwoRoundsStarded) {
                twoRoundsCount--;
                if (twoRoundsCount == 0) {
                    coins += pool;
                    send(Protocol.C5_COINS_UPDATE, "" + coins);
                    pool = 0;
                    send(Protocol.C6_POOL_UPDATE, pool + "");
                    send(Protocol.C27_CHANGE_ROLE);
                } else {
                    txtInformation.setText(txtInformation.getText() + "\nDeals left: " + twoRoundsCount);
                    send(Protocol.C30_TWO_DEALS, twoRoundsCount);
                }

            }
        }

        public void checkPool() {
            if (pool == 0) {
                send(Protocol.C27_CHANGE_ROLE, playerNick);

            }
        }
        public void updatePlayerPoints(int pkt) {
            txtPlayerPoints.setText(playerNick + ": " + pkt);
        }

        public void updateOpponentPoints(int pkt) {
            txtOpponentPoints.setText(opponentNick + ": " + pkt);
        }


        public void turnOffBetButtons() {
            btnBet500.setDisable(true);
            btnBet250.setDisable(true);
            btnBet100.setDisable(true);
            btnBet25.setDisable(true);
            btnBet10.setDisable(true);
            btnBet5.setDisable(true);
        }

        public void bet(int iloscPieniedzy) {
            if (bankerRole) {
                coins -= iloscPieniedzy;
                send(Protocol.C5_COINS_UPDATE, coins);

                pool += iloscPieniedzy;
                send(Protocol.C6_POOL_UPDATE, pool);
                checkPocket();

            } else {
                coins -= iloscPieniedzy;
                send(Protocol.C5_COINS_UPDATE, coins);

                bid += iloscPieniedzy;
                send(Protocol.C7_BID_UPDATE, bid);
                checkIfPlayerCanUseThisNumber();

                btnBet.setDisable(false);
            }
        }

        String getParameteres(String s) {
            return (s.split(";", 2).length > 1) ? s.split(";", 2)[1] : "";
        }

        void resetAllNumbers() {
            txtPool.setText("Pool: -");
            txtMoney.setText("Money: -");
            txtBid.setText("Bid: -");

        }

        void checkPocket() {
            if (coins < 5) {
                btnBet5.setDisable(true);

            } else {
                btnBet5.setDisable(false);
            }

            if (coins < 10) {
                btnBet10.setDisable(true);
            } else {
                btnBet10.setDisable(false);
            }

            if (coins < 25) {
                btnBet25.setDisable(true);
            } else {
                btnBet25.setDisable(false);
            }

            if (coins < 100) {
                btnBet100.setDisable(true);
            } else {
                btnBet100.setDisable(false);
            }

            if (coins < 250) {
                btnBet250.setDisable(true);
            } else {
                btnBet250.setDisable(false);
            }

            if (coins < 500) {
                btnBet500.setDisable(true);
            } else {
                btnBet500.setDisable(false);
            }

        }

        void checkIfPlayerCanUseThisNumber() {
            if (pool - bid < 500) {
                btnBet500.setDisable(true);
            } else {
                btnBet500.setDisable(false);
            }

            if (pool - bid < 250) {
                btnBet250.setDisable(true);
            } else {
                btnBet250.setDisable(false);
            }

            if (pool - bid < 100) {
                btnBet100.setDisable(true);
            } else {
                btnBet100.setDisable(false);
            }

            if (pool - bid < 25) {
                btnBet25.setDisable(true);
            } else {
                btnBet25.setDisable(false);
            }

            if (pool - bid < 10) {
                btnBet10.setDisable(true);
            } else {
                btnBet10.setDisable(false);
            }

            if (pool - bid < 5) {
                btnBet5.setDisable(true);
            } else {
                btnBet5.setDisable(false);
            }

            if (coins < 500) {
                checkPocket();
            }
        }


        public void send(Protocol protocol, String text) {
            out.println(protocol + ";" + text);
        }

        void send(Protocol command, int parameters) {
            out.println(command + ";" + parameters);
        }

        void send(Protocol command) {
            out.println(command + ";");
        }

    }

}