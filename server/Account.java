package newbank.server;

public class Account {
	
	private final String accountName;
	private int balance;

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


	public boolean checkBalance(int amount){
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
