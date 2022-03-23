package newbank.server;

import java.io.BufferedReader;
import java.util.HashMap;

import static java.lang.System.exit;
import static java.lang.System.out;

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

        return authenticator.checkLoginDetails(userName, password);
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
						result = createNewAccount(customer, splitRequest[1]);
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
				case "DEPOSIT" :
					// Format "DEPOSIT <amount> <accountName>
					if (splitRequest.length == 3){
						if(checkDouble(splitRequest[1]) & !checkDouble(splitRequest[2])) {
							result = deposit(customer, splitRequest);
						}
					}
					break;

                case "WITHDRAW" :
					if (splitRequest.length == 3){
						double amount = convertToDouble(splitRequest[1]);

						if(amount != -1) {
							result = withdraw(customer, amount, splitRequest[2]);
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

	private String deposit(CustomerID customer, String[] splitRequest) {

		double deposit = Double.parseDouble(splitRequest[1]); // No need to check this as format checked when input read
		// Check not trying to withdraw using deposit command
		if (deposit < 0) {
			return ("FAIL: Deposit amount must be a positive number");
		}

		try { // Try/catch needed in case findAccount throws exception
			customers.get(customer.getKey()).findAccount(splitRequest[2]).changeBalanceBy(deposit);
			String newBalance = customers.get(customer.getKey()).findAccount(splitRequest[2]).getBalance().toString();
			return "SUCCESS: The new balance for " + splitRequest[2] + " is £" + newBalance;
		} catch (NullPointerException e) {
			// No account found with that name
			return ("FAIL: No account found with that name.");
		}
	}

    private String withdraw(CustomerID customer, double amount, String account) {
		try {
			Customer customerDetails = customers.get(customer.getKey());
			Account customerAccount = customerDetails.findAccount(account);

			if (customerAccount.getBalance() >= amount) {
				customerAccount.changeBalanceBy(-amount);
				return String.format("SUCCESS: The new balance for Account \"%s\" is £%.2f", account, customerAccount.getBalance());
			}
		}
		catch (NullPointerException e) {
			return ("FAIL: No account found with the name \"%s\"");
		}

		return "FAIL";
	}

	private String payOther(CustomerID customer, String[] splitRequest) {
		return ("Code for payments here");
	}

	private String transferAccounts(CustomerID customer, String[] splitRequest) {
		return ("Code for transfers here");
	}

	private String showMyAccounts(CustomerID customer) {
		return (customers.get(customer.getKey())).accountsToString();
	}

	private String createNewAccount(CustomerID customer, String accountName) {

		customers.get(customer.getKey()).addAccount(new Account(accountName, 0.0));

		return "SUCCESS";
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

    /*Checks if a given string is a double, and catches exceptions*/
    private boolean checkDouble(String value) {
        try {
            double doubleVal = Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

	/*Checks if a given string is a double, and catches exceptions*/
	private double convertToDouble(String value){
		try {
			return Double.parseDouble(value);
		}
		catch (NumberFormatException e){
			out.println("Value entered was not a number.");
			return -1;
		}

	}
}
