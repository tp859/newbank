package newbank.server;

public class Account {
	
	private String accountName;
	private double balance;

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

}
