package bgu.spl.mics.application.objects;

import org.junit.Test;

import static org.junit.Assert.*;

public class CPUTest {

    @Test
    public void getNumOfCores() {
        CPU cp = new CPU(1);
        assertEquals(cp.getNumOfCores(), 1);
    }

    @Test
    public void updateClusterCPUTimeUnit() {
        CPU cp = new CPU(1);
        assertEquals(Cluster.getInstance().getCpuTimeUnit().get(),0);
        cp.updateClusterCPUTimeUnit();
        assertEquals(Cluster.getInstance().getCpuTimeUnit().get(), 1);
    }
}