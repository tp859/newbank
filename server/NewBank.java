package newbank.server;

import java.io.BufferedReader;
import java.util.HashMap;
import java.lang.Math;

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
					if (!ourCurrency.moneyValid(splitRequest[1])) {
						result = createNewAccount(customer, splitRequest[1]);
					}
					break;

				case "MOVE" :
					if (splitRequest.length == 4){
						if(ourCurrency.moneyValid(splitRequest[1]) & !ourCurrency.moneyValid(splitRequest[2]) & !ourCurrency.moneyValid(splitRequest[3])) {
							result = transferAccounts (customer, splitRequest);
						}
					}
					break;

				case "PAY" :
					if (splitRequest.length == 3){
						if(!ourCurrency.moneyValid(splitRequest[1]) & ourCurrency.moneyValid(splitRequest[2])) {
							result = payOther (customer, splitRequest);
						}
					}
					break;
				case "DEPOSIT" :
					// Format "DEPOSIT <amount> <accountName>
					if (splitRequest.length == 3){
						if(ourCurrency.moneyValid(splitRequest[1]) & !ourCurrency.moneyValid(splitRequest[2])) {
							result = deposit(customer, splitRequest);
						}
					}
					break;

                case "WITHDRAW" :
					if (splitRequest.length == 3){
						if (ourCurrency.moneyValid(splitRequest[1]) & !ourCurrency.moneyValid(splitRequest[2])) {
							int amount = ourCurrency.convertToPennies(splitRequest[1]);
							if (amount > 0) {
								result = withdraw(customer, amount, splitRequest[2]);
							}
						}
					}
					break;


				case "END":
					if (splitRequest.length == 1) {
						result = "Code for exit goes here";
					}
					break;

				case "SETOVERDRAFT":
					// format SETOVERDRAFT <amount> <account>
					if (splitRequest.length == 3){
						if(checkDouble(splitRequest[1]) & !checkDouble(splitRequest[2])) {
							result = setOverdraft(customer, splitRequest);
						}
					}
					break;

				case "CHECKOVERDRAFT":
					//format CHECKOVERDRAFT <account>
					if (splitRequest.length == 2){
						if (!checkDouble((splitRequest[1]))){
							result = checkAccountOverdraft(customer, splitRequest) ;
						}
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

	//Setting up overdraft of up to 1500 £ for individual accounts
	private String setOverdraft(CustomerID customer, String[] splitRequest){
		 double overdraft = Double.parseDouble(splitRequest[1]);
		 if (overdraft < 0 || overdraft > 1500){
			 return ("FAIL: overdraft limit must be between £0 and £1500");
		 }
		 if (customers.get(customer.getKey()).findAccount(splitRequest[2]).getBalance() < 0){
			 return "FAIL: Overdraft increase unavailable for the accounts with negative balance";
		 }
		 try {
			 customers.get(customer.getKey()).findAccount(splitRequest[2]).setOverdraft(overdraft);
			 String overdraftBalance = customers.get(customer.getKey()).findAccount(splitRequest[2]).getOverdraft().toString();
			 return "SUCCESS: The new overdraft limit for " + splitRequest[2] + " is £" + overdraftBalance;
		 } catch (NullPointerException e){
			 return ("FAIL: No account found with that name.");
		 }
	}

	private String checkAccountOverdraft(CustomerID customer, String[] splitRequest){
		try{
			Double overdraftAmount = customers.get(customer.getKey()).findAccount(splitRequest[1]).getOverdraft();
			if (overdraftAmount > 0) {
				return "The available overdraft for " + splitRequest[1] + " is £" + Double.toString(overdraftAmount);
			}
			else{
				return "No overdraft set up for the " + splitRequest[1] + " account";
			}
		} catch (NullPointerException e){
			return ("FAIL: No account found with that name.");
		}
	}

	private String deposit(CustomerID customer, String[] splitRequest) {
		int deposit = ourCurrency.convertToPennies(splitRequest[1]); // No need to check this as format checked when input read
		// Check not trying to withdraw using deposit command
		if (deposit < 0) {
			return ("FAIL: Deposit amount must be a positive number");
		}
		try { // Try/catch needed in case findAccount throws exception
			customers.get(customer.getKey()).findAccount(splitRequest[2]).changeBalanceBy(deposit);
			String newBalance = customers.get(customer.getKey()).findAccount(splitRequest[2]).printBalance();
			return "SUCCESS: The new balance for " + splitRequest[2] + " is " + newBalance;
		} catch (NullPointerException e) {
			// No account found with that name
			return ("FAIL: No account found with that name.");
		}
	}

    private String withdraw(CustomerID customer, int amount, String account) {
		try {
			Customer customerDetails = customers.get(customer.getKey());
			Account customerAccount = customerDetails.findAccount(account);

			if (customerAccount.getBalance() >= amount) {
				System.out.println(customerAccount.getBalance());
				customerAccount.changeBalanceBy(-amount);
				System.out.println(customerAccount.getBalance());
				return String.format("SUCCESS: The new balance for Account \"%s\" is %s", account, customerAccount.printBalance());
			}
			//Enter if overdraft account is set up by the user
			else if (customerAccount.getBalance() < amount && customerAccount.getOverdraft() > 0 ){
				if (customerAccount.approveOverdraft(amount)){
					customerAccount.changeBalanceBy(-(amount + 20)); // 20 is fixed fine for overdraft transaction
					return String.format("SUCCESS: The new balance for Account \"%s\" is £%.2f", account, customerAccount.getBalance());
				}
				return String.format("FAIL: Maximum overdraft for the Account \"%s\" is £%.2f", account, customerAccount.getOverdraft());
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
		try {
			int amount = ourCurrency.convertToPennies(splitRequest[1]);
			System.out.println(amount);
			if (amount <= 0) {
				return "FAIL: Amount must be greater than £0.00";
			}
			Account fromAccount = customers.get(customer.getKey()).findAccount(splitRequest[2]);
			Account toAccount = customers.get(customer.getKey()).findAccount(splitRequest[3]);

			//check if the accounts are the same
			if (fromAccount.equals(toAccount)) {
				return "FAIL: accounts are the same.";
			}

			if (fromAccount.checkBalance(amount)) {
				fromAccount.changeBalanceBy(-amount);
				toAccount.changeBalanceBy(amount);
				return "SUCCESS";
			} else {
				return "FAIL: Insufficient funds";
			}
		} catch (NullPointerException e) {
			return (e.getMessage());
		}

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
