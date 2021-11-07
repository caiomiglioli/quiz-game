import socketio
import time
import json

class Timer:
    def __init__(self):
        self.isTimerCancelled = {}

        self.sio = socketio.Client()

        # @self.sio.event
        # def timerHandshake():

        @self.sio.event
        def startCountdown(data):
            aux = json.loads(data)
            isValid = self.countdown(aux['countdown'], aux['callback'])
            # se o countdown finalizou e não nenhum pedido de cancelamento
            if isValid:
                self.sio.emit('timeout', data)
        
        @self.sio.event
        def cancelCountdown(data):
            self.isTimerCancelled[json.loads(data)['callback']] = True

    def connect(self):        
        self.sio.connect('http://localhost:5000')
        self.sio.emit('socketHandshake', "handshake")
        self.sio.wait()

    def countdown(self, startValue, callback):
        self.isTimerCancelled[callback] = False

        while startValue > 0:
            if(self.isTimerCancelled[callback] == True):
                print("===================TIMER CANCELADO======================")
                return False

            startValue -= 1

            if(startValue % 5 == 0):
                self.sio.emit('countdownUpdate', json.dumps({'countdown': startValue, 'callback' : callback}))

            time.sleep(0.9)
        return True
