package newbank.server;

public interface IAuthenticator {

    CustomerID checkLoginDetails(String userName, String password);
}
