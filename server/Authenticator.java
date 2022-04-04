package newbank.server;

import newbank.Database.NewBankDB;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.Random;



public class Authenticator implements IAuthenticator {
    @Override
    public CustomerID checkLoginDetails(String username, String password) {

        try {

        Boolean loginCorrect = NewBankServer.newBankDB.checkLogin(username, password);

        if(loginCorrect){
            return new CustomerID(username);
        }

        } catch (Exception e) {
            System.out.println("Cannot access logins database");
            Thread.currentThread().interrupt();
        }

        return null;
    }

    public String generateUsername(String firstname) {
        Random rand = new Random();
        int randomInt = rand.nextInt(100, 999);
        String username = firstname + randomInt;

        while (!checkUsernameUnique(username)) {
            randomInt = rand.nextInt(100, 999);
            username = firstname + randomInt;
        }

        return username;
    }

    private Boolean checkUsernameUnique(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader("server/logins.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] login = line.split(",");

                if (login[0].equals(username)) {
                    return false;
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot access logins database");
            Thread.currentThread().interrupt();
        }
        return true;
    }
}
