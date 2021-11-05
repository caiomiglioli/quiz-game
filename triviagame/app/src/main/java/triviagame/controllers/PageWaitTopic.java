package triviagame.controllers;

import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class PageWaitTopic {
    public Timer timer = null;

    @FXML
    private Label lbl_error_msg;

    @FXML
    private Label lbl_resposta;

    @FXML
    private Label lbl_sua_vez;

    @FXML
    private ProgressBar pgb_timer;
    
    public void updateTimer(int seconds){
        final Double time = (1.0/60.0) * seconds;
        pgb_timer.setProgress(time);
    }

    public void startTimer(){
        final Double time = 1.0/60.0; //100% / 60s = 0.01666...'
        
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
