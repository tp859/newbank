package newbank.Database;

import newbank.server.CustomerID;
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

    public Boolean checkUniqueUsername(String username) {
        try {
            this.statement = connection.createStatement();

            ResultSet results = statement.executeQuery(String.format("SELECT * FROM Logins WHERE CustomerID = '%s'", username));

            while (results.next()) {
                String user = results.getString("CustomerID");
                if (user.equals(username)) {
                    return false;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return true;
    }

    public Boolean checkUserExists(String username) {
        try {
            this.statement = connection.createStatement();

            ResultSet results = statement.executeQuery(String.format("SELECT * FROM Logins WHERE CustomerID = '%s'", username));

            if (results.next()) {
                return true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return true;
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

                CustomerID currentID = new CustomerID(customerID);
                Account account = new Account(currentID, accountName, balance, overdraft);

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
                customerID, account.getAccountName(), 0));
    }

    public Integer getCustomerAccountBalance(String customerID, String accountName) {
        try {

            this.statement = connection.createStatement();
            ResultSet results = statement.executeQuery(String.format("SELECT Balance FROM Accounts WHERE CustomerID = '%s' AND AccountName = '%s'", customerID, accountName));

            if (results.next()) {
                return results.getInt("Balance");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public Integer getCustomerAccountOverdraft(String customerID, String accountName) {
        try {

            this.statement = connection.createStatement();
            ResultSet results = statement.executeQuery(String.format("SELECT Overdraft FROM Accounts WHERE CustomerID = '%s' AND AccountName = '%s'", customerID, accountName));

            if (results.next()) {
                return results.getInt("Overdraft");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
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
