package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.Vector;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 * @inv numberOdCors!= null, can process only one data batch at current time.
 */
public class CPU {

    private int numOfCores;
    private Cluster cluster;
    private Collection<Data> dataCollection;
    public CPU (int numOfCores){
        this.numOfCores = numOfCores;
        this.cluster = Cluster.getInstance();
        this.dataCollection = new Vector<>();
    }

    public int getNumOfCores() {
        return numOfCores;
    }

    public void updateClusterCPUTimeUnit(){
        cluster.cpuTimeUnitIncrease();
    }
}
