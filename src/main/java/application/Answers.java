package application;

import java.util.ArrayList;
import java.util.List; // === START EDITS === added for getAll()
// === END EDITS ===

public class Answers { // Answers class
    
    // Answers data
    private ArrayList<Answer> answers = new ArrayList<>();
    private int i = 0; // This will help us number each answer.
    private int unread = 0; // This will help us track unread answers.
    
    public void newAnswer(String name, String user_answer, int q_number) { // Create a new answer object.
        if (name.equals("")) {
            System.out.println("Invalid name!");
        }
        else if (user_answer.length() < 1) {
            System.out.println("Invalid answer!");
        }
        else { // Add a new question object to the array list.
            i++;
            Answer answer = new Answer(name, user_answer, q_number, i);
            answers.add(answer);
            System.out.println("Successfully added answer #" + i);
            unread++;
        }
    }
    
    public void setAnswer(Answer answer) {
        this.answers.add(answer);
        // === START EDITS === keep count in sync when loading from DB
        i = Math.max(i, answer.getANumber());
        // === END EDITS ===
    }

    public int getCount() { // Retrieve the count (to distinguish between answered and unanswered questions).
        return i;
    }

    public Answer getAnswer(int a_number) { // Retrieve an answer by number/id.
        for (int n = 0; n < answers.size(); n++) {
            Answer answer = answers.get(n);
            if (answer.getANumber() == a_number) {
                return answer;
            }
        }
        return null;
    }

    // === START EDITS === expose list for ListView
    public List<Answer> getAll() {
        return answers;
    }
    // === END EDITS ===
    
    public boolean viewAnswers() {
        if (answers.isEmpty()) {
            System.out.println("There are currently no answers.");
            return false;
        }
        else {
            for (int n = 0; n < answers.size(); n++) {
                Answer answer = answers.get(n);
                System.out.println(answer.getANumber() + ".");
                System.out.println("Submitted by: " + answer.getName());
                System.out.println(answer.getAnswer());
            }
            return true;
        }
    }
    
    public void markAsRead() { // (Effectively) Mark all answers as read.
        this.unread = 0;
    }
    
    public int getUnread() { // Retrieve the number of unread answers.
        return unread;
    }
}
