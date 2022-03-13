package newbank.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NewBankClientHandler extends Thread{
	
	private final NewBank bank;
	private final BufferedReader in;
	private final PrintWriter out;
	
	
	public NewBankClientHandler(Socket s) throws IOException {
		bank = NewBank.getBank();
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new PrintWriter(s.getOutputStream(), true);
	}
	
	public void run() {
		// keep getting requests from the client and processing them
		try {
			CustomerID customer = getUserLogin();

			while (customer == null){
				out.println("Invalid details, try again.");
				customer = getUserLogin();
			}

			processInput(customer);

		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}

	private void processInput(CustomerID customer) throws IOException {
		out.println("Log In Successful. What do you want to do?");
		String request = "";

		// Added this for now as while(true) was causing a warning
		while(!request.equals("EXIT")) {
			request = in.readLine();
			System.out.println("Request from " + customer.getKey());
			String response = bank.processRequest(customer, request);
			out.println(response);
		}
	}

	private CustomerID getUserLogin() throws IOException {
		// ask for user name
		out.println("Enter Username");
		String userName = in.readLine();
		// ask for password
		out.println("Enter Password");
		String password = in.readLine();
		out.println("Checking Details...");
		// authenticate user and get customer ID token from bank for use in subsequent requests
		// if the user is authenticated then get requests from the user and process them
		return bank.checkLogInDetails(userName, password);
	}

}
