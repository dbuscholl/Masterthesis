package Static;

import org.apache.log4j.Logger;

/**
 * An uncaught exception handler waites for any exception which occurs during the complete process which has not been
 * catched by any other class. It is the final catcher and in this case returns the exception and details to it.
 */
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static Logger log = Logger.getLogger(UncaughtExceptionHandler.class.getName());
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Thread: " + t.getName() + " threw exception!",e);
    }
}
