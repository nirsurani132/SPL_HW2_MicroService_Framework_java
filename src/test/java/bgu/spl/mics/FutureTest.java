package bgu.spl.mics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FutureTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void get() throws InterruptedException {
        Future fu = new Future<Integer>();
        Integer t = new Integer(5);
        fu.resolve(t);
        assertEquals(fu.get(),t);
    }

    @Test
    public void resolve() throws InterruptedException {
        Future fu = new Future<Integer>();
        Integer t = new Integer(5);
        assertFalse(fu.isDone()); //Assert Future done by Resolve function
        fu.resolve(t);
        assertTrue(fu.isDone());
        assertEquals(fu.get(),t);
    }

    @Test
    public void isDone() {
        Future fu = new Future<Integer>();
        Integer t = new Integer(5);
        assertFalse(fu.isDone());
        fu.resolve(t);
        assertTrue(fu.isDone());
    }
}