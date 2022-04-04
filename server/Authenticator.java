package newbank.server;

import newbank.Database.NewBankDB;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;

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
}
