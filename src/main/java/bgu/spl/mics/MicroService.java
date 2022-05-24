package bgu.spl.mics;

import bgu.spl.mics.application.messages.LastTick;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.application.services.TimeService;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The MicroService is an abstract class that any micro-service in the system
 * must extend. The abstract MicroService class is responsible to get and
 * manipulate the singleton {@link MessageBus} instance.
 * <p>
 * Derived classes of MicroService should never directly touch the message-bus.
 * Instead, they have a set of internal protected wrapping methods (e.g.,
 * {@link #sendBroadcast(bgu.spl.mics.Broadcast)}, {@link #sendBroadcast(bgu.spl.mics.Broadcast)},
 * etc.) they can use. When subscribing to message-types,
 * the derived class also supplies a {@link Callback} that should be called when
 * a message of the subscribed type was taken from the micro-service
 * message-queue (see {@link MessageBus#register(bgu.spl.mics.MicroService)}
 * method). The abstract MicroService stores this callback together with the
 * type of the message is related to.
 * 
 * Only private fields and methods may be added to this class.
 * <p>
 */
public abstract class MicroService implements Runnable {

    private boolean terminated = false;
    private final String name;
    private ConcurrentHashMap<Class, Callback> callBackByEvent;
    private MessageBusImpl mb = MessageBusImpl.getInstance();

    /**
     * @param name the micro-service name (used mainly for debugging purposes -
     *             does not have to be unique)
     */
    public MicroService(String name) {
        this.name = name;
        this.callBackByEvent = new ConcurrentHashMap<>();
    }

    /**
     * Subscribes to events of type {@code type} with the callback
     * {@code callback}. This means two things:
     * 1. Subscribe to events in the singleton event-bus using the supplied
     * {@code type}
     * 2. Store the {@code callback} so that when events of type {@code type}
     * are received it will be called.
     * <p>
     * For a received message {@code m} of type {@code type = m.getClass()}
     * calling the callback {@code callback} means running the method
     * {@link Callback#call(java.lang.Object)} by calling
     * {@code callback.call(m)}.
     * <p>
     * @param <E>      The type of event to subscribe to.
     * @param <T>      The type of result expected for the subscribed event.
     * @param type     The {@link Class} representing the type of event to
     *                 subscribe to.
     * @param callback The callback that should be called when messages of type
     *                 {@code type} are taken from this micro-service message
     *                 queue.
     * @pre {@code type != null}, {@code callback != null}
     * @post the micro-service is subscribed to events of type {@code type}
     */
    protected final <T, E extends Event<T>> void subscribeEvent(Class<E> type, Callback<E> callback) {
        callBackByEvent.put(type, callback);
        mb.subscribeEvent(type,this);
    }

    /**
     * Subscribes to broadcast message of type {@code type} with the callback
     * {@code callback}. This means two things:
     * 1. Subscribe to broadcast messages in the singleton event-bus using the
     * supplied {@code type}
     * 2. Store the {@code callback} so that when broadcast messages of type
     * {@code type} received it will be called.
     * <p>
     * For a received message {@code m} of type {@code type = m.getClass()}
     * calling the callback {@code callback} means running the method
     * {@link Callback#call(java.lang.Object)} by calling
     * {@code callback.call(m)}.
     * <p>
     * @param <B>      The type of broadcast message to subscribe to
     * @param type     The {@link Class} representing the type of broadcast
     *                 message to subscribe to.
     * @param callback The callback that should be called when messages of type
     *                 {@code type} are taken from this micro-service message
     *                 queue.
     * @pre {@code type != null}, {@code callback != null}
     * @post the micro-service is subscribed to broadcast messages of type
     */
    protected final <B extends Broadcast> void subscribeBroadcast(Class<B> type, Callback<B> callback) {
        callBackByEvent.put(type, callback);
        mb.subscribeBroadcast(type, this);
    }

    /**
     * Sends the event {@code e} using the message-bus and receive a {@link Future<T>}
     * object that may be resolved to hold a result. This method must be Non-Blocking since
     * there may be events which do not require any response and resolving.
     * <p>
     * @param <T>       The type of the expected result of the request
     *                  {@code e}
     * @param e         The event to send
     * @return  		{@link Future<T>} object that may be resolved later by a different
     *         			micro-service processing this event.
     * 	       			null in case no micro-service has subscribed to {@code e.getClass()}.
     * @pre {@code e != null}
     * @post the micro-service has sent the event {@code e} using the message-bus
     */
    protected final <T> Future<T> sendEvent(Event<T> e) throws InterruptedException {
        return mb.sendEvent(e);
    }

    /**
     * A Micro-Service calls this method in order to send the broadcast message {@code b} using the message-bus
     * to all the services subscribed to it.
     * <p>
     * @param b The broadcast message to send
     * @pre {@code b != null}
     * @post the micro-service has sent the broadcast message {@code b} using the message-bus
     */
    protected final void sendBroadcast(Broadcast b) throws InterruptedException {
        mb.sendBroadcast(b);
    }

    /**
     * Completes the received request {@code e} with the result {@code result}
     * using the message-bus.
     * <p>
     * @param <T>    The type of the expected result of the processed event
     *               {@code e}.
     * @param e      The event to complete.
     * @param result The result to resolve the relevant Future object.
     *               {@code e}.
     * @pre {@code e != null}, {@code result != null}
     * @post the micro-service has completed the event {@code e} with the result
     */
    protected final <T> void complete(Event<T> e, T result) {
        mb.complete(e, result);
    }

    /**
     * this method is called once when the event loop starts.
     *
     * @pre none
     * @post the micro-service has started
     */
    protected abstract void initialize() throws InterruptedException;

    /**
     * Signals the event loop that it must terminate after handling the current
     * message.
     * @pre the micro-service is running
     * @post the micro-service has terminated
     */
    protected final void terminate() {
        this.terminated = true;
        this.shutdown();
        mb.unregister(this);
        Thread.currentThread().interrupt();
    }

    protected void shutdown() {

    }
    /**
     * @return the name of the service - the service name is given to it in the
     *         construction time and is used mainly for debugging purposes.
     * @pre none
     * @post returns the name of the service
     */
    public final String getName() {
        return name;
    }

    /**
     * The entry point of the micro-service. TODO: you must complete this code
     * otherwise you will end up in an infinite loop.
     *
     * @pre none
     * @post the micro-service has started
     */
    @Override
    public final void run() {
        mb.register(this);
        try {
            initialize();
        } catch (InterruptedException e) {
            this.terminate();
        }
        this.subscribeBroadcast(LastTick.class,(LastTick ms)->{
//        System.out.println(Thread.currentThread().getName() + ": Dont kill me 😱");
        this.terminate();
        });
        while (!terminated && !(this instanceof TimeService)) {
            try {
                Message m = mb.awaitMessage(this);
                callBackByEvent.get(m.getClass()).call(m);
            } catch (InterruptedException e) {
                this.terminate();
            }
        }
    }
}
