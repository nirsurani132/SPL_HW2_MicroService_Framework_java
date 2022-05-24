package bgu.spl.mics.application.objects;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private LinkedBlockingQueue<UnprocessedPair> unProcessedDataBatches;
	private ConcurrentHashMap<Model, GPU> gpuByModel;
	AtomicInteger batchesProcessed;
	AtomicInteger cpuTimeUnit;
	AtomicInteger gpuTimeUnit;
	private LinkedBlockingQueue<String> trainedModels;
	boolean isShutDown;


	/**
     * Retrieves the single instance of this class.
     */
	private static class ClusterHolder{
		private static Cluster instance = new Cluster();
	}

	public static Cluster getInstance() {
		return ClusterHolder.instance;
	}

	private Cluster(){
		unProcessedDataBatches = new LinkedBlockingQueue<>();
		gpuByModel = new ConcurrentHashMap<>();
		batchesProcessed = new AtomicInteger(0);
		cpuTimeUnit = new AtomicInteger(0);
		gpuTimeUnit = new AtomicInteger(0);
		this.trainedModels = new LinkedBlockingQueue<>();
		isShutDown = false;
	}

	public void sendDataToProcess(DataBatch dataBatch, GPU sender){
//		System.out.println(sender.getName() + " sent a new batch");
		unProcessedDataBatches.add(new UnprocessedPair(dataBatch, sender));
	}

	public UnprocessedPair takeDataToProcess() throws InterruptedException {
		try {
			if( unProcessedDataBatches.isEmpty()) {
				System.out.println(Thread.currentThread().getName() + " is waiting for a new batch - waste of time 👎🏼");
				return null;
			}
			System.out.println(Thread.currentThread().getName() + " is taking a new batch 💪🏼" + unProcessedDataBatches.peek().gpu);
			return unProcessedDataBatches.take();
		}
		catch (InterruptedException e){
			throw e;
		}
	}

	public void sendProcessedData(DataBatch dataBatch, GPU gpu){
		batchesProcessed.incrementAndGet();
		gpu.addToVram(dataBatch);
	}

	public void markModelAsDone(String modelName){
		trainedModels.add(modelName);
	}

	public void cpuTimeUnitIncrease(){
		cpuTimeUnit.incrementAndGet();
	}
	public void gpuTimeUnitIncrease(){
		gpuTimeUnit.incrementAndGet();
	}

	public AtomicInteger getCpuTimeUnit() {return cpuTimeUnit;}

	public AtomicInteger getGpuTimeUnit() {return gpuTimeUnit;}

	public AtomicInteger getBatchesProcessed() {return batchesProcessed;}

	public synchronized void shutdown(){
		if(!isShutDown) {
			isShutDown = true;
			ResultMaker wr = ResultMaker.getInstance();
			wr.setBatchesProcessed(batchesProcessed.get());
			wr.setCpuTimeUsed(cpuTimeUnit.get());
			wr.setGpuTimeUsed(gpuTimeUnit.get());
			System.out.println("number of batches: " + batchesProcessed.get());
			System.out.println("cpu time unit: " + cpuTimeUnit.get());
			System.out.println("gpu time unit: " + gpuTimeUnit.get());
			System.out.println("number of trained models: " + trainedModels.size());
		}
	}
}
