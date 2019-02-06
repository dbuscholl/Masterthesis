package Static;

import org.apache.log4j.Logger;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static Logger log = Logger.getLogger(UncaughtExceptionHandler.class.getName());
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Thread: " + t.getName() + " threw exception!",e);
    }
}
