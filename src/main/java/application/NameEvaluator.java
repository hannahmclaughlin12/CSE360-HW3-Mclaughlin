package application;

public class NameEvaluator{
	
	//This method detects if a name is a valid input
	public static String evaluateName(String input) {
		if (input == null || input.length() <= 0) {
			return "Please enter your first and last name.";
		}
		
		char currentChar;
		boolean space = false;
		
		//Loop checks for space and ensures only alphabetic characters are entered
		for (int i = 0; i < input.length(); i++) {
			currentChar = input.charAt(i);
			
			if (Character.isLetter(currentChar)) {
				continue;
			}
			else if (currentChar == ' ') {
				space = true;
			}
			else {
				return "Error: first and last name must only contain letters";
			}
		}
		if (!space) {
			return "Error: please enter your first and last name";
		}
		
		return "";
	}
}
