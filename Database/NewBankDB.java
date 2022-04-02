package newbank.Database;

import newbank.server.Account;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class NewBankDB {

    final static String DELIMITER = ",";
    Connection connection = null;
    Statement statement = null;

    public NewBankDB() {
        //Create connection to DB

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:Database/NewBankDB.db");

            System.out.println("Connected to NewBankDB!");

        } catch (Exception e) {
            System.out.println("Error connecting to database" + e.getMessage());
        }
    }

    public Boolean checkLogin(String username, String password) {
        try {

            this.statement = connection.createStatement();

            ResultSet results = statement.executeQuery(String.format("SELECT * FROM Logins WHERE CustomerID = '%s' AND Password = '%s'", username, password));

            if (results.next()){
                return true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public ArrayList<Account> getAccountsForCustomer(String customerID) {
        try {

            this.statement = connection.createStatement();
            ResultSet results = statement.executeQuery(String.format("SELECT * FROM Accounts WHERE CustomerID = '%s'", customerID));

            ArrayList<Account> customerAccounts = new ArrayList<>();

            while (results.next()) {
                String accountName = results.getString("AccountName");
                int balance = results.getInt("Balance");
                double overdraft = results.getDouble("Overdraft");

                Account account = new Account(accountName, balance, overdraft);

                customerAccounts.add(account);
            }

            return customerAccounts;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public Boolean addCustomerAccount(String customerID, Account account) {
        try {
            this.statement = connection.createStatement();
            statement.execute(String.format("INSERT INTO Accounts (CustomerID, AccountName, Balance) VALUES ('%s', '%s', '%d')",
                    customerID, account.getAccountName(), account.getBalance()));

            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public Boolean addLogin(String customerID, String password) {
        try {
            this.statement = connection.createStatement();
            statement.execute(String.format("INSERT INTO Logins (CustomerID, Password) VALUES ('%s', '%s')",
                    customerID, password));

            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
