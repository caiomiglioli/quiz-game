package triviagame.controllers;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
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
// import javafx.stage.Stage;
// import triviagame.App;

import triviagame.Globals;
import triviagame.handlers.StageHandler;
import triviagame.handlers.SocketHandler;

import triviagame.interfaces.HasChatBox;

public class PageConnect implements HasChatBox{

    StageHandler stageHandler = Globals.stageHandler;
    SocketHandler socketHandler = Globals.socketHandler;

    public Timer timer;

    @FXML
    private Button btn_connect;

    @FXML
    private Label lbl_error_msg;

    @FXML
    private Label lbl_qtde_players;

    @FXML
    private ProgressBar pgb_timer_connect;

    @FXML
    private TextFlow txt_log;

    @FXML
    private ScrollPane scroll;

    @FXML
    private TextField txtf_server;

    @FXML
    private TextField txtf_username;

    @FXML
    void connectToServer(ActionEvent event) {
        
        // scroll.setVmin(1.0);
        
        String username = txtf_username.getText();
        
        if(username.isBlank()){
            lbl_error_msg.setStyle("-fx-text-fill: red;");
            lbl_error_msg.setText("Erro: Campo vazio!");
            return;
        }else{
            btn_connect.setDisable(true);
            lbl_error_msg.setStyle("-fx-text-fill: green;");
            lbl_error_msg.setText("Entrando na partida...");
        }
        
        /***********************
         conectar com o servidor
        ************************/
        
        //enviar objeto json
        JSONObject json = new JSONObject();
        json.put("username", username);                
        this.socketHandler.emit("joinGame", json);
        
        // se houver falha, eu me mantenho na pagina
        Socket socket = socketHandler.getSocket();
        
        socket.on("joinGameReply", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject reply = new JSONObject((String)args[0]);
                Platform.runLater(() -> {
                    
                    // updateTimer(reply.getInt("countdown"));    
                    
                    if(reply.get("connection").equals("success")){
                        btn_connect.setDisable(true);
                        btn_connect.setText("Conectado");
                        lbl_error_msg.setStyle("-fx-text-fill: black;");
                        lbl_error_msg.setText("Aguardando inicio da partida!");
                        txtf_username.setDisable(true);
                        
                        //iniciar jogo:
                        socketHandler.initGameListeners();
                        
                    }else if(reply.get("connection").equals("full")){
                        btn_connect.setDisable(false);
                        lbl_error_msg.setStyle("-fx-text-fill: red;");
                        lbl_error_msg.setText("Erro: Nao ha vagas!");
                        
                    }else if(reply.get("connection").equals("running")){
                        btn_connect.setDisable(false);
                        lbl_error_msg.setStyle("-fx-text-fill: red;");
                        lbl_error_msg.setText("Erro: Jogo esta em andamento!");
                        
                    }else if(reply.get("connection").equals("duplicate")){
                        btn_connect.setDisable(false);
                        lbl_error_msg.setStyle("-fx-text-fill: red;");
                        lbl_error_msg.setText("Erro: Apelido ja esta em uso!");
                        
                    }else{
                        btn_connect.setDisable(false);
                        lbl_error_msg.setStyle("-fx-text-fill: red;");
                        lbl_error_msg.setText("Erro inesperado, tente novamente.");
                    }
                });
            }
        });
    }
    
    public void updateTimer(int seconds){
        Platform.runLater(() -> {
            final Double time = (1.0/300.0) * seconds;
            pgb_timer_connect.layout();
            pgb_timer_connect.setProgress(time);
        });
    }

    public void startTimer(){
        // 100% - 0.1+ - 10s
        // 100% - 0.01+ - 100s
        // 100% - 0.005 - 200s
        // 100% - 0.0025 - 400s
        // 100% - 0.00333333 - 300s
        final Double time = 1.0/300.0; //100% / 300s = 0.003333...
        
        if(timer != null){
            timer.cancel();
            timer.purge();
        }
        
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run(){
                Platform.runLater(() -> {
                    Double progress = pgb_timer_connect.getProgress();
                    pgb_timer_connect.setProgress(progress - time);
                    
                    if(progress < 0){
                        timer.cancel();
                        timer.purge();
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }
    
    public void printLine(String line){
        Platform.runLater(() -> {
            txt_log.getChildren().add(new Text(line + "\n"));
            scroll.layout();
            scroll.setVvalue(1.0f);
        });
    }
    
    public void updatePlayerCount(int count){
        Platform.runLater(() -> {
            lbl_qtde_players.setText(Integer.toString(count) + "/5");
        });
    }

    // inutil
    public void disableAttempt(){}
}