package application;
import java.util.ArrayList; // This class heavily uses an array list.

public class Questions {
	private ArrayList<Question> questions = new ArrayList<>();
	private int i = 1; // This will help us number each question.
	
	public void newQuestion(String student_name, String title, String user_question) {
		if (student_name.equals("")) { // Confirm that a name exists.
			System.out.println("Invalid name!");
		}
		else if (title.equals("")) {
			System.out.println("Invalid title!"); // Confirm that a title exists.
		}
		else if (user_question.length() < 10) { // Confirm that the question is of reasonable length.
			System.out.println("Invalid question!");
		}
		else { // Add a new question object to the array list.
			Question question = new Question(student_name, title, user_question, i);
			questions.add(question);
			System.out.println("Successfully added question #" + i);
			i++;
		}
	}
	
	public Question getQuestion(int q_number) { // Function for retrieving a question, based on an integer value.
		for (int n = 0; n < questions.size(); n++) {
			Question question = questions.get(n);
			if (question.getId() == q_number) {
				return question;
			}
		}
		return null;
	}
	
	public ArrayList<Question> getAllQuestions() {
		return questions;
	}
	
	public boolean viewAllQuestions() { // Function for displaying all questions.
		if (questions.isEmpty()) {
			System.out.println("There are currently no questions.");
			return false;
		}
		else {
			for (int n = 0; n < questions.size(); n++) {
				Question question = questions.get(n);
				System.out.println(question.getId() + ".");
				System.out.println("Submitted by: " + question.getStudent());
				System.out.println("Title: " + question.getTitle());
				System.out.println(question.getQuestion());
				if (question.getResolved() == true) {
					System.out.println("Resolved");
				}
			}
			return true;
		}
	}
	
	public boolean viewAllUnresolvedQuestions() { // Function for displaying all unresolved questions.
		int count = 0;
		for (int n = 0; n < questions.size(); n++) {
			Question question = questions.get(n);
			if (question.getResolved() == false) {
				System.out.println(question.getId() + ".");
				System.out.println("Submitted by: " + question.getStudent());
				System.out.println("Title:" + question.getTitle());
				System.out.println(question.getQuestion());
				count++;
			}
		}
		if (count < 1) {
			System.out.println("There are no unresolved questions.");
			return false;
		}
		return true;
	}
	
	public boolean viewYourUnresolvedQuestions(String name) { // Function for displayed your unresolved questions.
		int count = 0;
		for (int n = 0; n < questions.size(); n++) {
			Question question = questions.get(n);
			if (question.getResolved() == false && question.getStudent().equals(name)) {
				System.out.println(question.getId() + ".");
				System.out.println("Submitted by: " + question.getStudent());
				System.out.println("Title:" + question.getTitle());
				System.out.println(question.getQuestion());
				// print number of unread answers
				count++;
			}
		}
		if (count < 1) {
			System.out.println("There are no unresolved questions.");
			return false;
		}
		return true;
	}
	
	public boolean searchQuestions(String keyword_search) { // Function for searching for questions.
		int count = 0;
		keyword_search = keyword_search.toLowerCase();
		for (int n = 0; n < questions.size(); n++) {
			Question question = questions.get(n);
			if (question.getQuestion().contains(keyword_search)) {
				System.out.println(question.getId() + ".");
				System.out.println("Submitted by: " + question.getStudent());
				System.out.println("Title:" + question.getTitle());
				System.out.println(question.getQuestion());
				// print number of unread answers
				count++;
			}
		}
		if (count < 1) {
			System.out.println("There are no questions with that term.");
			return false;
		}
		return true;
	}
	
	public boolean ListUnanswered() { // Function to list unanswered questions.
		int count = 0;
		for (int n = 0; n < questions.size(); n++) {
			Question question = questions.get(n);
			if (question.getAnswers().getCount() == 0) {
				System.out.println(question.getId() + ".");
				System.out.println("Submitted by: " + question.getStudent());
				System.out.println("Title:" + question.getTitle());
				System.out.println(question.getQuestion());
				// print number of unread answers
				count++;
			}
		}
		if (count < 1) {
			System.out.println("There are no unanswered questions.");
			return false;
		}
		return true;
	}
	
	public boolean ListAnswered() { // Function to list answered questions.
		int count = 0;
		for (int n = 0; n < questions.size(); n++) {
			Question question = questions.get(n);
			if (question.getAnswers().getCount() > 0) {
				//System.out.println(question.getNumber() + ".");
				System.out.println("Submitted by: " + question.getStudent());
				System.out.println("Title:" + question.getTitle());
				System.out.println(question.getQuestion());
				// print number of unread answers
				count++;
			}
		}
		if (count < 1) {
			System.out.println("There are no answered questions.");
			return false;
		}
		return true;
	}
	
	public void addQuestion(Question question) {
		questions.add(question);
	}
	
}
