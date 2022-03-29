package newbank.server;

import java.util.ArrayList;

public class Customer {

	private final ArrayList<Account> accounts;

	public Customer() {
		accounts = new ArrayList<>();
	}

	public String accountsToString() {
		StringBuilder s = new StringBuilder();
		for(Account a : accounts) {
			s.append(a.toString()).append("\n");
		}

		return s.length() != 0 ? s.toString() : "No accounts exist for this user.";
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
		throw new NullPointerException("No account found with name \"" + name + "\"");
	}

	public Account getFirstAccount() throws NullPointerException {

		Account account = accounts.get(0);
		if (account!=null)
			return account;

		throw new NullPointerException("No account found with name \"");

	}
}