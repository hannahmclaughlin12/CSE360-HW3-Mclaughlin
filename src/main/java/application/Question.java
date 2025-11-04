package application;

public class Question { // Question class
	
	// Question data
	private String student_name;
	private String title;
	private String user_question;
	private int q_number;
	private boolean resolved;
	private Answers answers;
	private Questions clarifications;
	
	public Question(String student_name, String title, String user_question, int q_number) { // Create a question object.
		this.student_name = student_name;
		this.title = title;
		this.user_question = user_question;
		this.q_number = q_number;
		this.resolved = false;
		this.answers = new Answers();
		this.clarifications = new Questions();
	}
	
	public String getStudent() { // Retrieve the student's name.
		return student_name;
	}
	public String getTitle() { // Retrieve the question's title.
		return title;
	}
	public String getQuestion() { // Retrieve the question itself.
		return user_question;
	}
	public int getId() { // Retrieve the number associated with the question.
		return q_number;
	}
	public boolean getResolved() { // Retrieve the resolved status.
		return resolved;
	}
	
	public Questions getClarifications() {
		return clarifications;
	}
	
	public Answers getAnswers() { // Retrieve the question's answers.
		return answers;
	}
	
	public void setAnswers(Answers answers) {
		this.answers = answers;
	}
	
	public void setClarification(Question clarification) {
		this.clarifications.addQuestion(clarification);
	}
	
	public void setResolved(boolean resolved) { // Update the resolved status.
		this.resolved = resolved;
	}
	
	@Override
	public String toString() {
		return user_question;
	}
}
