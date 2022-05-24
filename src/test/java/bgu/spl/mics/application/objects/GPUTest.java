package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.application.services.StudentService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class GPUTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void sendData() throws InterruptedException {
        Cluster cluster = Cluster.getInstance();
        GPU gpu = new GPU("RTX3090");
        DataBatch dataBatch = new DataBatch(new Data("images",10),0);
        gpu.sendData(dataBatch);
        assertEquals (cluster.takeDataToProcess().dataBatch,dataBatch);
    }


    @Test
    public void testModel() {
        GPU gpu = new GPU("RTX3090");
        Student st = new Student("1", "a", "MSc");
        Model model = new Model("name", new Data("text",10), st);
        gpu.testModel(new TestModelEvent(model,st));
        assertNotEquals(model.getResult(),Model.ModelResult.None);
    }
}