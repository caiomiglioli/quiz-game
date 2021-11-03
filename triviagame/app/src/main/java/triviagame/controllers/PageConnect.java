package triviagame.controllers;

import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.text.TextFlow;
// import javafx.stage.Stage;
// import triviagame.App;

import triviagame.Globals;
import triviagame.handlers.StageHandler;
import triviagame.handlers.SocketHandler;

public class PageConnect {

    StageHandler stageHandler = Globals.stageHandler;
    SocketHandler socketHandler = Globals.socketHandler;

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
    private TextField txtf_server;

    @FXML
    private TextField txtf_username;

    @FXML
    void connectToServer(ActionEvent event) {
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

        socket.once("joinGameReply", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject reply = new JSONObject((String)args[0]);
                Platform.runLater(() -> {
                    if(reply.get("connection").equals("success")){
                        btn_connect.setDisable(true);
                        btn_connect.setText("Conectado.");
                        lbl_error_msg.setStyle("-fx-text-fill: black;");
                        lbl_error_msg.setText("Aguardando inicio da partida!");
                        txtf_username.setDisable(true);

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

    public void olamundo(){
        System.out.println("helloworld");
    }


}