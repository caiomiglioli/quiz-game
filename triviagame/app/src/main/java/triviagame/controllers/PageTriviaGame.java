package triviagame.controllers;

// import org.json.JSONObject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.text.TextFlow;

public class PageTriviaGame {

    @FXML
    private Button btn_send;

    @FXML
    private Label lbl_better_clue;

    @FXML
    private Label lbl_info;

    @FXML
    private ProgressBar prog_timeout;

    @FXML
    private TextFlow txtf_log;

    @FXML
    private TextFlow txtf_playerRank;

    @FXML
    private TextField txtf_send;

    @FXML
    void send_msg(ActionEvent event) {
    }

    // void updateUI(JSONObject json){
    public void updateUI(){

        prog_timeout.setProgress(0.5);
        btn_send.setText("aaaaa");
    }

}
