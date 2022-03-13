package newbank.server;

import java.io.BufferedReader;
import java.util.HashMap;

import static java.lang.System.exit;

public class NewBank {
	
	private static final NewBank bank = new NewBank();
	private final HashMap<String,Customer> customers;
	
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
		Authenticator authenticator = new Authenticator();

		CustomerID customerID = authenticator.checkLoginDetails(userName, password);

		if (customerID != null)
			return customerID;

		return null;
	}

	// commands from the NewBank customer are processed in this method
	public synchronized String processRequest(CustomerID customer, String request) {
		/*Splits input string and checks that the request is in a valid format*/
		String [] splitRequest = request.split(" ");

		/*If in valid format, checks the first word of the string to see which bit of code needs to be run*/
		if(customers.containsKey(customer.getKey())) {
			String result = "Command in incorrect format. Try again.";
			switch(splitRequest[0]) {
				case "SHOWMYACCOUNTS" :
					if (splitRequest.length == 1){
						result = showMyAccounts(customer);
					}
					break;

				case "NEWACCOUNT" :
					if (!checkInteger(splitRequest[1])) {
						result = createNewAccount(customer, splitRequest);
					}
					break;

				case "MOVE" :
					if (splitRequest.length == 4){
						if(checkInteger(splitRequest[1]) & !checkInteger(splitRequest[2]) & !checkInteger(splitRequest[3])) {
							result = transferAccounts (customer, splitRequest);
						}
					}
					break;

				case "PAY" :
					if (splitRequest.length == 3){
						if(!checkInteger(splitRequest[1]) & checkInteger(splitRequest[2])) {
							result = payOther (customer, splitRequest);
						}
					}
					break;

				case "END":
					if (splitRequest.length == 1) {
						result = "Code for exit goes here";
					}
					break;

				default :
					result = "Command in incorrect format. Try again.";

			}
			return result;
		}
		//The code shouldn't reach here, as users should be set up correctly, but leaving a return here just in case//
		return "FAIL. TRY AGAIN.";
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
