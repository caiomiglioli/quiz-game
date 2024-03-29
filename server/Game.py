import time
import schedule
import socketio
import json

import random
import math

from scipy.spatial import distance

from ScheduleHandler import ScheduleHandler

class Game:
    def __init__(self, socket : socketio.Server):
        self.sio = socket           #socket
        self.ROOM = 'main'          #socket room broadcast
        self.SIDTIMER = None        #socket room timer
        
        self.players = []           #array de players
        self.currentMaster = 0      #mestre da rodada / conta em qual round está
        self.topic = None           #tema
        self.clue = None            #dica
        self.answer = None          #resposta

        self.percentClue = 0.3


        self.roundScorers = 0       #quantidade de jogadores que acertaram no round
        self.cluePunishment = 0
        self.score = 10             #quantidade maxima de pontos que alguem ganha em um round

        #controles
        self.countdown = 300        #countdown do timeout
        self.hasBegun = False       #game iniciou
        self.isRoundOver = False    #round acabou 
        self.maxHalf = 2            #conta quantas vezes cada jogador pode ser o mestre
        self.currentHalf = 0        #conta quantas vezes cada jogador foi o mestre

        self.pageConnectTime = 10   #indica o tempo de timeout da página Connect
        self.chooseTopicTime = 30   #indica o tempo de timeout da página ChooseTopic
        self.roundIsOverTime = 10   #indica o tempo de timeout da página RoundIsOver
        self.triviaTime = 60        #indica o tempo de timeout da página TriviaGame
    #end init

    ############################### Pré-Jogo ##################################
    ############################### Pré-Jogo ##################################
    #retornar nome dos jogadores no PageConnect
    def getPlayerNames(self):
        names = { 'playerCount' : len(self.players), 'countdown' : self.countdown}

        i = 1
        for player in self.players:
            key = 'player{}'
            names[key.format(i)] = player['username']
            i += 1

        return names
    #end returnplayers

    #remover player que desconectou
    def removePlayer(self, sid):
        for player in self.players:
            if(player['sid'] == sid):
                self.players.remove(player)
                    # self.players.pop()

        # reiniciar jogo
        if len(self.players) < 2:
            self.restartGame()
    #end removeplayers

    def restartGame(self):
        print("====================== RESTART GAME =========================")
        print("====================== RESTART GAME =========================")
       
        # cancelar todos os timers
        self.sio.emit('cancelAllCountdown', "cancel-all", sid=self.SIDTIMER)
            
        #remover todos os jogadores
        self.players.clear()
        self.currentMaster = 0
        self.topic = self.clue = self.answer = None
        self.roundScorers = self.cluePunishment = 0

        self.countdown = 300
        self.hasBegun = self.isRoundOver = False
        self.currentHalf = 0

        # jogar todo mundo pra tela inicial
        res = json.dumps(self.getPlayerNames())
        self.sio.emit('connectReply', res, room=self.ROOM)
        # self.sio.emit('restartGame', "restart", room=self.ROOM)

    #end restartgame

    #Adicionar players ao jogo quando clicarem no botão "Conectar" na página conect
    def addPlayer(self, sid, data):
        #sincronizar o countdown de todo mundo (progressbar)
        obj_return = {}

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

        self.newEvent('join', sid)

        obj_return['connection'] = 'success'
        return obj_return
    #end addplayer

    #Mudar jogadores para ready quando seus clientes estivem com todos os Listeners rodando
    def setPlayerReady(self, sid, value):
        for player in self.players:
            if(player['sid'] == sid):
                player['ready'] = value
        
        count = 0
        for player in self.players:
            if(player['ready'] == True):
                count +=1

        if count >= 5:
            self.cancelTimer('pageConnect')
            self.startGame()
    #end setplayerready

    #iniciar Timeout no PageConnect
    def startPreGame(self):
        self.countdown = self.pageConnectTime #pageconnect countdown
        self.startTimer(self.pageConnectTime, "pageConnect")
    #end startpregame

    def startGame(self):
        #jogo não pode iniciar ainda
        if(len(self.players) < 2):
            self.startPreGame()
            self.newEvent(type='notEnoughPlayers', sid=self.ROOM)
            return

        #jogo iniciou
        print("Jogo Iniciou")
        self.hasBegun = True
        self.newEvent('gameHasBegun')
        # time.sleep(5)

        #rodadas
        # for i in range(2):
        #     for player in self.players:
        self.startRound()
    #end startGame

    ############################### Jogo ##################################
    ############################### Jogo ##################################  
    #escolher o novo mestre e iniciar o round
    def startRound(self):
        print("=================== start round ======================")
        print("master: " + str(self.currentMaster) + " len:" + str(len(self.players)))
        print("half: " + str(self.currentHalf))
        print("=================== start round ======================")
        #se eu aumentar o mestre, eu vou passar (>) do numero de jogadores na partida?
        #não, entao iniciar proximo round
        if (self.currentMaster + 1) <= len(self.players):
            #emit escolher o tema pro master
            self.isRoundOver = False
            self.chooseTopic(self.currentMaster)
            #fazer o proximo jogador ser master
            self.currentMaster += 1
            self.roundScorers = 0
            self.cluePunishment = 0
        
        #se sim,
        else:  
            #todos os jogadores jogaram 2 vezes?
            #nao, então muda pro proximo half
            if (self.currentHalf + 1) < self.maxHalf:
                self.currentMaster = 0
                self.currentHalf += 1
                self.startRound()
            #sim, então fim de jogo
            else:
                # self.newEvent('gameOver')
                self.startTimer(5, "gameOverTimeout")

                #fim de jogo
    #end startround

    #====>> escolher tema
    #mestre vai escolher o tema da rodada
    def chooseTopic(self, master):
        #definir timeouts
        self.topic = self.clue = self.answer = None
        self.countdown = self.chooseTopicTime
        self.startTimer(self.chooseTopicTime, "chooseTopicTimeout")

        data = { "countdown" : self.countdown }
        for player in self.players:
            if player == self.players[master]:
                data["playerType"] = "master"
            else:
                data["playerType"] = "player"
            self.sio.emit('ChooseTopic', json.dumps(data), room=player['sid'])
    #endchoosetopic

    #trigger do choosetopic1: Se o mestre escolheu o tema > iniciar jogo
    def chooseTopicSelect(self, sid, data):
        self.cancelTimer("chooseTopicTimeout")
        self.topic = data['topic']
        self.clue = data['clue']
        self.answer = data['answer']
        player = self.getPlayerFromSID(sid)
        if player:
            self.startTrivia(self.players.index(player))

    #endchooseTopicSelect

    #trigger do choosetopic2: Se o mestre não escolheu o tema > escolher novo mestre e dar startround
    def chooseTopicTimeout(self):
        #acabou o tempo e nao foi preenchido os campos
        if self.topic == None or self.clue == None or self.answer == None:
            self.isRoundOver = True
            #emit comunicado de q o mestre nao deu o tema
            #time.sleep(5)
            self.startRound()
    #end choosetopicisover
    #====>> fim escolher tema

    #====>> gameplay
    #inicia a página TriviaGame
    def startTrivia(self, master):
        self.countdown = self.triviaTime

        #info do trivia
        data = {
            "countdown" : self.countdown,
            "topic" : self.topic,
            "clue"  : self.clue,
            "answer" : self.answer,
            "master" : self.players[master]['username']
        }

        #info dos players
        i = 1
        for player in self.players:
            data['player{}'.format(i)] = player['username']
            data['player{}_points'.format(i)] = player['points']
            i += 1

        #playercount
        data['playerCount'] = i-1

        for player in self.players:
            if player == self.players[master]:
                data["playerType"] = "master"
            else:
                data["playerType"] = "player"
            self.sio.emit('startTrivia', json.dumps(data), room=player['sid'])
        
        #start countdown
        self.startTimer(self.triviaTime, "triviaTimeout")  

    def computeAttempt(self, sid, data):
        str_return = data['attempt']
        player = self.getPlayerFromSID(sid)
        master = self.players[self.currentMaster-1]
        pointsScored = 0

        if data['attempt'].lower() == self.answer.lower():
            str_return = "acertou!"
            pointsScored = self.score - (self.roundScorers*2) - (self.cluePunishment*2)
            player['points'] += pointsScored if (pointsScored > 1) else 1
            master['points'] += (self.score - (self.cluePunishment*2))/len(self.players)
            self.roundScorers += 1
            print("PONTUAÇãO DO PLAYER:  ", player['points'])
        
        elif len(data['attempt']) == len(self.answer):
            Normalized_HD = distance.hamming(list(data['attempt']), list(self.answer))
            if Normalized_HD <= 0.4:
                str_return = "passou perto..."

        self.newEvent('newAttempt', sid, {'attempt': str_return, 'pointsScored': pointsScored})

        if self.roundScorers == len(self.players)-1:
            # bonus se todos acertarem
            master['points'] += self.score/len(self.players)
            self.cancelTimer('triviaTimeout')
            self.startTimer(5, "triviaTimeout2")

    def triviaTimeout(self):
        self.roundScorers = 0

        self.cancelTimer('triviaTimeout')
        self.sio.emit('finishRound', json.dumps({'answer': self.answer}))
        
        self.startTimer(self.roundIsOverTime, "roundIsOverTimeout")
    
        #passa pra tela de final de round

    def finishRound(self):
        self.cancelTimer('roundIsOverTimeout')
        self.startRound()

    def gameOverTimeout(self):
        allPlayers = {p['username'] : p['points'] for p in self.players}
        
        podiumPlayers = sorted(allPlayers.items(), key=lambda item: item[1], reverse=True)

        podium = {'playerNum': len(podiumPlayers)}

        for i in range(len(podiumPlayers)):
            podium['top'+str(i+1)+'_name'] = podiumPlayers[i][0]
            podium['top'+str(i+1)+'_points'] = podiumPlayers[i][1]


        self.sio.emit('gameOver', json.dumps(podium), room=self.ROOM)
        
        self.startTimer(30, "restartGame")

        #fazer um timeout de X segundos para voltar para tela inicial
        
    def giveClueUtil(self):
        reply = [ '_' for i in range(len(self.answer))] # fazer um "_" com o mesmo numero de caracteres da resposta

        num = math.ceil(len(self.answer) * self.percentClue) # quantas letras eu vou dar na dica

        while(num > 0): #enquanto eu nao setei todos os caracteres:
            for i in range(len(self.answer)): #para cada letra da resposta
                if(reply[i] != self.answer[i]): #testar se no reply está "" ou se já foi mostrada
                    if random.random() > 1-self.percentClue: #escolher aleatoriamente se vou mostrar essa letra
                        reply[i] = self.answer[i] #colocar letra no vetor de caracteres
                        num -= 1
                if num <= 0: #curto circuito pra evitar que eu mostre mais letras doq num
                    break

        reply = "".join(reply) #juntar os caracteres numa string
        res = {'clue': reply}

        self.sio.emit('newClue', json.dumps(res), room=self.ROOM)
        self.cluePunishment = 1
        # print(reply)


    #====>> gameplay
    
    ########################### chat/comunicação #############################
    ########################### chat/comunicação #############################
    #emitar mensagem de texto para atualizar o chatbox das paginas
    def newEvent(self, type, sid=None, data=None):
        res = { 'type' : str(type), 'countdown' : int(self.countdown) }
        
        if type == 'join':
            player = self.getPlayerFromSID(sid)
            if player:
                res['username'] = player['username']
                res['playerCount'] = len(self.players)
                self.sio.emit('newEvent', json.dumps(res), room=self.ROOM)

        if type == 'notEnoughPlayers':
            self.sio.emit('newEvent', json.dumps(res), room=self.ROOM)

        if type == 'gameHasBegun':
            self.sio.emit('newEvent', json.dumps(res), room=self.ROOM)
        
        if type == 'disconnect':
            player = self.getPlayerFromSID(sid)
            if player:
                res['username'] = player['username']
                self.sio.emit('newEvent', json.dumps(res), room=self.ROOM)
        
        if type == 'newMessage':
            player = self.getPlayerFromSID(sid)
            if player:
                res['username'] = data['playerType'] + player['username']
                res['message'] = data['message']
                self.sio.emit('newEvent', json.dumps(res), room=self.ROOM)

        if type == 'newAttempt':
            player = self.getPlayerFromSID(sid)
            if player:
                res['username'] = player['username']
                res['attempt'] = data['attempt']

                self.sio.emit('newEvent', json.dumps(res), room=self.ROOM)

                if data['attempt'] == "acertou!":
                    self.sio.emit('newEvent', json.dumps({'type': 'rightAnswer', 'pointsScored': data['pointsScored'], 'countdown' : int(self.countdown)}), room=sid)

        if type == 'gameOver':
            self.sio.emit('newEvent', json.dumps(res), room=self.ROOM)

    #end newevent

    #pegar a info do jogador a partir do SID
    def getPlayerFromSID(self, sid):
        for player in self.players:
            if(player['sid'] == sid):
                return player
        return None
    #endgetPlayerFromSID
    
    ############################### timeout ##################################
    ############################### timeout ##################################
    #inicia o timer
    def startTimer(self, countdown, callback):
        self.sio.emit("startCountdown", json.dumps({"countdown": countdown, "callback" : callback}), sid=self.SIDTIMER)
    #end starttimer

    #cancelar o timer
    def cancelTimer(self, timersCallback):
        self.sio.emit("cancelCountdown", json.dumps({"callback" : timersCallback}), sid=self.SIDTIMER)
    #end canceltimer
    