package newbank.server;

import newbank.server.Constants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewBank {

    private static final NewBank bank = new NewBank();
    private final HashMap<String, Customer> customers;
    Authenticator authenticator = new Authenticator();

    private NewBank() {
        customers = new HashMap<>();
        addTestData();
    }

    private void addTestData() {
        Customer bhagy = new Customer();
        bhagy.addAccount(new Account("Main", 1000.0));
        customers.put("Bhagy", bhagy);

        Customer christina = new Customer();
        christina.addAccount(new Account("Savings", 1500.0));
        customers.put("Christina", christina);

        Customer john = new Customer();
        john.addAccount(new Account("Checking", 250.0));
        customers.put("John", john);


    }

    public static NewBank getBank() {

        return bank;
    }

    public synchronized CustomerID checkLogInDetails(String userName, String password) {

        return authenticator.checkLoginDetails(userName, password);
    }

    public synchronized String generateUsername(String firstname) {

        return authenticator.generateUsername(firstname);
    }


    // commands from the NewBank customer are processed in this method
    public synchronized String processRequest(CustomerID customer, String request) {
        try {

            /*Splits input string and checks that the request is in a valid format*/
            String[] splitRequest = request.split(" ");

            /*If in valid format, checks the first word of the string to see which bit of code needs to be run*/

            // Checks if username exists in db
            if (NewBankServer.newBankDB.checkUserExists(customer.getKey())) {
                String result = "Command in incorrect format. Try again.";
                switch (splitRequest[0]) {
                    case Commands.SHOW_ACCOUNTS:
                        if (splitRequest.length == 1) {
                            result = showMyAccounts(customer);
                        }
                        break;

                    case Commands.CREATE_ACCOUNT:
                        if (!ourCurrency.moneyValid(splitRequest[1])) {
                            result = createNewAccount(customer, splitRequest[1]);
                        }
                        break;

                    case Commands.TRANSFER:
                        if (splitRequest.length == 4) {
                            if (ourCurrency.moneyValid(splitRequest[1]) & !ourCurrency.moneyValid(splitRequest[2]) & !ourCurrency.moneyValid(splitRequest[3])) {
                                result = transferAccounts(customer, splitRequest);
                            }
                            break;
                        }

                    case Commands.PAY_OTHER:
                        // Fomat "PAY <customerID> <amount>
                        if (splitRequest.length == 3) {
                            if (ourCurrency.moneyValid(splitRequest[2])) {
                                result = payOther(customer, splitRequest);
                            }
                        }
                        break;

                    case Commands.DEPOSIT:
                        // Format "DEPOSIT <amount> <accountName>
                        if (splitRequest.length == 3) {
                            if (ourCurrency.moneyValid(splitRequest[1]) & !ourCurrency.moneyValid(splitRequest[2])) {
                                result = deposit(customer, splitRequest);
                            }
                            break;
                        }

                    case Commands.WITHDRAW:
                        if (splitRequest.length == 3) {
                            if (ourCurrency.moneyValid(splitRequest[1]) & !ourCurrency.moneyValid(splitRequest[2])) {
                                int amount = ourCurrency.convertToPennies(splitRequest[1]);
                                if (amount > 0) {
                                    result = withdraw(customer, amount, splitRequest[2]);
                                }
                            }
                            break;
                        }

                    case Commands.EXIT_CLIENT:
                        if (splitRequest.length == 1) {
                            result = "Code for exit goes here";
                        }
                        break;

                    case Commands.SET_OVERDRAFT:
                        // format SETOVERDRAFT <amount> <account>
                        if (splitRequest.length == 3) {
                            if (ourCurrency.moneyValid(splitRequest[1]) & !ourCurrency.moneyValid(splitRequest[2])) {
                                result = setOverdraft(customer, splitRequest);
                            }
                        }
                        break;

                    case Commands.CHECK_OVERDRAFT:
                        //format CHECKOVERDRAFT <account>
                        if (splitRequest.length == 2) {
                            if (!ourCurrency.moneyValid((splitRequest[1]))) {
                                result = checkAccountOverdraft(customer, splitRequest);
                            }
                        }
                        break;

                    default:
                        throw new NullPointerException("No account found with this name");
                }

                return result;
            }
        } catch (Exception e) {
            return (e.getMessage());
        }
        //The code shouldn't reach here, as users should be set up correctly, but leaving a return here just in case//
        return ("No account found with this name");
    }

    //Setting up overdraft of up to 1500 £ for individual accounts
    private String setOverdraft(CustomerID customer, String[] splitRequest){
        int overdraft = ourCurrency.convertToPennies(splitRequest[1]);
        if (overdraft < 0 || overdraft > Overdraft.LIMIT){
            return ("FAIL: overdraft limit must be between £0 and " + ourCurrency.printMoney(Overdraft.LIMIT));
        }
        if (customers.get(customer.getKey()).findAccount(splitRequest[2]).getBalance() < 0){
            return "FAIL: Overdraft increase unavailable for the accounts with negative balance";
        }
        try {
            customers.get(customer.getKey()).findAccount(splitRequest[2]).setOverdraft(overdraft);
            int overdraftBalance = customers.get(customer.getKey()).findAccount(splitRequest[2]).getOverdraft();
            return "SUCCESS: The new overdraft limit for " + splitRequest[2] + " is " + ourCurrency.printMoney(overdraftBalance);
        } catch (NullPointerException e){
            return ("FAIL: No account found with that name.");
        }
    }

    private String checkAccountOverdraft(CustomerID customer, String[] splitRequest){
        try{
            int overdraftAmount = customers.get(customer.getKey()).findAccount(splitRequest[1]).getOverdraft();
            String overdraftPrint = customers.get(customer.getKey()).findAccount(splitRequest[1]).printOverdraft();
            if (overdraftAmount > 0) {
                return "The available overdraft for " + splitRequest[1] + " is " + overdraftPrint;
            }
            else{
                return "No overdraft set up for the " + splitRequest[1] + " account";
            }
        } catch (NullPointerException e){
            return ("FAIL: No account found with that name.");
        }
    }

    private String deposit(CustomerID customer, String[] splitRequest) {
        int deposit = ourCurrency.convertToPennies(splitRequest[1]); // No need to check this as format checked when input read
        // Check not trying to withdraw using deposit command
        if (deposit < 0) {
            return ("FAIL: Deposit amount must be a positive number");
        }

        customers.get(customer.getKey()).findAccount(splitRequest[2]).changeBalanceBy(deposit);
        String newBalance = customers.get(customer.getKey()).findAccount(splitRequest[2]).printBalance();
        return "SUCCESS: The new balance for " + splitRequest[2] + " is " + newBalance;
    }

    private String withdraw(CustomerID customer, int amount, String account) {
        Customer customerDetails = customers.get(customer.getKey());
        Account customerAccount = customerDetails.findAccount(account);

        if (customerAccount.getBalance() >= amount) {
            System.out.println(customerAccount.getBalance());
            customerAccount.changeBalanceBy(-amount);
            System.out.println(customerAccount.getBalance());
            return String.format("SUCCESS: The new balance for Account \"%s\" is %s", account, customerAccount.printBalance());
        }
        //Enter if overdraft account is set up by the user
        else if (customerAccount.getBalance() < amount && customerAccount.getOverdraft() > 0) {
            if (customerAccount.approveOverdraft(amount)) {
                customerAccount.changeBalanceBy(-(amount + Overdraft.TRANSACTION_FEE)); // £20/2000p is fixed fine for overdraft transaction
                return String.format("SUCCESS: The new balance for Account \"%s\" is %s", account, customerAccount.printBalance());
            }
            return String.format("FAIL: Maximum overdraft for the Account \"%s\" is %s", account, customerAccount.printOverdraft());
        }
        return String.format("FAIL: The Account \"%s\" does not have enough balance, and it has no overdraft set up.", account);
    }

    private String payOther(CustomerID fromCustomer, String[] splitRequest) {
            int amount = ourCurrency.convertToPennies(splitRequest[2]);
            CustomerID toCustomer = new CustomerID(splitRequest[1]);

            Customer fromCustomerDetails = new Customer(fromCustomer);
            Account fromCustomerAccount = fromCustomerDetails.getFirstAccount();

            Customer toCustomerDetails = new Customer(toCustomer);
            Account toCustomerAccount = toCustomerDetails.getFirstAccount();

            // Update paying Customer's account
            if (fromCustomerAccount.getBalance() >= amount) {
                fromCustomerAccount.changeBalanceBy(-amount);
                NewBankServer.newBankDB.updateCustomerAccountBalance(fromCustomer.getKey(), fromCustomerAccount.getAccountName(), fromCustomerAccount.getBalance());

                // Update receiving Customers account
                toCustomerAccount.changeBalanceBy(+amount);
                NewBankServer.newBankDB.updateCustomerAccountBalance(toCustomer.getKey(), toCustomerAccount.getAccountName(), toCustomerAccount.getBalance());

                return String.format("SUCCESS: The new balance for Account \"%s\" is %s", fromCustomerAccount.getAccountName(), fromCustomerAccount.printBalance());
            }

            return String.format("FAIL: The account \"%s\" does not have enough balance available.", fromCustomerAccount.getAccountName());
    }

    private String transferAccounts(CustomerID customer, String[] splitRequest) {
        int amount = ourCurrency.convertToPennies(splitRequest[1]);
        System.out.println(amount);
        if (amount <= 0) {
            return "FAIL: Transferred amount must be a positive number";
        }
        Account fromAccount = customers.get(customer.getKey()).findAccount(splitRequest[2]);
        Account toAccount = customers.get(customer.getKey()).findAccount(splitRequest[3]);

        //check if the accounts are the same
        if (fromAccount.equals(toAccount)) {
            return "FAIL: accounts are the same.";
        }

        if (fromAccount.checkBalance(amount)) {
            fromAccount.changeBalanceBy(-amount);
            toAccount.changeBalanceBy(amount);
            return "SUCCESS";
        } else {
            return "FAIL: Insufficient funds";
        }
    }

    private String showMyAccounts(CustomerID customerID) {
        Customer customer = new Customer(customerID);

        return customer.accountsToString();
    }

    private String createNewAccount(CustomerID customer, String accountName) {
        if(customers.get(customer.getKey()).checkAcc(accountName)){
            return String.format("The account \"%s\" already exists", accountName);
        }else{
            List<String> account_names = new ArrayList<String>();
            account_names.add("Main");
            account_names.add("Savings");
            account_names.add("Checking");
            for (String acc : account_names) {
                if (acc.equals(accountName)) {
                    customers.get(customer.getKey()).addAccount(new Account(accountName, 0.0));
                    return "SUCCESS: New account is created";
                }
            }
        }
        return "FAIL:Invalid account name";
    }


    /*Checks if a given string is a double, and catches exceptions*/
    private boolean checkDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


}