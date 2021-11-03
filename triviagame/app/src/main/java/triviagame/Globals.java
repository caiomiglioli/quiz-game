package triviagame;

//Handlers
import triviagame.handlers.SocketHandler;
import triviagame.handlers.StageHandler;

public class Globals {
    public static final StageHandler stageHandler = new StageHandler();
    public static final SocketHandler socketHandler = new SocketHandler(stageHandler);
}
