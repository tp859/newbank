package newbank.server;

import newbank.Database.NewBankDB;

import java.util.HashMap;
import java.util.ArrayList;
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
        CustomerID bhagyID = new CustomerID("Bhagy");
        Customer bhagy = new Customer(bhagyID);
        bhagy.loadAccounts();
        //bhagy.addAccount(new Account(new CustomerID("Bhagy"), "Main", 1000.0));
        customers.put("Bhagy", bhagy);

        CustomerID christinaID = new CustomerID("Christina");
        Customer christina = new Customer(christinaID);
        christina.loadAccounts();
        //christina.addAccount(new Account(new CustomerID("Christina"), "Savings", 1500.0));
        customers.put("Christina", christina);

        CustomerID johnID = new CustomerID("John");
        Customer john = new Customer(johnID);
        john.loadAccounts();
        //john.addAccount(new Account(new CustomerID("John"), "Checking", 250.0));
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
            if (customers.containsKey(customer.getKey())) {
                String result = "Command in incorrect format. Try again.";
                switch (splitRequest[0]) {
                    case "SHOWMYACCOUNTS":
                        if (splitRequest.length == 1) {
                            result = showMyAccounts(customer);
                        }
                        break;

                    case "NEWACCOUNT":
                        if (!ourCurrency.moneyValid(splitRequest[1])) {
                            result = createNewAccount(customer, splitRequest[1]);
                        }
                        break;

                    case "MOVE":
                        if (splitRequest.length == 4) {
                            if (ourCurrency.moneyValid(splitRequest[1]) & !ourCurrency.moneyValid(splitRequest[2]) & !ourCurrency.moneyValid(splitRequest[3])) {
                                result = transferAccounts(customer, splitRequest);
                            }
                            break;
                        }

                    case "PAY":
                        // Fomat "PAY <customerID> <amount>
                        if (splitRequest.length == 3) {
                            if (ourCurrency.moneyValid(splitRequest[2])) {
                                result = payOther(customer, splitRequest);
                            }
                        }
                        break;

                    case "DEPOSIT":
                        // Format "DEPOSIT <amount> <accountName>
                        if (splitRequest.length == 3) {
                            if (ourCurrency.moneyValid(splitRequest[1]) & !ourCurrency.moneyValid(splitRequest[2])) {
                                result = deposit(customer, splitRequest);
                            }
                            break;
                        }

                    case "WITHDRAW":
                        if (splitRequest.length == 3) {
                            if (ourCurrency.moneyValid(splitRequest[1]) & !ourCurrency.moneyValid(splitRequest[2])) {
                                int amount = ourCurrency.convertToPennies(splitRequest[1]);
                                if (amount > 0) {
                                    result = withdraw(customer, amount, splitRequest[2]);
                                }
                            }
                            break;
                        }

                    case "END":
                        if (splitRequest.length == 1) {
                            result = "Code for exit goes here";
                        }
                        break;

                    case "SETOVERDRAFT":
                        // format SETOVERDRAFT <amount> <account>
                        if (splitRequest.length == 3) {
                            if (ourCurrency.moneyValid(splitRequest[1]) & !ourCurrency.moneyValid(splitRequest[2])) {
                                result = setOverdraft(customer, splitRequest);
                            }
                        }
                        break;

                    case "CHECKOVERDRAFT":
                        //format CHECKOVERDRAFT <account>
                        if (splitRequest.length == 2) {
                            if (!ourCurrency.moneyValid((splitRequest[1]))) {
                                result = checkAccountOverdraft(customer, splitRequest);
                            }
                        }
                        break;

                    default:
                        throw new NullPointerException("Command in incorrect format. Try again.");
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
        if (overdraft < 0 || overdraft > 150000){
            return ("FAIL: overdraft limit must be between £0 and £1500");
        }
        if (NewBankServer.newBankDB.getCustomerAccountBalance(customer.getKey(), splitRequest[2]) < 0){
            return "FAIL: Overdraft increase unavailable for the accounts with negative balance";
        }
        try {
            NewBankServer.newBankDB.updateCustomerAccountOverdraft(customer.getKey(), splitRequest[2], overdraft);
            int overdraftBalance = customers.get(customer.getKey()).findAccount(splitRequest[2]).getOverdraft();
            return "SUCCESS: The new overdraft limit for " + splitRequest[2] + " is " + ourCurrency.printMoney(overdraftBalance);

        } catch (NullPointerException e){
            return ("FAIL: No account found with that name.");
        }
    }

    private String checkAccountOverdraft(CustomerID customer, String[] splitRequest){
        try{
            int overdraftAmount = NewBankServer.newBankDB.getCustomerAccountOverdraft(customer.getKey(), splitRequest[1]);
            if (overdraftAmount > 0) {
                return "The available overdraft for " + splitRequest[1] + " is " + ourCurrency.printMoney(overdraftAmount);
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
                customerAccount.changeBalanceBy(-(amount + 2000)); // £20/2000p is fixed fine for overdraft transaction
                return String.format("SUCCESS: The new balance for Account \"%s\" is %s", account, customerAccount.printBalance());
            }
            return String.format("FAIL: Maximum overdraft for the Account \"%s\" is %s", account, customerAccount.printOverdraft());
        }
        return String.format("FAIL: The Account \"%s\" does not have enough balance, and it has no overdraft set up.", account);
    }

    private String payOther(CustomerID fromCustomer, String[] splitRequest) {
            int amount = ourCurrency.convertToPennies(splitRequest[2]);
            String toCustomer = splitRequest[1];

            Customer fromCustomerDetails = customers.get(fromCustomer.getKey());
            Account fromCustomerAccount = fromCustomerDetails.getFirstAccount();

            Customer toCustomerDetails = customers.get(toCustomer);
            Account toCustomerAccount = toCustomerDetails.getFirstAccount();

            if (fromCustomerAccount.getBalance() >= amount) {
                fromCustomerAccount.changeBalanceBy(-amount);
                return String.format("SUCCESS: The new balance for Account \"%s\" is %s", fromCustomerDetails.getFirstAccount().getAccountName(), fromCustomerAccount.printBalance());
            }
            else if (fromCustomerAccount.getBalance() < amount && fromCustomerAccount.getOverdraft() > 0) {
                if (fromCustomerAccount.approveOverdraft(amount)) {
                    fromCustomerAccount.changeBalanceBy(-(amount + 2000));
                    toCustomerAccount.changeBalanceBy(amount);; // £20/2000p is fixed fine for overdraft transaction
                    return "SUCCESS";
                }
            }

            toCustomerAccount.changeBalanceBy(+amount);
            return String.format("SUCCESS: The new balance for Account \"%s\" is %s", toCustomerDetails.getFirstAccount().getBalance(), toCustomerAccount.printBalance());
    }

    private String transferAccounts(CustomerID customer, String[] splitRequest) {
        Account from = null;
        Account to = null;
        int amount = ourCurrency.convertToPennies(splitRequest[1]);
        if (amount <= 0) {
            return "FAIL: Transferred amount must be a positive number";
        }
        //regarding code below, assigning accounts,
        //I was not sure how best to call values between Customer class and db, therefore
        //might seem chunky
        ArrayList<Account> UserAccounts = NewBankServer.newBankDB.getAccountsForCustomer(customer.getKey());
        for(Account acc: UserAccounts) {
            if (acc.getAccountName().equals(splitRequest[2])) {
                from = acc;
                break;
            }
        }
        for(Account acc: UserAccounts) {
            if (acc.getAccountName().equals(splitRequest[3])) {
                to = acc;
                break;
            }
        }
        if (from == null){
            return String.format("FAIL: Account \"%s\" does not exist", splitRequest[2]);
        }
        if (to == null){
            return String.format("FAIL: Account \"%s\" does not exist", splitRequest[3]);
        }
        //check if the accounts are the same
        if (from.equals(to)) {
            return "FAIL: accounts are the same.";
        }

        if (from.checkBalance(amount)) {
            from.changeBalanceBy(-amount);
            to.changeBalanceBy(amount);
            return "SUCCESS";
        } else if (from.getBalance() < amount && from.getOverdraft() > 0) {
            if (from.approveOverdraft(amount)) {
                from.changeBalanceBy(-(amount + 2000)); // £20/2000p is fixed fine for overdraft transaction
                to.changeBalanceBy(amount); 
                return "SUCCESS";
            }
        }
        return "FAIL: Insufficient funds";
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
                    customers.get(customer.getKey()).addAccount(new Account(customer, accountName, 0.0));
                    NewBankServer.newBankDB.addCustomerAccount(customer.getKey(), customers.get(customer.getKey()).findAccount(accountName));
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
