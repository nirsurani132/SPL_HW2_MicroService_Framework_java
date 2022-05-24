package bgu.spl.mics.application.objects;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;

import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 *
 * @INV type != Null cluster != Null can only store a limited number of
 * processed batches at a time in the VRAM (depends on type)
 * can process train only one model at a time.
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Cluster cluster;
    private AtomicInteger availableRoom;
    private boolean inProcess;
    private LinkedBlockingQueue<DataBatch> vram;
    private DataBatch currDataBatch;
    private int remainTicksToTrain;
    private LinkedList<Event> jobs;
    private Event currEvent;

    private int sentToCpuIndex;
    private Model currModel;

    public GPU(String type) {
        switch (type.trim().toUpperCase(Locale.ROOT)) {
            case "RTX3090":
                this.type = Type.RTX3090;
                availableRoom = new AtomicInteger(32);
                break;
            case "RTX2080":
                this.type = Type.RTX2080;
                availableRoom = new AtomicInteger(16);
                break;
            case "GTX1080":
                this.type = Type.GTX1080;
                availableRoom = new AtomicInteger(8);
                break;
            default:
                throw new IllegalArgumentException("Wrong type of GPU");
        }
        this.cluster = Cluster.getInstance();
        this.inProcess = false;
        vram = new LinkedBlockingQueue<DataBatch>();
        remainTicksToTrain = 0;
        this.jobs = new LinkedList<>();
        sentToCpuIndex = 0;
        currModel = null;

    }

    public void startNewModel() {
        currEvent = jobs.pop(); // get the next job
        inProcess = true; // set in process
        sentToCpuIndex = 0; // reset the index of the sent to cpu
        currModel = ((TrainModelEvent) currEvent).getModel(); // get the model
        System.out.println("🖥"+Thread.currentThread().getName()+": I took a job " + currModel.getName() + " to train");
        while (availableRoom.get() != 0 && sentToCpuIndex < currModel.getSize()) { // split the data to batches
            DataBatch d = new DataBatch(currModel.getData(), sentToCpuIndex); // create new data batch
            sendData(d); // send the batch to the cluster
            availableRoom.decrementAndGet(); // decrement the available room
            sentToCpuIndex += 1000;
        }
    }

    // @PRE GPU has not finished to process current batch data
    // @POST GPU finished current data batch processing,sent it to its cluster(if returned true,otherwise return false).
    public void sendData(DataBatch dataBatch) {
        cluster.sendDataToProcess(dataBatch, this);
    }

    // @PRE the cpu processed the data and sent it back through the cluster.
    // @POST GPU is done training with processedData(if returned true,otherwise return false).
    public TrainModelEvent trainData() {
        if (remainTicksToTrain == 0) { // if we finished training the curr batch
            if (isVramEmpty()){ // finished with the last batch{
                System.out.println("🖥"+Thread.currentThread().getName()+": I am board 😒 with model " + currModel.getName());
                return null;
            }
            currDataBatch = vram.poll();   // get the next batch
            remainTicksToTrain = TickToProcess(); // get the number of ticks to process the next batch
        }
        System.out.println("🖥"+Thread.currentThread().getName()+": I am training model 👍🏼 " + currModel.getName());
        remainTicksToTrain--; // decrement the ticks to train this batch
        this.updateClusterGPUTimeUnit();
        if (remainTicksToTrain == 0) { // if we finished training the batch
            //Finish With Current Event
            if (currDataBatch.data.raiseProcess()) { // we increse the process of the data and if it was the last one:
                System.out.println("🖥"+Thread.currentThread().getName()+": Done with the model 🤬");
                inProcess = false; // we are done with the model
                return (TrainModelEvent) currEvent; // complete the event
            }
            if (sentToCpuIndex == currModel.getSize()) // if there is no more data to send to the cpu
                availableRoom.incrementAndGet(); // increment the available room because we don't need this space for the next batch
            else // otherwise, use this space for the next batch:
            {
//                System.out.println(Thread.currentThread().getName() + ": sending next batch, my available room is " + availableRoom.get() + " and my sent to cpu index is " + sentToCpuIndex);
                sendData(new DataBatch(currModel.getData(), sentToCpuIndex)); // send the next batch to the cluster
                sentToCpuIndex += 1000; // increment the sent to cpu index
            }
        }

        return null; // null means Process not finished yet
    }

    // @PRE gets a model for testing from the message bus
    // @POST will return true with a probability of 0.1 for MSc student, and 0.2 for PhD student.
    public void testModel(TestModelEvent e) {
        double randomRate;
        if (e.getStudent().getStatus() == Student.Degree.MSc) {
            randomRate = 0.6;
        } else
            randomRate = 0.8;
        e.getModel().setStatus(Model.ModelStatus.Tested);
        if (Math.random() < randomRate) {
//            System.out.println("🤪 I passed the test");
            e.getModel().setResult(Model.ModelResult.Good);
        } else {
//            System.out.println("😭 I failed the test");
            e.getModel().setResult(Model.ModelResult.Bad);
        }
    }


    public String getTypeString() {
        return type.toString();
    }

    public Type getType() {
        return type;
    }

    public boolean isInProcess() {
        return inProcess;
    }

    public void addJob(Event e) {
        jobs.add(e);
    }

    public int getJobsSize() {
        return jobs.size();
    }

    public boolean isJobsEmpty() {
        return jobs.isEmpty();
    }

    public boolean isVramEmpty() {
        return vram.isEmpty();
    }

    public void addToVram(DataBatch dataBatch) {
        vram.add(dataBatch);
    }

    private int TickToProcess() {
        switch (this.type) {
            case RTX3090:
                return 1;
            case RTX2080:
                return 2;
            case GTX1080:
                return 4;
        }
        return 0;
    }

    public void updateClusterGPUTimeUnit() {
        cluster.gpuTimeUnitIncrease();
    }

    public void markModelAsDone(String modelName) {
        cluster.markModelAsDone(modelName);
    }

    public Event getCurrEvent() {
        return currEvent;
    }

    public void shutdown() {
        cluster.shutdown();
    }

    public LinkedList<Event> getJobs() {
        return this.jobs;
    }
}

