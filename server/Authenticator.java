package newbank.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Authenticator implements IAuthenticator {
    @Override
    public CustomerID checkLoginDetails(String userName, String password) {

        try (BufferedReader reader = new BufferedReader(new FileReader("server/logins.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] login = line.split(",");

                if (login[0].equals(userName) && login[1].equals(password)) {
                    return new CustomerID(userName);
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot access logins database");
            Thread.currentThread().interrupt();
        }

        return null;
    }
}
