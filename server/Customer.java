package newbank.server;

import java.util.ArrayList;
import java.util.List;

public class Customer {

	private ArrayList<Account> accounts;
	private CustomerID customerID;

//	public Customer() {
//		accounts = new ArrayList<>();
//	}

	public Customer(CustomerID customerID) {
		this.customerID = customerID;
		accounts = new ArrayList<Account>();
	}

	public String accountsToString() {
		StringBuilder s = new StringBuilder();
		for(Account a : accounts) {
			s.append(a.toString()).append("\n");
		}

		return s.length() != 0 ? s.toString() : "No accounts exist for this user.";
	}

	public void loadAccounts() {
		accounts = NewBankServer.newBankDB.getAccountsForCustomer(customerID.getKey());
	}

	public CustomerID getCustomerID() {
		return this.customerID;
	}

	public void addAccount(Account account) {
		NewBankServer.newBankDB.addCustomerAccount(customerID.getKey(), account);
		loadAccounts();
	}

	public Account findAccount(String name) throws NullPointerException {
		for (Account account : accounts) {
			if (account.getAccountName().equals(name)) {
				return account;
			}
		}
		throw new NullPointerException("No account found with name \"" + name + "\"");
	}

	public boolean checkAcc(String name) throws NullPointerException {
		for (Account account : accounts) {
			if (account.getAccountName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public Account getFirstAccount() throws NullPointerException {

		Account account = accounts.get(0);
		if (account!=null)
			return account;

		throw new NullPointerException("No account found with name \"");
	}
}