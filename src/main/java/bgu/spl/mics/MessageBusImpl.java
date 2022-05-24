package bgu.spl.mics;

import bgu.spl.mics.application.messages.TrainModelEvent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

    private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> queueMap;
    private ConcurrentHashMap<Class<? extends Event>, EventHandlersVector> handlers;
    private ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>> broadcastServices;
    private ConcurrentHashMap<Event, Future> futureByEvent;
    private Boolean sendFlag;
    private final Object sendingLock;
    private final Object broadcastLocker;
    private final Object handlersLock;

    private static class MBHolder{
        private static MessageBusImpl mb = new MessageBusImpl();
    }
    private MessageBusImpl() {
        queueMap = new ConcurrentHashMap<>();
        handlers = new ConcurrentHashMap<>();
        broadcastServices = new ConcurrentHashMap<>();
        handlers.put(TrainModelEvent.class,new EventHandlersVector());
        sendFlag = false;
        sendingLock = new Object();
        broadcastLocker = new Object();
        handlersLock = new Object();
        futureByEvent = new ConcurrentHashMap<>();
    }

    @Override
    public void startSending() {
        sendFlag=true;
        synchronized (sendingLock){
            this.sendingLock.notifyAll();
        }
    }

    public static MessageBusImpl getInstance() {
        return MBHolder.mb;
    }


    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        if (handlers.get(type) == null) {
            synchronized (handlersLock){
                if (handlers.get(type) == null)
                    handlers.put(type, new EventHandlersVector());
            }
        }
        handlers.get(type).addHandler(m);
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        if (broadcastServices.get(type) == null) {
            synchronized (broadcastLocker){
                if (broadcastServices.get(type) == null)
                    broadcastServices.put(type, new LinkedBlockingQueue<>());  // TODO: check why regular vector
            }
        }
        broadcastServices.get(type).add(m);
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        futureByEvent.get(e).resolve(result);
//        futureByEvent.remove(e); // TODO: fix me
    }

    @Override
    public void sendBroadcast(Broadcast b) throws InterruptedException {
        if(!sendFlag)
            synchronized (sendingLock){
                    while (!sendFlag)
                        this.sendingLock.wait();
            }
        if (broadcastServices.get(b.getClass()) != null)
            for (MicroService m : broadcastServices.get(b.getClass())) {
                queueMap.get(m).add(b);
            }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) throws InterruptedException {
        // TODO: what if there is no one that can handle it? no one subscribe
        if(!sendFlag)
            synchronized (sendingLock){
                    while (!sendFlag)
                        this.sendingLock.wait();
            }
        Future<T> eventRes = new Future<>();
        futureByEvent.put(e,eventRes);
        MicroService handler = handlers.get(e.getClass()).getNext();
        if (handler == null)
            throw new InterruptedException();
        queueMap.get(handler).add(e);
        return eventRes;
    }

    @Override
    public void register(MicroService m) {
        queueMap.put(m, new LinkedBlockingQueue<>());
    }

    @Override
    public void unregister(MicroService m) {
        System.out.println("unregistering "+ Thread.currentThread().getName());
        for (EventHandlersVector handler : handlers.values())
            handler.removeHandler(m);
        for (LinkedBlockingQueue<MicroService> broadcastQueue : broadcastServices.values())
            broadcastQueue.remove(m);
        queueMap.remove(m);
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        if (queueMap.get(m) == null)
            throw new InterruptedException("the service is not registered");
        return queueMap.get(m).take();
    }

    public Boolean isMCRegisteredToEvent(MicroService microService, Class<? extends Event> type){
        return handlers.get(type).contains(microService);
    }
    public Boolean isMCRegisteredToBroadcast(MicroService microService, Class<? extends Broadcast> type){
        return broadcastServices.get(type).contains(microService);
    }

    public Boolean isMCRegisteredToMB(MicroService microService){
        return queueMap.get(microService) != null;
    }
}