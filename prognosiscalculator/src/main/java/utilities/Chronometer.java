package utilities;

import java.util.ArrayList;

/**
 * This is an extremely tiny but useful tool to measure execution times! It contains an array where all times are being
 * stored and can calculate differences between them.
 */
public class Chronometer {
    private ArrayList<Long> times;

    public Chronometer() {
        times = new ArrayList<>();
    }

    /**
     * adds a new time marker
     */
    public void addNow() {
        times.add(System.currentTimeMillis());
    }

    /**
     * gets the difference between the last two markers in milliseconds
     * @return
     */
    public long getLastDifferece() {
        if (times.size() > 1) {
            return times.get(times.size() - 1) - times.get(times.size() - 2);
        }
        return 0;
    }

    /**
     * gets the timedefference between the given and the next index
     * @param index
     * @return milliseconds between this and next index
     */
    public long getDifference(int index) {
        if (index > times.size() - 2) {
            return 0;
        } else {

        }
        return times.get(index + 1) - times.get(index);
    }

    // TODO: function public long getDifference(int from, int to)

    /**
     * resets the list of markers (for performance reasons)
     */
    public void clear() {
        times.subList(times.size() - 1, times.size()).clear();
    }
}
