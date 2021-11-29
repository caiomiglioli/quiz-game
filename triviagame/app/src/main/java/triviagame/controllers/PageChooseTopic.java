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
import javafx.scene.control.TextField;

import triviagame.Globals;
import triviagame.handlers.SocketHandler;
import triviagame.handlers.StageHandler;

public class PageChooseTopic {
    
    public Timer timer = null;  
    StageHandler stageHandler = Globals.stageHandler;
    SocketHandler socketHandler = Globals.socketHandler;  

    @FXML
    private Button btn_start;

    @FXML
    private Label lbl_dica;

    @FXML
    private Label lbl_error_msg;

    @FXML
    private Label lbl_resposta;

    @FXML
    private Label lbl_sua_vez;

    @FXML
    private Label lbl_tema;

    @FXML
    private ProgressBar pgb_timer;

    @FXML
    private TextField txtf_dica;

    @FXML
    private TextField txtf_resposta;

    @FXML
    private TextField txtf_tema;

    @FXML
    void startRound(ActionEvent event) {
        String clue = txtf_dica.getText();
        String answer = txtf_resposta.getText();
        String topic = txtf_tema.getText();

        //Erro, vazio
        int flagError = 0;
        if(clue.isBlank()){
            flagError = 1;
            txtf_dica.setPromptText("Erro: Campo Vazio!");
            txtf_dica.setStyle("-fx-prompt-text-fill: red");
        }
        if(answer.isBlank()){
            flagError = 1;
            txtf_resposta.setPromptText("Erro: Campo Vazio!");
            txtf_resposta.setStyle("-fx-prompt-text-fill: red");
        }
        if(topic.isBlank()){
            flagError = 1;
            txtf_tema.setPromptText("Erro: Campo Vazio!");
            txtf_tema.setStyle("-fx-prompt-text-fill: red");
        }
        if(flagError == 1){
            return;
        }
        
        //enviar objeto json
        JSONObject json = new JSONObject();
        json.put("topic", topic);     
        json.put("clue", clue);     
        json.put("answer", answer);             
        this.socketHandler.emit("startTrivia", json);

        btn_start.setDisable(true);
        btn_start.setText("Iniciando Round...");
    }

    public void updateTimer(int seconds){
        final Double time = (1.0/60.0) * seconds;
        pgb_timer.setProgress(time);
    }

    public void startTimer(){
        final Double time = 1.0/60.0; //100% / 60s = 0.01666...

        if(timer != null){
            timer.cancel();
            timer.purge();
        }

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run(){
                Platform.runLater(() -> {
                    Double progress = pgb_timer.getProgress();
                    pgb_timer.setProgress(progress - time);

                    if(progress < 0){
                        timer.cancel();
                        timer.purge();
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

}
