import time
import schedule
import socketio
import json

from ScheduleHandler import ScheduleHandler

class Game:
    def __init__(self, socket : socketio.Server):
        self.sio = socket      # socket
        self.players = []       # array de players
        self.hasBegun = False   # game iniciou

        self.scheduler = ScheduleHandler()
        self.countdown = 300

        self.ROOM = 'main'
    #end init

    def addPlayer(self, sid, data):
        
    #se players = 0, iniciar o countdown de 5min pra partida começar
    #se players = 0, iniciar o countdown de 5min pra partida começar
    #se players = 0, iniciar o countdown de 5min pra partida começar
    #se players = 0, iniciar o countdown de 5min pra partida começar
    #se players = 0, iniciar o countdown de 5min pra partida começar
    #se players = 0, iniciar o countdown de 5min pra partida começar
    #se players = 0, iniciar o countdown de 5min pra partida começar
    #se players = 0, iniciar o countdown de 5min pra partida começar
        # jsonreturn = json.dumps(data)
        #sincronizar o countdown de todo mundo (progressbar)
        obj_return = { 'countdown' : self.countdown }

        if self.hasBegun == True:
            obj_return['connection'] = 'running'
            return obj_return

        if len(self.players) >= 5:
            obj_return['connection'] = 'full'
            return obj_return

        # if(len(self.players) > 0): 
        for player in self.players:
            if(player['username'] == data['username']):
                obj_return['connection'] = 'duplicate'
                return obj_return

        # se a pessoa for a primeira a entrar, iniciar o countdown
        if(len(self.players) <= 0):
            self.startPreGame()
            # startGame() # vai ser oq irá iniciar o jogo inteiro (rounds) e fazer o loop

        self.players.append({
            'username' : data['username'],
            'points' : 0.0,
            'ready' : False,
            'sid' : sid
        })

        obj_return['connection'] = 'success'
        return obj_return
    #end addplayer

    def removePlayer(self, sid):
        for player in self.players:
            if(player['sid'] == sid):
                self.players.remove(player)
                    # self.players.pop()
    #end removeplayers
    
    def getPlayerNames(self):
        names = { 'playerCount' : len(self.players), 'countdown' : self.countdown}

        i = 1
        for player in self.players:
            key = 'player{}'
            names[key.format(i)] = player['username']
            i += 1

        return names
    #end returnplayers

    def setPlayerReady(self, sid, value):
        for player in self.players:
            if(player['sid'] == sid):
                player['ready'] = value
    #end setplayerready

    def startPreGame(self):
        self.scheduler.start()
        self.countdown = 300
        self.currentJob = schedule.every(1).seconds.do(self.countdownDecrease, _callback=self.startGame)
    #end startpregame

    def countdownDecrease(self,  _callback = None):
        self.countdown -= 1
        if self.countdown <= 0:
            if _callback:
                _callback()
            schedule.cancel_job(self.currentJob)
    #end countdowndecrease

    def startGame(self):
        print("Jogo startou")
    #end startGame

    def newEvent(self, sid, type):
        res = { 'type' : type }
        if type == 'join':
            player = self.getPlayerFromSID(sid)
            if player:
                res['username'] = player['username']
                res['playerCount'] = len(self.players)
                print("emitttttttttttttttttttttttttttttttttttttttttttttttttttt")
                self.sio.emit('newEvent', json.dumps(res), room=self.ROOM)
    #end newevent
    
    def getPlayerFromSID(self, sid):
        for player in self.players:
            if(player['sid'] == sid):
                return player
        return None
    #endgetPlayerFromSID


# game = Game("socket")
# game.startPreGame()

# while True:
#     time.sleep(1)