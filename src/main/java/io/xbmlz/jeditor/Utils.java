package io.xbmlz.jeditor;

import java.util.Map;

public class Utils {

    public static String getMapFirstKey(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
