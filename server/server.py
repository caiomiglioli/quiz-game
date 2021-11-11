import eventlet
import socketio
import json
import threading

from Game import Game
from Timer import Timer

#instancia do socketio
sio = socketio.Server(logger=True, engineio_logger=True)
app = socketio.WSGIApp(sio)

#instancia do jogo
game = Game(sio)


#######################################################
#game game game game game game game game game game game
@sio.event
def connect(sid, environ):
    sio.enter_room(sid, game.ROOM)
    print('connect ', sid)
    res = json.dumps(game.getPlayerNames())
    sio.emit('connectReply', res, room=sid)
#end connect

@sio.event
def disconnect(sid):
    game.newEvent("disconnect", sid)
    game.removePlayer(sid)
    print('disconnect ', sid)
#end disconnect

@sio.event
def joinGame(sid, data):
    res = game.addPlayer(sid, data)
    sio.emit('joinGameReply', json.dumps(res), room=sid)
#end joingame

@sio.event
def gameReady(sid, data):
    game.setPlayerReady(sid, True)

@sio.event
def startTrivia(sid, data):
    game.chooseTopicSelect(sid, data)#json.loads(data))

@sio.event
def newMessage(sid, data):
    game.newEvent('newMessage', sid, data)#json.loads(data))

@sio.event
def newAttempt(sid, data):
    game.computeAttempt(sid, data)

#game game game game game game game game game game game
#######################################################


#######################################################
# timer timer timer timer timer timer timer timer timer 
@sio.event
def socketHandshake(sid, data):
    print("** timer connected - sid: " + sid)
    game.SIDTIMER = sid
    sio.leave_room(sid, game.ROOM)
    # sio.emit("timerStart", "message", room=sid)

@sio.event
def timeout(sid, data):
    res = json.loads(data)
    if(res['callback'] == 'pageConnect'):
        game.startGame()
    
    if(res['callback'] == 'chooseTopicTimeout'):
        game.chooseTopicTimeout()

    if(res['callback'] == 'triviaTimeout'):
        game.triviaTimeout()
    
    print("TIMEOUT #########################################")

@sio.event
def countdownUpdate(sid, data):
    game.countdown = json.loads(data)['countdown']

# funcao main da thread countdown
def countdown():
    timer = Timer()
    timer.connect()

# instanciando thread countdown
ct = threading.Thread(target = countdown)
ct.setDaemon(True)
ct.start()
# timer timer timer timer timer timer timer timer timer 
#######################################################

#server start
if __name__ == '__main__':
    eventlet.wsgi.server(eventlet.listen(('', 5000)), app)