package bgu.spl.mics;

import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.application.services.StudentService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusImplTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void subscribeEvent() {
        MessageBusImpl mb = MessageBusImpl.getInstance();
        StudentService service = new StudentService(new Student("1", "a", "MSc"));
        mb.register(service);
        mb.subscribeEvent(TrainModelEvent.class, service);
        assertTrue(mb.isMCRegisteredToEvent(service, TrainModelEvent.class));
    }

    @Test(timeout = 500)
    public void subscribeBroadcast() {
        MessageBusImpl mb = MessageBusImpl.getInstance();
        StudentService service = new StudentService(new Student("1", "a", "MSc"));
        mb.register(service);
        mb.subscribeBroadcast(TickBroadcast.class, service);
        assertTrue(mb.isMCRegisteredToBroadcast(service, TickBroadcast.class));
    }

    @Test
    public void complete() throws InterruptedException {
        MessageBusImpl mb = MessageBusImpl.getInstance();
        Student st = new Student("1", "a", "MSc");
        StudentService service = new StudentService(st);
        mb.register(service);
        mb.subscribeEvent(TrainModelEvent.class, service);
        mb.startSending();
        Model model = new Model("name", new Data("text",10), st);
        TrainModelEvent e = new TrainModelEvent(service.getName(), model);
        Future<Model> f = mb.sendEvent(e);
        assertFalse(f.isDone());
        mb.complete(e,model);
        assertTrue(f.isDone());
    }

    @Test
    public void sendBroadcast() throws InterruptedException {
        MessageBusImpl mb = MessageBusImpl.getInstance();
        Student st = new Student("1", "a", "MSc");
        StudentService service = new StudentService(st);
        mb.register(service);
        mb.subscribeBroadcast(TickBroadcast.class, service);
        mb.startSending();
        mb.sendBroadcast(new TickBroadcast());
        try {
            assertNotNull(mb.awaitMessage(service));
        }
        catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    public void sendEvent() {

    }

    @Test
    public void register() {
        MessageBusImpl mb = MessageBusImpl.getInstance();
        Student st = new Student("1", "a", "MSc");
        StudentService service = new StudentService(st);
        mb.register(service);
        assertTrue(mb.isMCRegisteredToMB(service));
    }

    @Test
    public void unregister() {
        MessageBusImpl mb = MessageBusImpl.getInstance();
        Student st = new Student("1", "a", "MSc");
        StudentService service = new StudentService(st);
        mb.register(service);
        assertTrue(mb.isMCRegisteredToMB(service));
        mb.unregister(service);
        assertFalse(mb.isMCRegisteredToMB(service));
    }

    @Test (timeout = 500)
    public void awaitMessage() throws InterruptedException {
        MessageBusImpl mb = MessageBusImpl.getInstance();
        Student st = new Student("1", "a", "MSc");
        StudentService service = new StudentService(st);
        mb.register(service);
        mb.subscribeBroadcast(TickBroadcast.class, service);
        mb.startSending();
        mb.sendBroadcast(new TickBroadcast());
        try {
            assertNotNull(mb.awaitMessage(service));
        }
        catch (InterruptedException e) {
            fail();
        }
    }
}