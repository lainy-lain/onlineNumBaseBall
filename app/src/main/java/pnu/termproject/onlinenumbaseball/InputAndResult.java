package pnu.termproject.onlinenumbaseball;

public class InputAndResult {
    private String input;
    private String result;

    public InputAndResult() {}

    public InputAndResult(String input, String result) {
        this.input = input;
        this.result = result;
    }

    public InputAndResult(final InputAndResult other){
        this.input = other.getInput();
        this.result = other.getResult();
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
