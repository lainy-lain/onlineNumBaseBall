package pnu.termproject.onlinenumbaseball;

import java.util.ArrayList;

public class RoomIdManage {
    private ArrayList<Integer> available = new ArrayList<>();
    private int maxId = 0;

    public void add(int id) {
        available.add(id);
    }

    public int receiveId() {
        int id;
        if (available.isEmpty()) {
            id = maxId + 1;
            maxId++;
        }
        else {
            id = available.get(0);
            available.remove(0);
        }
        return id;
    }

    public ArrayList<Integer> getAvailable() {
        return available;
    }

    public int getMaxId() {
        return maxId;
    }
}
