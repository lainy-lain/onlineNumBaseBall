package pnu.termproject.onlinenumbaseball;

public class Room {
    private int roomId;
    private String roomName;
    private String user1Id;
    private String user2Id;
    private String user1Name;
    private String user2Name;
    private String user1Photo;
    private String user2Photo;
    private int owner = 1;
    private int numUser = 1;
    private boolean user1State = true;
    private boolean user2State = false;
    private boolean ownerChanged = false;

    public Room(String name, String userId, String userName, String userPhoto) {
        roomName = name;
        user1Id = userId;
        user1Name = userName;
        user1Photo = userPhoto;
    }

    public Room() {}

    // 유저 입장
    public void addUser(String userId, String userName, String userPhoto) {
        if (user1State) {
            user2State = true;
            user2Id = userId;
            user2Name = userName;
            user2Photo = userPhoto;
        }
        else {
            user1State = true;
            user1Id = userId;
            user1Name = userName;
            user1Photo = userPhoto;
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
                ownerChanged = true;
            }
        }
        else {
            user2State = false;
            if (owner == 2) {
                owner = 1;
                ownerChanged = true;
            }
        }
        numUser--;
        ownerChanged = false;
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

    public int getNumUser() {
        return numUser;
    }

    public boolean user1State() {
        return user1State;
    }

    public boolean user2State() {
        return user2State;
    }

    public String getUser1Name() {
        return user1Name;
    }

    public String getUser2Name() {
        return user2Name;
    }

    public String getUser1Photo() {
        return user1Photo;
    }

    public String getUser2Photo() {
        return user2Photo;
    }

    public void setRoomId(int id) {
        roomId = id;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setOwnerChanged(boolean changed) {
        ownerChanged = changed;
    }
    public boolean isOwnerChanged() {
        return ownerChanged;
    }
}
