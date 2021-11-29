package triviagame.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javafx.application.Platform;


import triviagame.interfaces.PageAnswer;

public class PageRoundIsOver implements PageAnswer {

    @FXML
    private Label lbl_answer;

    public void showAnswer(String answer) {
        Platform.runLater(() -> {
            lbl_answer.setText(answer);
        });
    }
}
