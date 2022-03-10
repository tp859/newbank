package newbank.server;

import java.io.BufferedReader;
import java.util.HashMap;

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
		if(customers.containsKey(customer.getKey())) {
			switch(request) {
			case "SHOWMYACCOUNTS" : return showMyAccounts(customer);
			default : return "FAIL";
			}
		}
		return "FAIL";
	}
	
	private String showMyAccounts(CustomerID customer) {
		return (customers.get(customer.getKey())).accountsToString();
	}

}
