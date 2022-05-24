package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.*;


/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    // Fields
    GPU gpu;
    Registor reg;

    // Constructors
    public GPUService(GPU gpu, Registor registor) {
        super(gpu.getTypeString());
        this.gpu = gpu;
        this.reg = registor;

    }

    @Override
    protected void initialize() throws InterruptedException {
//        System.out.println("🖥 I am a new GPU");
        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast tickBroadcast) -> {
                    handleTick();
                }
        );
        this.subscribeEvent(TrainModelEvent.class, (TrainModelEvent e) -> {
            System.out.println("🖥 I am " + Thread.currentThread().getName() + " got a new model from: " + e.getSenderName());
            e.getModel().setStatus(Model.ModelStatus.Training);
            gpu.addJob(e);
        });
        this.subscribeEvent(TestModelEvent.class,(TestModelEvent e) -> {
//            System.out.println("🖥 I am " + Thread.currentThread().getName() + " Start Testing 👨🏽‍🔬 Model: " + e.getModel().getName());
            gpu.testModel(e);
            complete(e,e.getModel());
        });
        reg.Increment();
    }

    private void handleTick() {
//        System.out.println("🖥 Tick broadcast received " + Thread.currentThread().getName());
        if (!gpu.isJobsEmpty() && !gpu.isInProcess()) { // if there is a job to do and we are not in process of another job
            gpu.startNewModel(); // start new model
        }
        if (gpu.isInProcess()) { // if we are in process of a model
            TrainModelEvent event = gpu.trainData();
            if (event != null) { // if we finished training the all data of the model
                event.getModel().setStatus(Model.ModelStatus.Trained);
                gpu.markModelAsDone(event.getModel().getName());
                complete(event, event.getModel()); // process batch
            }
        }
    }

    @Override
    protected void shutdown() {
//        Event currEvent = gpu.getCurrEvent();
//        Model currModel;
        gpu.shutdown();
//        if(currEvent instanceof TrainModelEvent) {
//            currModel = ((TrainModelEvent) currEvent).getModel();
//            currModel.setStatus(Model.ModelStatus.Interrupted);
//            complete(gpu.getCurrEvent(), currModel);
//        }
//        for (Event event : gpu.getJobs()) {
//            if(event instanceof TrainModelEvent) {
//                currModel = ((TrainModelEvent) event).getModel();
//                currModel.setStatus(Model.ModelStatus.Interrupted);
//                complete(event, currModel);
//            }
//        }
    }
}
