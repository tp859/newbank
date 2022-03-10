package newbank.server;

import java.util.HashMap;

import static java.lang.System.exit;

public class NewBank {
	
	private static final NewBank bank = new NewBank();
	private HashMap<String,Customer> customers;
	
	private NewBank() {
		customers = new HashMap<>();
		addTestData();
	}
	
	private void addTestData() {
		Customer bhagy = new Customer();
		bhagy.addAccount(new Account("Main", 1000.0));
		customers.put("Bhagy", bhagy);
		
		Customer christina = new Customer();
		christina.addAccount(new Account("Savings", 1500.0));
		customers.put("Christina", christina);
		
		Customer john = new Customer();
		john.addAccount(new Account("Checking", 250.0));
		customers.put("John", john);
	}
	
	public static NewBank getBank() {

		return bank;
	}
	
	public synchronized CustomerID checkLogInDetails(String userName, String password) {
		if(customers.containsKey(userName)) {
			return new CustomerID(userName);
		}
		return null;
	}

	// commands from the NewBank customer are processed in this method
	public synchronized String processRequest(CustomerID customer, String request) {
		/*Splits input string and checks that the request is in a valid format*/
		String [] splitRequest = request.split(" ");
		boolean valid = checkRequest(splitRequest);

		/*If in valid format, checks the first word of the string to see which bit of code needs to be run*/
		if(customers.containsKey(customer.getKey()) & valid) {
			String result = "";
			switch(splitRequest[0]) {
				case "SHOWMYACCOUNTS" :
					result = showMyAccounts(customer);
					break;

				case "NEWACCOUNT" :
					result = createNewAccount(customer, splitRequest);
					break;

				case "MOVE" :
					result = transferAccounts (customer, splitRequest);
					break;

				case "PAY" :
					result = payOther (customer, splitRequest);
					break;

				case "END":
					result = "Code for exit goes here";
					break;
				default :
					result = "Incorrect format code here";

			}
			return result;
		}
		return "FAIL";
	}

	private String payOther(CustomerID customer, String[] splitRequest) {
		return ("Code for payments here");
	}

	private String transferAccounts(CustomerID customer, String[] splitRequest) {
		return ("Code for transfers here");
	}

	private String createNewAccount(CustomerID customer, String[] splitRequest) {
		return ("Code for new Account here");
	}
	private String showMyAccounts(CustomerID customer) {
		return (customers.get(customer.getKey())).accountsToString();
	}

	/*Checks if request is in valid format*/
	private boolean checkRequest(String[] splitRequest){
		switch(splitRequest[0]) {
			case "SHOWMYACCOUNTS" : case "END" :
				if (splitRequest.length == 1){
					return true;
				}
				break;

			case "NEWACCOUNT" :
				if (splitRequest.length == 2){
					if (!checkInteger(splitRequest[1])) {
						return true;
					}
					return false;
				}
				break;

			case "MOVE" :
				if (splitRequest.length == 4){
					if(checkInteger(splitRequest[1]) & !checkInteger(splitRequest[2]) & !checkInteger(splitRequest[3])) {
						return true;
					}
					return false;
				}
				break;

			case "PAY" :
				if (splitRequest.length == 3){
					if(!checkInteger(splitRequest[1]) & checkInteger(splitRequest[2])) {
						return true;
					}
				}
				break;
			/*Leaving this here for now in case anyone wants to change the conditions for SHOWMYACCOUNTS or END
			*later on and want to split the cases*/
			 /*

			*case "END":
			*	if (splitRequest.length == 1) {
			*		return true;
			*	}
			*	break;
				*/
			default :
				return false;
		}
		return false;
	}

	/*Checks if a given string is an integer, and catches exceptions*/
	private boolean checkInteger(String value){
		try {
			int intValue = Integer.parseInt(value);
			return true;
		}
		catch (NumberFormatException e){
			return false;
		}
	}
}
