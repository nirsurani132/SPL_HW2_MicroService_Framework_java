package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.*;

import java.util.LinkedList;

/**
 * CPU service is responsible for handling the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    LinkedList<Message> jobs;
    Event currEvent;
    CPU cpu;
    Registor reg;
    Cluster cluster;
    Boolean inProcess;
    int remainTicks;
    DataBatch currData;
    GPU currGpu;
    public CPUService(CPU cpu,Registor reg) {
        super(Integer.toString(cpu.getNumOfCores()));
        this.cpu = cpu;
        this.reg = reg;
        this.cluster = Cluster.getInstance();
        inProcess = false;
    }

    @Override
    protected void initialize() {
        MessageBusImpl mb = MessageBusImpl.getInstance();
        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast tickBroadcast) -> {
//                    System.out.println("👾 Tick broadcast received " + Thread.currentThread().getName());
                    if(!inProcess) {
                        UnprocessedPair pair = cluster.takeDataToProcess();
                        if (pair == null) {
                            return;
                        }
                        currData = pair.dataBatch;
                        currGpu = pair.gpu;
                        inProcess=true;
                        switch ((Data.Type)currData.getType()){
                            case Text:
                                remainTicks = (32/ cpu.getNumOfCores()) * 2;
                                break;
                            case Images:
                                remainTicks = (32/ cpu.getNumOfCores()) * 4;
                                break;
                            case Tabular:
                                remainTicks = (32/ cpu.getNumOfCores());
                                break;
                        }
                    }
                    remainTicks--;
                    cpu.updateClusterCPUTimeUnit();
                    if(remainTicks == 0) {
//                        System.out.println(Thread.currentThread().getName() + ": Done with the Betch");
                        cluster.sendProcessedData(currData,currGpu);
                        currData = null;
                        inProcess= false;
                    }
                }
        );
        reg.Increment();
    }
}
