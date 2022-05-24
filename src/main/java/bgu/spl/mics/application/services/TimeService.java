package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.LastTick;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{
	int tickTime;
	int duration;

	public TimeService(int tickTime, int duration) {
		super("TimeService");
		this.duration = duration;
		this.tickTime = tickTime;
	}

	@Override
	protected void initialize() throws InterruptedException {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (duration > 0) {
					duration --;
//					System.out.println("⏰ sending tick " + Thread.currentThread().getName());
					try {
						sendBroadcast(new TickBroadcast());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				else {
//					System.out.println("⏰ sending last tick " + Thread.currentThread().getName());
					try {
						sendBroadcast(new LastTick());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					timer.cancel();
					terminate();
				}
			}
		};
		timer.scheduleAtFixedRate(task, 0, tickTime);
	}


}
