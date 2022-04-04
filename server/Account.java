package newbank.server;

public class Account {
	
	private final String accountName;
	private int balance;
	private int overdraft;

	public Account(String accountName, double openingBalance) {
		this.accountName = accountName;
		this.balance = ourCurrency.convertToPennies(Double.toString(openingBalance));
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

	public int getOverdraft(){
		return this.overdraft;
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
