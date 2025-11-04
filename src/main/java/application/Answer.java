package application;

public class Answer { // Answer class
    
    // Answer data
    private String name;
    private String user_answer;
    private int q_number;
    private int a_number;     // Note: we use this as the DB 'id'
    private boolean resolves;
    
    public Answer(String name, String user_answer, int q_number, int a_number) {
        this.name = name;
        this.user_answer = user_answer;
        this.q_number = q_number;
        this.a_number = a_number;
        this.resolves = false;
    }
    
    public String getName() { return name; }
    public String getAnswer() { return user_answer; }
    public int getQNumber() { return q_number; }
    public int getANumber() { return a_number; }
    public boolean getResolves() { return resolves; }
    
    public void setResolves() { this.resolves = true; }

    // === START EDITS === better display in ListView
    @Override
    public String toString() {
        String mark = resolves ? " (resolves)" : "";
        return "#" + a_number + " • " + name + ": " + user_answer + mark;
    }
    // === END EDITS ===

    // added {
    
    public int getId() { // Retrieve the number associated with the question.
		return a_number;
	}
    
    public void setResolves(boolean resolves) { // Update the resolved status.
		this.resolves = resolves;
	}
    // }
}
