package application;

public class EmailEvaluator{

	//This method evaluates emails for valid formatting
	public static String evaluateEmail(String input) {
		boolean valid = true;
		
		//Checks that input is not empty
		if (input == null || input.length() <= 0) {
			return "Please enter an email address";
		}
		
		int atNdx = input.indexOf('@');
		
		//Checks that there is exactly one at symbol
		if (atNdx == -1 || atNdx != input.lastIndexOf('@')) {
			valid = false;
		}
		
		String local = input.substring(0, atNdx);
		String domain = input.substring(atNdx + 1);
		//Checks that local and domain exist
		if (local.isEmpty() || domain.isEmpty()) {
			valid = false;
		}
		
		//Checks that there are no consecutive periods
		if (input.contains("..")) {
			valid = false;
		}
		
		//Checks that a '.' exists in domain and that it is not at the beginning or end of domain
		int dotNdx = domain.indexOf('.');
		if (dotNdx == -1 || dotNdx == 0 || dotNdx == domain.length() - 1) {
			valid = false;
		}
		
		//Checks that all other characters in input are valid
		for (int i = 0; i < input.length(); i++) {
			char character = input.charAt(i);
			
			if (!(Character.isLetterOrDigit(character) || character == '@' || character == '.' || character == '_' || character == '-')) {
				valid = false;
			}
		}
		
		if (valid) {
			return "";
		}
		
		return "Error: please enter a valid email address";
	}
}
