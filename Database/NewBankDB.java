package newbank.Database;

import newbank.server.Account;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class NewBankDB {

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

    public Boolean addLogin(String customerID, String password) {

        return execute(String.format("INSERT INTO Logins (CustomerID, Password) VALUES ('%s', '%s')",
                customerID, password));
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

        return execute(String.format("INSERT INTO Accounts (CustomerID, AccountName, Balance) VALUES ('%s', '%s', '%d')",
                customerID, account.getAccountName(), account.getBalance()));
    }

    public Boolean updateCustomerAccountBalance(String customerID, String accountName, int newBalance){

        return updateCustomerAccount(customerID, accountName, newBalance, "Balance");
    }

    public Boolean updateCustomerAccountOverdraft(String customerID, String accountName, int newOverdraft){

        return updateCustomerAccount(customerID, accountName, newOverdraft, "Overdraft");
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private Boolean updateCustomerAccount(String customerID, String accountName, int value, String valueName){
        return execute(String.format("UPDATE Accounts SET %s = '%d' WHERE CustomerID = '%s' and AccountName = '%s'",
                valueName, value, customerID, accountName));
    }

    private Boolean execute(String query){
        try {
            this.statement = connection.createStatement();

            return statement.execute(query);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return false;
    }
}