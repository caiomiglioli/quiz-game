package triviagame.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
// import javafx.stage.Stage;

public class PageYourRound {

    @FXML
    private Button btn_start;

    @FXML
    private Label lbl_dica;

    @FXML
    private Label lbl_resposta;

    @FXML
    private Label lbl_sua_vez;

    @FXML
    private Label lbl_tema;

    @FXML
    private Label lbl_error_msg;

    @FXML
    private ProgressBar pgb_timer;

    @FXML
    private TextField txtf_dica;

    @FXML
    private TextField txtf_resposta;

    @FXML
    private TextField txtf_tema;

    @FXML
    void start_round(ActionEvent event) {
        String theme = txtf_tema.getText();
        String clue = txtf_dica.getText();
        String answer = txtf_resposta.getText();

        if(theme.isBlank() || clue.isBlank() || answer.isBlank()){
            lbl_error_msg.setStyle("-fx-text-fill: red;");
            lbl_error_msg.setText("Erro: Campo vazio!");
            return;
        }
        lbl_error_msg.setStyle("-fx-text-fill: green;");
        lbl_error_msg.setText("Iniciando rodada...");
        
        btn_start.setDisable(true);
        lbl_error_msg.setStyle("-fx-text-fill: black;");
        lbl_error_msg.setText("Iniciando rodada.");

        // Stage stage = (Stage)btn_start.getScene().getWindow();
        // // App app = new App();
        // try{
        //     new App().changeScene(stage, "/pageTriviaGameMaster.fxml");
        // }catch (Exception e){
        //     //xd
        // }
    }
}
