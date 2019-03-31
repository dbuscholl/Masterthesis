package utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MathToolbox {
    public static float meterDistanceBetween(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    public static double mean(ArrayList<Integer> values) {
        long sum = 0;
        for (int i = 0; i < values.size(); i++) {
            sum += values.get(i);
        }
        return (double) sum / values.size();
    }

    public static double median(ArrayList<Integer> values) {
        Collections.sort(values);
        if (values.size() % 2 == 0) {
            return ((double) values.get(values.size() / 2) + (double) values.get(values.size() / 2 - 1)) / 2;
        } else {
            return (double) values.get(values.size() / 2);
        }
    }

    /**
     * calculate mode of array of integers
     * @param values array of which the mode should be calculated
     * @return int array with item 0 containg the mode value and item 1 how often it was inside the values array
     */
    public static int[] mode(ArrayList<Integer> values) {
        int maxValue = 0, maxCount = 0;

        for (int i = 0; i < values.size(); ++i) {
            int count = 0;
            for (Integer integer : values) {
                if (integer.equals(values.get(i))) ++count;
            }
            if (count > maxCount) {
                maxCount = count;
                maxValue = values.get(i);
            }
        }

        return new int[]{maxValue, maxCount};
    }

    public static int castToIntWithPossibleLoss(long l) {
        if (l < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        if (l > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) l;
    }
}
