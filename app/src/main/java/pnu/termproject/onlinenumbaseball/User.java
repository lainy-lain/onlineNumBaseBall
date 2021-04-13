package pnu.termproject.onlinenumbaseball;

public class User {
    private String userId;
    private String userName;
    private String userProfile;
    private double meanTime;
    private double meanTurn;
    private double ability; // sum of meanTime and meanTurn

    public User() {

    }

    public User(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    /*************** GETTER AND SETTER **************/
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public double getMeanTime() {
        return meanTime;
    }

    public void setMeanTime(double meanTime) {
        this.meanTime = meanTime;
    }

    public double getMeanTurn() {
        return meanTurn;
    }

    public void setMeanTurn(double meanTurn) {
        this.meanTurn = meanTurn;
    }

    public double getAbility() {
        return ability;
    }

    public void setAbility(double ability) {
        this.ability = ability;
    }
    /***********************************************/

    @Override
    public String toString(){
        return "User{" +
                "userProfile='" + userProfile + '\'' +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", meanTime='" + meanTime + '\'' +
                ", meanTurn='" + meanTurn + '\'' +
                ", ability='" + ability + '\'' +
                '}';
    }
}
