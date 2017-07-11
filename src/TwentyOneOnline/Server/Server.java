package TwentyOneOnline.Server;

import TwentyOneOnline.Protocol.Protocol;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Server extends Application {

    private Button btnStart, btnStop;
    private Text txtPort = new Text();
    private TextField textFieldPortInput = new TextField("2345");
    private ServerThread serverThread;
    private boolean isServerStarted = false;
    private ArrayList<ConnectionThread> connections = new ArrayList<>();
    private ArrayList<Integer[]> listOfArraysWithUsedCards = new ArrayList<>();

    public Parent createWindow() throws IOException {
        Pane rootPanel = new Pane();

        txtPort.setText("Enter port: ");
        txtPort.setStyle("-fx-font-weight: bold");
        textFieldPortInput.setPrefWidth(50);

        btnStart = new Button("Start");
        btnStart.setOnAction(event -> {
            serverThread = new ServerThread();
            serverThread.start();
            isServerStarted = true;
            btnStart.setDisable(true);
            btnStop.setDisable(false);
            textFieldPortInput.setDisable(true);

        });

        btnStop = new Button("Stop");
        btnStop.setDisable(true);
        btnStop.setOnAction(event -> {
            serverThread.kill();
            isServerStarted = false;
            btnStart.setDisable(false);
            btnStop.setDisable(true);
            textFieldPortInput.setDisable(false);
        });

        HBox hbItems = new HBox(5);
        hbItems.getChildren().addAll(txtPort, textFieldPortInput, btnStart, btnStop);
        hbItems.setPrefSize(300, 100);
        hbItems.setAlignment(Pos.CENTER);

        rootPanel.getChildren().add(hbItems);
        return rootPanel;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createWindow());
        primaryStage.setScene(scene);
        primaryStage.setWidth(300);
        primaryStage.setHeight(100);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Server");
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                serverThread.kill();
                System.exit(0);
            }
        });
    }

    private class ServerThread extends Thread {
        private ServerSocket server;

        public void kill(){
            try {
                server.close();
                for (ConnectionThread connection: connections){
                    if(connection != null) {
                        connection.out.println(Protocol.C32_LOGOUT);
                        connection.socket.close();
                    }
                }
            } catch (IOException e){}
        }

        public void run(){
            try {
                server = new ServerSocket(new Integer(textFieldPortInput.getText()));
                while (isServerStarted) {
                    Socket socket = server.accept();
                    new ConnectionThread(socket).start();
                }
            } catch (SocketException e) {
                System.out.println(e);
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                try {
                    if (server != null) server.close();
                } catch (IOException e) {}
            }
        }
    }

    private class ConnectionThread extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String line = "";
        private String nick = "";
        private boolean hasAnOpponent = false;
        private String opponentNick = "";
        private boolean isBanker = false;
        private int coins = 11000;
        private int idOfArrayWithUsedCards = 0;
        private int rolledCard = 0;
        private ConnectionThread opponent = null;
        private ArrayList<Integer> rolledCards = new ArrayList<>();
        private int idInArray = 0;



        public ConnectionThread(Socket socket){
            this.socket=socket;
            synchronized(connections){
                connections.add(this);
                idInArray = connections.size()-1;
            }
        }

        public void run(){
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                while(isServerStarted){
                    line = in.readLine();

                    Protocol command = Protocol.valueOf(line.split(";")[0]);

                    switch (command){
                        case C1_LOGIN:
                            this.sendOut(command, "Enter nickname: ");
                            break;
                        case C2_SET_NICK:
                            nick = getParameteres(line);
                            System.out.println("new " + nick);

                            if (isNickValid()) {
                                this.sendOut(Protocol.C5_COINS_UPDATE, this.coins);
                                for (ConnectionThread c: connections) {
                                    if (c != null) {
                                        if (!c.hasAnOpponent && c != this) {

                                            listOfArraysWithUsedCards.add(new Integer[52]);

                                            c.sendOut(Protocol.C4_SET_INITIAL_ROLE,
                                                    "banker");
                                            c.isBanker = true;
                                            c.hasAnOpponent = true;
                                            c.sendOut(Protocol.C3_SYNCHRINIZE_NICKS,
                                                    this.nick);
                                            c.opponentNick = this.nick;
                                            c.opponent = this;
                                            c.idOfArrayWithUsedCards = listOfArraysWithUsedCards.size() - 1;

                                            //   System.out.println(c.nick + " has an opponent: " + this.nick);
                                            this.sendOut(Protocol.C4_SET_INITIAL_ROLE,
                                                    "notBanker");
                                            this.isBanker = false;
                                            this.hasAnOpponent = true;
                                            this.sendOut(Protocol.C3_SYNCHRINIZE_NICKS,
                                                    c.nick);
                                            this.opponentNick = c.nick;
                                            this.opponent = c;
                                            //    System.out.println(this.nick + " has an opponent: " + c.nick);
                                            this.idOfArrayWithUsedCards = listOfArraysWithUsedCards.size() - 1;

                                            System.out.println(this.nick + " & " + c.nick + " have " + (listOfArraysWithUsedCards.size() - 1));
                                        }
                                    }
                                }
                            }
                            break;

                        case C5_COINS_UPDATE:
                            this.coins = Integer.parseInt(getParameteres(line));
                            this.sendOut(Protocol.C5_COINS_UPDATE, this.coins);
                            break;

                        case C6_POOL_UPDATE:
                            String pool = getParameteres(line);
                            this.sendOut(Protocol.C6_POOL_UPDATE, pool);
                            opponent.sendOut(Protocol.C6_POOL_UPDATE, pool);
                            break;

                        case C7_BID_UPDATE:
                            String bid = getParameteres(line);
                            this.sendOut(command, bid);
                            opponent.sendOut(command, bid);
                            break;

                        case C8_DEAL_ONE_CARD_TO_EACH_PLAYER:
                            opponent.rolledCards.clear();
                            this.rolledCards.clear();
                            refillDeck();

                            opponent.rolledCard = rollAnCard();
                            this.rolledCard = rollAnCard();

                            opponent.rolledCards.add(opponent.rolledCard);
                            this.rolledCards.add(this.rolledCard);


                           opponent.sendOut(Protocol.C9_GET_DEALED_CARDS,
                                    opponent.rolledCard + "," +
                                    this.rolledCard);


                            this.sendOut(Protocol.C9_GET_DEALED_CARDS, this.rolledCard + "," +
                                    opponent.rolledCard);
                            break;

                        case C10_INFORM_OPPONENT_ABOUT_SET_BID:
                            opponent.sendOut(Protocol.C11_SET_BID);
                            break;

                        case C12_INFORM_OPPONENT_ABOUT_ACCEPTED_BID:
                            opponent.sendOut(Protocol.C13_BID_ACCEPTED);
                            break;

                        case C14_CHECK_POINTS:
                            int points = Integer.parseInt(getParameteres(line));

                            System.out.println("Player " + this.nick + " has " + points + " points");

                            if (points > 21) {
                                this.sendOut(Protocol.C18_LOSS);
                                System.out.println("do: "+opponent.nick);
                                opponent.sendOut(Protocol.C20_SHOW_ALL_CARDS, this.rolledCards + ";fura");
                            } else if (points == 21) {
                                this.sendOut(Protocol.C19_POINT);
                                System.out.println("do: "+opponent.nick);
                                opponent.sendOut(Protocol.C20_SHOW_ALL_CARDS, this.rolledCards + ";oczko");
                            }
                            break;

                        case C15_TAKE_AN_CARD:
                            this.rolledCard = rollAnCard();
                            this.rolledCards.add(this.rolledCard);

                            this.sendOut(Protocol.C16_SHOW_ROLLED_CARD, this.rolledCard);
                            opponent.sendOut(Protocol.C17_SHOW_GREYED_CARD);
                            break;

                        case C21_LET_TO_TAKE_CARD_BANKER:
                            opponent.sendOut(Protocol.C22_TAKE_CARD_BANKER);
                            break;

                        case C23_WHOO_WINS:
                            String[] parts = getParameteres(line).split(",");

                            int bankerPoints = Integer.parseInt(parts[0]);
                            int playerPoints = Integer.parseInt(parts[1]);

                            System.out.println("ROZSTRZYGNIECIEEEE " + bankerPoints + " vs " + playerPoints);

                            if (bankerPoints == playerPoints || bankerPoints > playerPoints) {
                                System.out.println("wygrana bankiera");

                                this.sendOut(Protocol.C24_WIN);
                                opponent.sendOut(Protocol.C25_LOST);


                            } else {

                                this.sendOut(Protocol.C25_LOST);
                                opponent.sendOut(Protocol.C24_WIN);

                            }

                            break;

                        case C26_CARDS_AT_TABLE:
                            opponent.sendOut(Protocol.C26_CARDS_AT_TABLE, this.rolledCards);
                            this.sendOut(Protocol.C26_CARDS_AT_TABLE, opponent.rolledCards);
                            break;

                        case C28_ADD_MONEY_OPPONENT:
                            opponent.sendOut(Protocol.C28_ADD_MONEY_OPPONENT, getParameteres(line));
                            break;

                        case C27_CHANGE_ROLE:
                            this.isBanker = !this.isBanker;
                            this.sendOut(Protocol.C27_CHANGE_ROLE);
                            opponent.isBanker = !opponent.isBanker;
                            opponent.sendOut(Protocol.C27_CHANGE_ROLE);

                            break;

                        case C30_TWO_DEALS:
                            int lr = Integer.parseInt(getParameteres(line));
                            opponent.sendOut(Protocol.C30_TWO_DEALS, lr);
                            break;

                        case C29_STAYING_IN_GAME:
                            this.hasAnOpponent = false;
                            break;


                        case C32_LOGOUT:

                            opponent.sendOut(Protocol.C31_END_OF_GAME, getParameteres(line));
                            this.socket.close();
                            currentThread().interrupt();
                            connections.set(idInArray, null);

                            for (ConnectionThread connection : connections) {
                                System.out.println(connection);
                            }
                            break;
                    }
                }

            } catch (IOException e){
            } finally {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e){}
            }

        }

        int rollAnCard(){
            int numberOfRandomCard = ThreadLocalRandom.current().nextInt(0, 51 + 1);

            while (listOfArraysWithUsedCards.get(this.idOfArrayWithUsedCards)[numberOfRandomCard] == -1) {
                numberOfRandomCard = (int) (Math.random() * 52);
            }

            listOfArraysWithUsedCards.get(this.idOfArrayWithUsedCards)[numberOfRandomCard] = -1;

            return numberOfRandomCard;
        }

        ConnectionThread getConnection(String searchNick){
            for (ConnectionThread c: connections) {
                if (c.nick.equals(searchNick)) {
                    return c;
                }
            }
            return null;
        }

        void refillDeck() {
            for (int i = 0; i < 52; i++) {
                listOfArraysWithUsedCards.get(this.idOfArrayWithUsedCards)[i] = i;
            }
        }

        void sendOut(Protocol command, String parameters){
            System.out.println("Sending connand: " + command +  ";"+ parameters);
            out.println(command + ";" + parameters);
        }

        void sendOut(Protocol command, int parameters){
            System.out.println("Sending connand: " + command +  ";"+ parameters);
            out.println(command + ";" + parameters);
        }

        void sendOut(Protocol command){
            System.out.println("Sending connand: " + command +  ";");
            out.println(command + ";");
        }

        void sendOut(Protocol command, ArrayList<Integer> parameters){
            System.out.println("Sending connand: " + command +  ";");
            out.println(command + ";" + parameters);
        }
        String send(Protocol command, String parameters){
            return command + ";" + parameters;
        }

        String getParameteres(String s){
            System.out.println(Arrays.toString(s.split(";", 2)));
            return (s.split(";", 2).length > 1) ? s.split(";",2)[1] : "";
        }

        boolean isNickValid() {
            for (int i = 0; i < nick.length(); i++){
                if (nick.charAt(i) == ';' || nick.charAt(i) == '.'|| nick.charAt(i) == ','  ){
                    out.println(send(Protocol.C1_LOGIN,
                            "<html>Followed characters are not allowed in nick ; . ,<br>" +
                                    "Enter correct nickname:</html>"));
                    return false;
                }
            }

            if (nick.equals("null") || nick.trim().equals("")) {
                out.println(send(Protocol.C1_LOGIN, "<html>You didn't enter nick." +
                        "<br>Enter nickname:</html>"));
                return false;
            }

            boolean isCorrect = true;
            for (ConnectionThread c: connections) {
                if (c != null) {
                    if (c.nick.equals(nick) && c != this) {
                        isCorrect = false;
                    }
                }
            }
            if (!isCorrect) {
                out.println(send(Protocol.C1_LOGIN,
                        "<html>This nick is already taken.<br>" +
                                "Enter other nickname:</html>"));
                return false;
            }
            return true;
        }
    }

    public static void main(String[] args){
        launch(args);
    }
}