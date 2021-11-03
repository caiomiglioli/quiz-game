package triviagame.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

public class PageTriviaGameMaster {

    @FXML
    private Button btn_clue;

    @FXML
    private Label lbl_better_clue;

    @FXML
    private Label lbl_clue;

    @FXML
    private Label lbl_info;

    @FXML
    private Label lbl_theme;

    @FXML
    private ProgressBar pgb_timer;

    @FXML
    private TextArea txta_log;

    @FXML
    private TextArea txta_players;

    @FXML
    void reveal_letter(ActionEvent event) {

    }
}
