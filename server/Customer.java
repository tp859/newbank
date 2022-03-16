package newbank.server;

import java.util.ArrayList;

public class Customer {
	
	private ArrayList<Account> accounts;
	
	public Customer() {
		accounts = new ArrayList<>();
	}
	
	public String accountsToString() {
		String s = "";
		for(Account a : accounts) {
			s += a.toString() + "\n";
		}
		return s;
	}

	public void addAccount(Account account) {
		accounts.add(account);		
	}

	public Account findAccount(String name) throws NullPointerException {
		for (Account account : accounts) {
			if (account.getAccountName().equals(name)) {
				return account;
			}
		}
		throw new NullPointerException("No account found with this name");
	}
}
