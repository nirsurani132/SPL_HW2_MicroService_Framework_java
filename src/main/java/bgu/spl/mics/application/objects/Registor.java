package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.InputFile;

import java.util.concurrent.atomic.AtomicInteger;

public class Registor {

    volatile AtomicInteger numOfActivatedHandlers;
    int inputSumHandlers;

    public Registor(int inputSum){
        numOfActivatedHandlers = new AtomicInteger(0);
        inputSumHandlers = inputSum;
    }

    //thread safe increment the counter
    public void Increment(){
        numOfActivatedHandlers.incrementAndGet();
        if(numOfActivatedHandlers.get() == inputSumHandlers){
            MessageBusImpl mb = MessageBusImpl.getInstance();
            mb.startSending();
        }
    }
}
