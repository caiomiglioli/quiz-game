package triviagame.interfaces;

import org.json.JSONObject;

public interface PageTrivia {
    public void printLine(String line);
    public void startUI(JSONObject data);
    public void startTimer();
}
