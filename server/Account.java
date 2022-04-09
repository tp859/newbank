package newbank.server;

public class Account extends Customer {
	
	private final String accountName;
	private int balance;
	private int overdraft;

	public Account(CustomerID customerID, String accountName, double openingBalanceInPounds) {
		super(customerID);
		this.accountName = accountName;
		this.balance = ourCurrency.convertToPennies(Double.toString(openingBalanceInPounds));
	}

	public Account(CustomerID customerID, String accountName, int openingBalance, double overdraft) {
		super(customerID);
		this.accountName = accountName;
		this.balance = openingBalance;
		this.overdraft = (int)overdraft;
	}
	
	public String toString() {
		return (accountName + ": " + ourCurrency.printMoney(balance));
	}

	// Getters
	public String getAccountName() {
		return this.accountName;
	}

	public int getBalance() {
		return NewBankServer.newBankDB.getCustomerAccountBalance(super.getCustomerID().getKey(), getAccountName());
	}

	public int getOverdraft(){
		return NewBankServer.newBankDB.getCustomerAccountOverdraft(super.getCustomerID().getKey(), getAccountName());
	}

	public String printBalance(){
		return ourCurrency.printMoney(balance);
	}

	// Setters
	public void changeBalanceBy(int sum) {
		int newBalance = balance + sum;
		NewBankServer.newBankDB.updateCustomerAccountBalance(super.getCustomerID().getKey(), getAccountName(), newBalance);
		this.balance += sum;
	}

	public String printOverdraft() {
		return ourCurrency.printMoney(this.overdraft);
	}

	public void setOverdraft(int amount){
		this.overdraft = amount;
	}

	public boolean approveOverdraft(int amount){
		//Checking if the system should allow transaction with 20£ fixed fine
		return balance + overdraft >= amount + 2000;
	}

	public boolean checkBalance(int amount){
		return !(amount > this.balance);
	}

}
