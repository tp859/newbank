package newbank.server;

public class Account {
	
	private final String accountName;
	private double balance;
	private double overdraft;

	public Account(String accountName, double openingBalance) {
		this.accountName = accountName;
		this.balance = openingBalance;

	}
	
	public String toString() {
		return (accountName + ": " + balance);
	}

	// Getters
	public String getAccountName() {
		return this.accountName;
	}

	public Double getBalance() {
		return this.balance;
	}

	// Setters
	public void changeBalanceBy(double sum) {
		this.balance += sum;
	}


	public boolean checkBalance(double amount){
		return !(amount > this.balance);
	}

	public Double getOverdraft(){
		return this.overdraft;
	}

	public void setOverdraft(double amount){
		this.overdraft = amount;
	}

	public boolean approveOverdraft(double amount){
		//Checking if the system should allow transaction with 20£ fixed fine
		if(balance + overdraft >= amount + 20){
			return true;
		}
		return false;
	}

}
