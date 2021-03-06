package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	private T ans;
	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		ans = null;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * @pre ans ==null
	 * @post return the result of type T if it is available, if not wait until it is available.
     */
	public synchronized T get() throws InterruptedException {
		while (!isDone()){
			this.wait();
		}
		return ans;
	}
	
	/**
     * Resolves the result of this Future object.
	 * @param result	the result of the computation.
	 * @pre ans == null
	 * @post ans == result.
     */
	public synchronized void resolve (T result) {
		if(result != null) {
			ans = result;
			this.notifyAll();
		}
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
	 * @pre none
	 * @post return true if this object has been resolved, false otherwise
     */
	public boolean isDone() {
		return (ans != null);
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
	 * @pre isDone() == false
	 * @post return the result of type T if it is available, if not,
	 * 	     wait for {@code timeout} TimeUnits {@code unit}. If time has
	 * 	     elapsed, return null.
     */
	public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException {
		if (!isDone())
			this.wait(unit.toMillis(timeout));
		if(!isDone())
			return ans;
		return null;
	}

}
