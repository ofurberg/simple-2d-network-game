package lab3.networkgame.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UniqueIdentifier {

    private static List<Integer> ids = new ArrayList<Integer>();
    private static final int RANGE = 50;

    private static int index = 3;

    static {
        for (int i = 3; i < RANGE; i++) {
            ids.add(i);
        }
        Collections.shuffle(ids);
    }
    private UniqueIdentifier() {

    }

    public static int getIdentifier() {
        if (index > ids.size()-1) {
            index = 3;
        }
        return ids.get(index++);
    }
}
