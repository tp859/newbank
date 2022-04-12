package newbank.server;

import newbank.server.Constants.*;
import java.util.ArrayList;
import java.util.List;

public class NewBank {

    private static final NewBank bank = new NewBank();
    private Customer currentCustomer;
    Authenticator authenticator = new Authenticator();

    private NewBank() {

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

    public void initialiseCustomer(CustomerID customerID) {
        this.currentCustomer = new Customer(customerID);
        this.currentCustomer.loadAccounts();
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
                            result = showMyAccounts();
                        }
                        break;

                    case Commands.CREATE_ACCOUNT:
                        if (!ourCurrency.moneyValid(splitRequest[1])) {
                            result = createNewAccount(splitRequest[1]);
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
                                result = payOther(splitRequest);
                            }
                        }
                        break;

                    case Commands.DEPOSIT:
                        // Format "DEPOSIT <amount> <accountName>
                        if (splitRequest.length == 3) {
                            if (ourCurrency.moneyValid(splitRequest[1]) & !ourCurrency.moneyValid(splitRequest[2])) {
                                result = deposit(splitRequest);
                            }
                            break;
                        }

                    case Commands.WITHDRAW:
                        if (splitRequest.length == 3) {
                            if (ourCurrency.moneyValid(splitRequest[1]) & !ourCurrency.moneyValid(splitRequest[2])) {
                                int amount = ourCurrency.convertToPennies(splitRequest[1]);
                                if (amount > 0) {
                                    result = withdraw(amount, splitRequest[2]);
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
                        throw new NullPointerException("Command in incorrect format. Try again.");
                }

                // Refresh data after transaction
                currentCustomer.loadAccounts();

                return result;
            }
        } catch (Exception e) {
            return (e.getMessage());
        }

        return "Some error happened";
    }

    //Setting up overdraft of up to 1500 £ for individual accounts
    private String setOverdraft(CustomerID customer, String[] splitRequest) {
        int overdraft = ourCurrency.convertToPennies(splitRequest[1]);
        if (overdraft < 0 || overdraft > 150000) {
            return ("FAIL: overdraft limit must be between £0 and £1500");
        }
        if (NewBankServer.newBankDB.getCustomerAccountBalance(customer.getKey(), splitRequest[2]) < 0) {
            return "FAIL: Overdraft increase unavailable for the accounts with negative balance";
        }
        try {
            NewBankServer.newBankDB.updateCustomerAccountOverdraft(customer.getKey(), splitRequest[2], overdraft);
            int overdraftBalance = currentCustomer.findAccount(splitRequest[2]).getOverdraft();
            return "SUCCESS: The new overdraft limit for " + splitRequest[2] + " is " + ourCurrency.printMoney(overdraftBalance);

        } catch (NullPointerException e) {
            return ("FAIL: No account found with that name.");
        }
    }

    private String checkAccountOverdraft(CustomerID customer, String[] splitRequest) {
        try {
            int overdraftAmount = NewBankServer.newBankDB.getCustomerAccountOverdraft(customer.getKey(), splitRequest[1]);
            if (overdraftAmount > 0) {
                return "The available overdraft for " + splitRequest[1] + " is " + ourCurrency.printMoney(overdraftAmount);
            } else {
                return "No overdraft set up for the " + splitRequest[1] + " account";
            }
        } catch (NullPointerException e) {
            return ("FAIL: No account found with that name.");
        }
    }

    private String deposit(String[] splitRequest) {
        int deposit = ourCurrency.convertToPennies(splitRequest[1]); // No need to check this as format checked when input read
        // Check not trying to withdraw using deposit command
        if (deposit < 0) {
            return ("FAIL: Deposit amount must be a positive number");
        }

        currentCustomer.findAccount(splitRequest[2]).changeBalanceBy(deposit);
        String newBalance = currentCustomer.findAccount(splitRequest[2]).printBalance();
        return "SUCCESS: The new balance for " + splitRequest[2] + " is " + newBalance;
    }

    private String withdraw(int amount, String account) {
        Account customerAccount = currentCustomer.findAccount(account);

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

    private String payOther(String[] splitRequest) {
        int amount = ourCurrency.convertToPennies(splitRequest[2]);
        CustomerID toCustomer = new CustomerID(splitRequest[1]);
        if (NewBankServer.newBankDB.checkUniqueUsername(toCustomer.getKey())) {
            return ("FAIL: Recipient not found");
        }
        Account fromCustomerAccount = currentCustomer.getFirstAccount();

        Customer toCustomerDetails = new Customer(toCustomer);
        toCustomerDetails.loadAccounts();
        Account toCustomerAccount = toCustomerDetails.getFirstAccount();

        // Update paying Customer's account
        if (fromCustomerAccount.getBalance() >= amount) {
            fromCustomerAccount.changeBalanceBy(-amount);
            NewBankServer.newBankDB.updateCustomerAccountBalance(currentCustomer.getCustomerID().getKey(), fromCustomerAccount.getAccountName(), fromCustomerAccount.getBalance());

            // Update receiving Customers account
            toCustomerAccount.changeBalanceBy(+amount);
            NewBankServer.newBankDB.updateCustomerAccountBalance(toCustomer.getKey(), toCustomerAccount.getAccountName(), toCustomerAccount.getBalance());

            return String.format("SUCCESS: The new balance for Account \"%s\" is %s", fromCustomerAccount.getAccountName(), fromCustomerAccount.printBalance());
        }
        else if (fromCustomerAccount.getBalance() < amount && fromCustomerAccount.getOverdraft() > 0) {
            if (fromCustomerAccount.approveOverdraft(amount)) {
                fromCustomerAccount.changeBalanceBy(-(amount + 2000));
                toCustomerAccount.changeBalanceBy(amount); // £20/2000p is fixed fine for overdraft transaction
                return "SUCCESS";
            }
        }

        return String.format("FAIL: The account \"%s\" does not have enough balance available.", fromCustomerAccount.getAccountName());
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
        for (Account acc : UserAccounts) {
            if (acc.getAccountName().equals(splitRequest[2])) {
                from = acc;
                break;
            }
        }
        for (Account acc : UserAccounts) {
            if (acc.getAccountName().equals(splitRequest[3])) {
                to = acc;
                break;
            }
        }
        if (from == null) {
            return String.format("FAIL: Account \"%s\" does not exist", splitRequest[2]);
        }
        if (to == null) {
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

    private String showMyAccounts() {
        return currentCustomer.accountsToString();
    }

    private String createNewAccount(String accountName) {
        if (currentCustomer.checkAcc(accountName)) {
            return String.format("The account \"%s\" already exists", accountName);
        } else {
            List<String> account_names = new ArrayList<>();
            account_names.add("Main");
            account_names.add("Savings");
            account_names.add("Checking");
            for (String acc : account_names) {
                if (acc.equals(accountName)) {
                    currentCustomer.addAccount(new Account(currentCustomer.getCustomerID(), accountName, 0));
                    return "SUCCESS: New account is created";
                }
            }
        }
        return "FAIL:Invalid account name";
    }
}
