import schedule
import threading
import time

class ScheduleHandler():
    def __init__(self):
        self.cease_continuous_run = None
    #end init

    def start(self, interval=1):
        self.cease_continuous_run = threading.Event()

        class ScheduleThread(threading.Thread):
            @classmethod
            def run(cls):
                while not self.cease_continuous_run.is_set():
                    schedule.run_pending()
                    time.sleep(interval)

        continuous_thread = ScheduleThread()
        continuous_thread.setDaemon(True)
        continuous_thread.start()
    #end start

    def terminate(self):
        if self.cease_continuous_run:
            self.cease_continuous_run.set()
    #end terminate



#################################

def job_that_executes_once(nome1, nome2, nome3):
    # Do some work that only needs to happen once...
    print("olaaaa " + nome1 + nome2+nome3)
    # return schedule.CancelJob

# schedule.every().day.at('22:30').do(job_that_executes_once)
# job = schedule.every(5).seconds.do(job_that_executes_once)


#################################

# # Start the background thread
# game = Game("xd")
# scheduler = ScheduleHandler(game)
# scheduler.start()

# # Do some other things...
# i = 0
# while i<21:
#     i+=1
#     print("segundo: " + str(i))
#     if i == 11:
#         schedule.cancel_job(job)
        
#     time.sleep(1)

# # Stop the background thread
# scheduler.terminate()