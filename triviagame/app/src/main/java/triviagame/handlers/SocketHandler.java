package triviagame.handlers;

import java.net.URI;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import javafx.application.Platform;

// import triviagame.controllers.PageConnect;
import triviagame.controllers.PageTriviaGame;

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
                try {
                    stageHandler.changeScene("/pages/pageConnect.fxml");
                    System.out.println("Conectado ao servidor.");
                } catch(Exception e){
                    System.out.println("Exception Error: " + e);
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

        socket.on("startRound", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject reply = new JSONObject((String)args[0]);
                System.out.println("startround call");
                try {
                    System.out.println("startround trycatch");
                    if(reply.get("playerType").equals("master")){
                        stageHandler.changeScene("/pages/YourRound.fxml");

                    }else{
                        System.out.println("startround else");
                        stageHandler.changeSceneSynchronous("/pages/pageTriviaGame.fxml");

                                               
                        
                        PageTriviaGame controller = (PageTriviaGame)stageHandler.getSceneController();
                        System.out.println("controller " + controller.getClass());

                        Platform.runLater(() -> {
                            controller.updateUI();
                        });
                        // controller.prog_timeout.setProgress(0.5);
                    }
                
                    
                } catch(Exception e){
                    System.out.println("Exception Error: " + e);
                
                } finally {
                    // PageConnect controller = (PageConnect) stageHandler.getSceneController();
                    // controller.olamundo();

                }
                
            }
        });

        socket.emit("gameReady", "xd");


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