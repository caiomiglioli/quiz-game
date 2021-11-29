package triviagame.interfaces;

public interface HasChatBox {
    public void printLine(String line);
    public void updatePlayerCount(int count);
    public void updateTimer(int seconds);
    public void disableAttempt();
}
