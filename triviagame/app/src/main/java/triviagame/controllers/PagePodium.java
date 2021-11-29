package triviagame.controllers;

import org.json.JSONObject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PagePodium {

    @FXML
    private Label lbl_top1_name;

    @FXML
    private Label lbl_top1_points;

    @FXML
    private Label lbl_top2_name;

    @FXML
    private Label lbl_top2_points;

    @FXML
    private Label lbl_top3_name;

    @FXML
    private Label lbl_top3_points;

    public void updateRank(JSONObject json){
        Platform.runLater(() -> {
            // for(int i=1; i <= json.getInt("playerNum") && i <= 3; i++){
            // }
            lbl_top1_name.setText(json.getString("top1_name"));
            lbl_top1_points.setText(String.format("%.1f pontos", json.getDouble("top1_points")));

            lbl_top2_name.setText(json.getString("top2_name"));
            lbl_top2_points.setText(String.format("%.1f pontos", json.getDouble("top2_points")));

            if(json.getInt("playerNum") > 2){
                lbl_top3_name.setText(json.getString("top3_name"));
                lbl_top3_points.setText(String.format("%.1f pontos", json.getDouble("top3_points")));    
            }
        });
    }

}
