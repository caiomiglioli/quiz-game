package triviagame.handlers;

import java.net.URI;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

// import javafx.application.Platform;
import triviagame.controllers.PageChooseTopic;
import triviagame.controllers.PageConnect;
import triviagame.controllers.PagePodium;
// import triviagame.controllers.PageTriviaGame;
// import triviagame.controllers.PageTriviaGameMaster;
// import triviagame.controllers.PageTriviaGame;
import triviagame.controllers.PageWaitTopic;
import triviagame.interfaces.HasChatBox;
import triviagame.interfaces.PageTrivia;
import triviagame.interfaces.PageAnswer;

public class SocketHandler {

    private StageHandler stageHandler;
    private Socket socket;

    public SocketHandler(StageHandler stageHandler){
        this.stageHandler = stageHandler;
    }

    public void init(){
        //crio socket
        URI uri = URI.create("http://localhost:5000");
        IO.Options options = IO.Options.builder().build();
        socket = IO.socket(uri, options);

        // conecto no socket
        socket.connect();

        //handshake
        socket.once("connectReply", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                //antibug
                String arg = (String) args[0];                
                for (Object object : args) {
                    String obj = (String) object;
                    if(obj.charAt(0) == '{'){
                        arg = obj;
                        break;
                    }
                }
                JSONObject response = new JSONObject(arg);
                //end antibug

                try {
                    stageHandler.changeSceneSynchronous("/pages/pageConnect.fxml");
                    PageConnect controller = (PageConnect)stageHandler.getSceneController();
                    // controller.updateTimer(seconds);

                    controller.startTimer();

                    int playerCount = response.getInt("playerCount");

                    controller.updatePlayerCount(playerCount);
                    controller.updateTimer(response.getInt("countdown"));                      

                    for(int i=1; i<=playerCount; i++){
                        String key = "player" + Integer.toString(i);
                        String name = response.getString(key);
                        controller.printLine(name + " entrou no jogo...");
                    }
                    
                    System.out.println("Conectado ao servidor.");

                } catch(Exception e){
                    System.out.println("Exception Error: " + e);
                }
            }
        });

        //atualizar chatbox
        socket.on("newEvent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("NewEvent: " + (String)args[0]);
                //get controller
                HasChatBox controller = (HasChatBox) stageHandler.getSceneController();
                
                JSONObject event = new JSONObject((String)args[0]);
                controller.updateTimer(event.getInt("countdown"));

                //type join
                if(event.getString("type").equals("join")){
                    controller.printLine(event.getString("username") + " entrou no jogo...");
                    controller.updatePlayerCount(event.getInt("playerCount"));
                }

                //type notEnoughPlayers
                else if(event.getString("type").equals("notEnoughPlayers")){
                    controller.printLine("Nao ha jogadores o suficiente... Reiniciando contagem!");
                }

                //type gameHasBegun
                else if(event.getString("type").equals("gameHasBegun")){
                    controller.printLine("Tudo pronto! O jogo esta comecando!");             
                }

                //type disconnect
                else if(event.getString("type").equals("disconnect")){
                    controller.printLine(event.getString("username") + " saiu do jogo...");
                }

                //type newmessage
                else if(event.getString("type").equals("newMessage")){
                    controller.printLine(event.getString("username") + ": " + event.getString("message"));
                }

                else if(event.getString("type").equals("newAttempt")){
                    controller.printLine(event.getString("username") + ": " + event.getString("attempt"));
                }
                
                else if(event.getString("type").equals("rightAnswer")){
                    controller.printLine("Voce ganhou " + Integer.toString(event.getInt("pointsScored")) + " pontos!");
                    controller.disableAttempt();
                }

                else if(event.getString("type").equals("gameOver")){
                    controller.printLine("Jogo finalizado!!!");
                    controller.printLine("Calculando vencedores...");
                }
            }
        });
     
        System.out.println("socket handler iniciado");
    }

    public void terminate(){
        // terminate sockets
        socket.off();
        socket.disconnect();
        // socket.close();

        System.out.println("socket handler terminado");
    }

    public void emit(String event, Object message){
        socket.emit(event, message);
    }

    public Socket getSocket(){
        return socket;
    }

    public void initGameListeners(){
       
        socket.on("ChooseTopic", new Emitter.Listener() {
            @Override
            public void call(Object... args) {             
                JSONObject reply = new JSONObject((String)args[0]);

                try {
                    if(reply.get("playerType").equals("master")){
                        stageHandler.changeSceneSynchronous("/pages/pageChooseTopic.fxml");
                        PageChooseTopic controller = (PageChooseTopic)stageHandler.getSceneController();
                        // controller.updateTimer(segundos);
                        controller.startTimer();

                    }else{
                        stageHandler.changeSceneSynchronous("/pages/pageWaitTopic.fxml");
                        PageWaitTopic controller = (PageWaitTopic)stageHandler.getSceneController();
                        // controller.updateTimer(segundos);
                        controller.startTimer();
                    }
                
                    
                } catch(Exception e){
                    System.out.println("Exception Error: " + e);
                
                } finally {
                    // PageConnect controller = (PageConnect) stageHandler.getSceneController();
                    // controller.olamundo();

                }
                
            }
        });

        socket.on("startTrivia", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("START TRIVIA" + (String)args[0]);
                JSONObject response = new JSONObject((String)args[0]);

                try{
                    //Carregar páginas
                    if(response.getString("playerType").equals("master")){
                        stageHandler.changeSceneSynchronous("/pages/pageTriviaGameMaster.fxml");
                    }else{
                        stageHandler.changeSceneSynchronous("/pages/pageTriviaGame.fxml");
                    }
                    
                    //atualizar informaçoes
                    PageTrivia controller = (PageTrivia) stageHandler.getSceneController();

                    controller.startUI(response);
                    controller.startTimer();

                }catch(Exception e){
                    System.out.println("Exception Error: " + e);
                }
                // CONTINUAR

            }
        });

        socket.on("finishRound", new Emitter.Listener() {
            @Override
            public void call(Object... args) {                
                JSONObject reply = new JSONObject((String)args[0]);
                //get controller
                try {
                    stageHandler.changeSceneSynchronous("/pages/pageRoundIsOver.fxml");
                    PageAnswer controller = (PageAnswer) stageHandler.getSceneController();
                    controller.showAnswer(reply.getString("answer"));
                } catch (Exception e) {
                    System.out.println("Exception Error: " + e);
                }
            }
        });

        socket.on("gameOver", new Emitter.Listener() {
            @Override
            public void call(Object... args) {                
                JSONObject reply = new JSONObject((String)args[0]);

                try {
                    stageHandler.changeSceneSynchronous("/pages/pagePodium.fxml");
                    PagePodium controller = (PagePodium) stageHandler.getSceneController();
                    controller.updateRank(reply);
                } catch (Exception e) {
                    System.out.println("Exception Error: " + e);
                }
            }
        });
        //emitir sinal de que o player está pronto'
        socket.emit("gameReady", "success");

        System.out.println("init game listeners");
    }
}


        // new Socket

        // //conectar ao servidor
        // try {
        //     //crio socket
        //     URI uri = URI.create("http://localhost:5000");
        //     IO.Options options = IO.Options.builder().build();
        //     socket = IO.socket(uri, options);

        //     //conecto no socket
        //     socket.connect();

        //     //recebo mensagens
        //     socket.once("connectReply", new Emitter.Listener() {
        //         @Override
        //         public void call(Object... args) {
        //             System.out.println("Conectado.");
        //         }
        //     });

        // } catch(Exception ex) {
        //     if(socket != null){
        //         socket.disconnect();
        //         System.out.println("Disconectado. " + socket);
        //     }
        //     System.out.println("Exception Error: " + ex);

        // } finally {
        //     // if(socket != null){
        //     //     socket.disconnect();
        //     //     System.out.println("Disconectado. " + socket);
        //     // }
        // }

        
        // app.stageHandler.changeScene();
        // // //crio socket
        // // URI uri = URI.create("http://localhost:5000");
        // // IO.Options options = IO.Options.builder().build();
        // // Socket socket = IO.socket(uri, options);

        // // //conecto no socket
        // // socket.connect();
        // System.out.println("fechando.");

        // //emito mensagens
        // socket.emit("my_message", "testando 123");
        // socket.emit("hello", "world");

        // //recebo mensagens
        // socket.on("world", new Emitter.Listener() {
        //     @Override
        //     public void call(Object... args) {
        //         System.out.println("hello world!!!!!. -> " + args[0]);
        //     }
        // });


        //abrir pagina de conectar

        // import org.json.JSONObject;

                // //enviar objeto json
                // JSONObject json = new JSONObject();
                // json.put("heeello", "wooolrd");
                // socket.emit("hello", json);

                
                // //receber e ler objeto json
                // socket.once("world", new Emitter.Listener() {
                //     @Override
                //     public void call(Object... args) {
                //         //receber e ler objeto json
                //         JSONObject my_obj = new JSONObject((String)args[0]);
                //         System.out.println("my_obj.hello: " + my_obj.get("hello"));
                //     }
                // });