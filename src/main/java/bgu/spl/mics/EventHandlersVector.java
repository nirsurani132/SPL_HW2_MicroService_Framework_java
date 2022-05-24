package bgu.spl.mics;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class EventHandlersVector {
    volatile AtomicInteger counter;
    volatile Vector<MicroService> handlers;
    public EventHandlersVector(){
        counter = new AtomicInteger(0);
        handlers = new Vector<>();
    }
    public MicroService getNext() {
        MicroService ans;
        if (handlers.size() == 0)
            return null;
        return handlers.get(counter.incrementAndGet()%handlers.size());
    }

    public synchronized void addHandler(MicroService m){
        handlers.add(m);
    }

    public void removeHandler(MicroService m){
        handlers.remove(m);
    }

    public Boolean contains(MicroService microService) {
        return handlers.contains(microService);
    }
}


