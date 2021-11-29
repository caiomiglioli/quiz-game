package triviagame.controllers;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import triviagame.Globals;
import triviagame.handlers.SocketHandler;
import triviagame.handlers.StageHandler;
import triviagame.interfaces.HasChatBox;
import triviagame.interfaces.PageTrivia;

public class PageTriviaGame implements PageTrivia, HasChatBox {

    StageHandler stageHandler = Globals.stageHandler;
    SocketHandler socketHandler = Globals.socketHandler;
    public Timer timer;
    private boolean isAttemptDisabled = false;

    @FXML
    private Button btn_send;

    @FXML
    private Label lbl_better_clue;

    @FXML
    private Label lbl_clue;

    @FXML
    private Label lbl_info;

    @FXML
    private Label lbl_topic;

    @FXML
    private ProgressBar prog_timeout;

    @FXML
    private TextFlow txt_log;

    @FXML
    private TextFlow txtf_playerRank;

    @FXML
    private TextField txtf_send;

    @FXML
    private ScrollPane scroll;

    @FXML
    void send_msg(ActionEvent event) {
        String texto = txtf_send.getText();
        txtf_send.setText("");

        if(texto.isBlank())
            return;
        
        //utilizando o chat
        if(texto.charAt(0) == '!'){
            JSONObject json = new JSONObject();
            json.put("message", texto.substring(1));
            json.put("playerType", "[P] ");  
            this.socketHandler.emit("newMessage", json);
        //resposta do trivia
        }else{
            if(this.isAttemptDisabled == true){
                printLine("ERRO: você já acertou.");
            }else{
                JSONObject json = new JSONObject();
                json.put("attempt", texto);
                json.put("playerType", "[P] ");
                this.socketHandler.emit("newAttempt", json);
            }
        }
    }

    // void updateUI(JSONObject json){
    public void startUI(JSONObject data){
        Platform.runLater(() -> {
            updateTimer(data.getInt("countdown"));
            lbl_clue.setText("Dica: " + data.getString("clue"));
            lbl_topic.setText("Tema: " + data.getString("topic"));
            lbl_info.setText("Palavra de " + data.getString("answer").length() + " letras");
            this.isAttemptDisabled = false;

            //txtf_playerRank
            for(int i=1; i<=data.getInt("playerCount"); i++){
                String player = "player" + i;
                String points = "player" + i + "_points";
                String master = "[P]";

                if( data.getString(player).equals(data.getString("master")) ){
                    master = "[M]";
                }
                String rank = String.format("%s %s: %.1f pontos", master, data.getString(player), data.getDouble(points));
                printRank(rank);
            }

        });
    }

    public void printLine(String line){
        Platform.runLater(() -> {
            txt_log.getChildren().add(new Text(line + "\n"));
            scroll.layout();
            scroll.setVvalue(1.0f);
        });
    }

    public void printRank(String line){
        Platform.runLater(() -> {
            txtf_playerRank.getChildren().add(new Text(line + "\n"));
        });
    }
    
    public void updateTimer(int seconds){
        Platform.runLater(() -> {
            final Double time = (1.0/120.0) * seconds;
            prog_timeout.layout();
            prog_timeout.setProgress(time);
        });
    }

    public void startTimer(){
        final Double time = 1.0/120.0; //100% / 120s (2 minutos) = 0.0083333...

        if(timer != null){
            timer.cancel();
            timer.purge();
        }

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run(){
                Platform.runLater(() -> {
                    Double progress = prog_timeout.getProgress();
                    prog_timeout.setProgress(progress - time);

                    if(progress < 0){
                        timer.cancel();
                        timer.purge();
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }
    public void disableAttempt(){
        this.isAttemptDisabled = true;
    }

    //nao utilizados
    public void updatePlayerCount(int count){}
}
