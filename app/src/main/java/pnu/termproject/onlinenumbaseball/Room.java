package pnu.termproject.onlinenumbaseball;

public class Room {
    private String roomName;
    private String user1Id;
    private String user2Id;
    private int owner = 1;
    private int numUser = 1;
    private boolean user1State = true;
    private boolean user2State = false;

    public Room(String name, String userId) {
        roomName = name;
        user1Id = userId;
    }

    // 유저 입장
    public void addUser(String userId) {
        if (user1State) {
            user2State = true;
            user2Id = userId;
        }
        else {
            user1State = true;
            user1Id = userId;
        }
        numUser++;
    }

    // 유저 퇴장, 방에 2명 있을 때
    // 1명 있을 때는 방을 없애야 함
    public void exitUser(String userId) {
        if (userId.equals(user1Id)) { // 1 자리에 있을 때
            user1State = false;
            if (owner == 1) { // 방장이었으면 넘겨주기
                owner = 2;
            }
        }
        else {
            user2State = false;
            if (owner == 2) {
                owner = 1;
            }
        }
        numUser--;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getUser1Id() {
        return user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public int getOwner() {
        return owner;
    }

    public int getNumUser() {
        return numUser;
    }

    public boolean user1State() { return user1State; }

    public boolean user2State() {
        return user2State;
    }
}
