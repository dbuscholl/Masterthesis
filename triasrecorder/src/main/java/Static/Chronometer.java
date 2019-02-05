package Static;

import java.util.ArrayList;
import java.util.List;

public class Chronometer {
    private ArrayList<Long> times;

    public Chronometer() {
        times = new ArrayList<>();
    }

    public void addNow() {
        times.add(System.currentTimeMillis());
    }

    public long getLastDifferece() {
        if (times.size() > 1) {
            return times.get(times.size() - 1) - times.get(times.size() - 2);
        }
        return 0;
    }

    public long getDifference(int index) {
        if (index > times.size() - 2) {
            return 0;
        } else {

        }
        return times.get(index + 1) - times.get(index);
    }

    public void clear() {
        times.subList(times.size() - 1, times.size()).clear();
    }
}
