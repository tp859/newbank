package newbank.server;

public class Account {
	
	private final String accountName;
	private int balance;
	private Double overdraft;

	public Account(String accountName, double openingBalanceInPounds) {
		this.accountName = accountName;
		this.balance = ourCurrency.convertToPennies(Double.toString(openingBalanceInPounds));
	}

	public Account(String accountName, int openingBalance, double overdraft) {
		this.accountName = accountName;
		this.balance = openingBalance;
		this.overdraft = overdraft;
	}
	
	public String toString() {
		return (accountName + ": " + ourCurrency.printMoney(balance));
	}

	// Getters
	public String getAccountName() {
		return this.accountName;
	}

	public int getBalance() {
		return this.balance;
	}

	public String printBalance(){
		return ourCurrency.printMoney(balance);
	}

	// Setters
	public void changeBalanceBy(int sum) {
		this.balance += sum;
	}

	public Double getOverdraft(){
		return this.overdraft;
	}

	public void setOverdraft(double amount){
		this.overdraft = amount;
	}

	public boolean approveOverdraft(double amount){
		//Checking if the system should allow transaction with 20£ fixed fine
		return balance + overdraft >= amount + 20;
	}

	public boolean checkBalance(int amount){
		return !(amount > this.balance);
	}

}
