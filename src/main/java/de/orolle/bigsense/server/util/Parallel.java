package de.orolle.bigsense.server.util;

import java.util.HashMap;

import org.vertx.java.core.Handler;

/**
 * Helper Class to execute code in parallel and notify listener after
 * all code is executed.
 *
 * @author Oliver Rolle
 * @param <E> 	result type for parallel execution
 */
public class Parallel<E>{
	
	/** The registered handler. */
	protected int registeredHandler = 0;
	
	/** The done handler. */
	protected int doneHandler = 0;
	
	/** The result. */
	protected final HashMap<String,E> result = new HashMap<String, E>();

	/** The done. */
	protected final Handler<HashMap<String, E>> done;

	/** The fixed size. */
	protected final boolean fixedSize;

	/**
	 * Unlimited number of parallel executions.
	 *
	 * @param <E> the element type
	 * @param done 	Listener which is called after everything is executed
	 * @return 	a Parallel handler instance
	 */
	public static <E> Parallel<E> Completed(Handler<HashMap<String, E>> done){
		return new Parallel<E>(-1 , done);
	}

	/**
	 * Limited number of parallel executions.
	 *
	 * @param <E> the element type
	 * @param size 	Number of parallel executions
	 * @param done 	Listener which is called after everything is executed
	 * @return 	a Parallel handler instance
	 */
	public static <E> Parallel<E> Completed(int size, Handler<HashMap<String, E>> done){
		return new Parallel<E>(size, done);
	}

	/**
	 * Construct parallel execution.
	 *
	 * @param size 	Number of parallel executions; -1 is unlimited
	 * @param done 	Handler which is called after parallel execution is completed
	 */
	private Parallel(int size, Handler<HashMap<String, E>> done) {
		this.done = done;
		fixedSize = size > 0;
		if(fixedSize){
			this.registeredHandler = size;
		}
	}

	/**
	 * Generates a named result handler for a single execution.
	 *
	 * @param name 	name of the handler
	 * @param h 	Handler which is called of a this single execution is completed
	 * @return 	handler to receive a computation result for a single execution
	 */
	public Handler<E> handler(final String name, final Handler<E> h){
		final String index = name == null? registeredHandler+"" : name;
		taskAddHandler();

		return new Handler<E>(){
			@Override
			public void handle(E arg0) {
				taskFinishHandler();
				result.put(index, arg0);

				if(h != null){
					h.handle(arg0);
				}

				if(doneHandler == registeredHandler){
					done.handle(result);
				}
			}
		};
	}

	/**
	 * Generates a named result handler for a single execution.
	 *
	 * @return 	handler to receive a computation result for a single execution
	 */
	public Handler<E> handler(){
		return handler(null, null);
	}

	/**
	 * Generates a named result handler for a single execution.
	 *
	 * @param name 	name of the handler
	 * @return 	handler to receive a computation result for a single execution
	 */
	public Handler<E> handler(String name){
		return handler(name, null);
	}

	/**
	 * Counts finished executions.
	 */
	private synchronized void taskFinishHandler() {
		doneHandler++;
	}

	/**
	 * Counts added executions.
	 */
	private synchronized void taskAddHandler() {
		if(!fixedSize){
			registeredHandler++;
		}
	}
}
