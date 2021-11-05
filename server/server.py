import eventlet
import socketio
import json

import time

from Game import Game

# data = {"hello" : "world"}
# jsonreturn = json.dumps(data)

# print("datadump: " + json.dumps(data))

sio = socketio.Server()
app = socketio.WSGIApp(sio, static_files={
    '/': {'content_type': 'text/html', 'filename': 'index.html'}
})

#instancia do jogo
game = Game(sio)

@sio.event
def connect(sid, environ):
    print('connect ', sid)
    res = json.dumps(game.getPlayerNames())
    sio.emit('connectReply', res, room=sid)
#end connect

@sio.event
def disconnect(sid):
    game.removePlayer(sid)
    print('disconnect ', sid)
    # sio.emit('disconnectReply', 'succesful!', room=sid)
#end disconnect

@sio.event
def joinGame(sid, data):
    res = game.addPlayer(sid, data)
    sio.emit('joinGameReply', json.dumps(res), room=sid)
    
    if res['connection'] == 'success':
        print("successsssssssssssssssssssssssssssssssssssssssssssssssss")
        sio.enter_room(sid, game.ROOM)
        game.newEvent(sid, 'join')
#end joingame

@sio.event
def gameReady(sid, data):
    game.setPlayerReady(sid, True)
    #teste
    # data = {"playerType" : "player"}
    # jsonreturn = json.dumps(data)
    # sio.emit('startRound', jsonreturn, room=sid)
    # print('startround enviado ', jsonreturn)
#end playerconnect


# @sio.event
# def hello(sid, dataaa):
#     print('dict' + dataaa['heeello'])
#     print("data: " + json.dumps(dataaa))

#     # print('hello ', data)
#     sio.emit('world', jsonreturn, room=sid)



if __name__ == '__main__':
    eventlet.wsgi.server(eventlet.listen(('', 5000)), app)