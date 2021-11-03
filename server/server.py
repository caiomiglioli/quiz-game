import eventlet
import socketio
import json

import time

# data = {"hello" : "world"}
# jsonreturn = json.dumps(data)

# print("datadump: " + json.dumps(data))

sio = socketio.Server()
app = socketio.WSGIApp(sio, static_files={
    '/': {'content_type': 'text/html', 'filename': 'index.html'}
})

@sio.event
def connect(sid, environ):
    print('connect ', sid)
    sio.emit('connectReply', 'succesful!', room=sid)

@sio.event
def disconnect(sid):
    print('disconnect ', sid)
    sio.emit('disconnectReply', 'succesful!', room=sid)


@sio.event
def joinGame(sid, data):
    #logica
    data = {"connection" : "success"}
    jsonreturn = json.dumps(data)
    sio.emit('joinGameReply', jsonreturn, room=sid)

@sio.event
def gameReady(sid, data):
    data = {"playerType" : "player"}
    jsonreturn = json.dumps(data)
    sio.emit('startRound', jsonreturn)
    print('startround enviado ', jsonreturn)
#end playerconnect


# @sio.event
# def hello(sid, dataaa):
#     print('dict' + dataaa['heeello'])
#     print("data: " + json.dumps(dataaa))

#     # print('hello ', data)
#     sio.emit('world', jsonreturn, room=sid)



if __name__ == '__main__':
    eventlet.wsgi.server(eventlet.listen(('', 5000)), app)