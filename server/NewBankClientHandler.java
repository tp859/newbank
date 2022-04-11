package newbank.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.FileReader;

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
		// Keep getting requests from the client and processing them
		try {
			CustomerID customer = null;

			while (customer == null) {
				// Load welcome text from file
				loadMainMenu();

				String selectedOption = getMenuOption();

				// Check option is valid
				while (!selectedOption.equals("1") && !selectedOption.equals("2")) {
					out.println("Invalid option, please try again.");
					selectedOption = getMenuOption();
				}

				if (selectedOption.equals("1")) {
					// Existing user
					out.println("Welcome back!");
					customer = getUserLogin();

					// Handle authentication fail
					while (customer == null) {
						out.println("Invalid details, would you like to retry (y/n)?");
						selectedOption = getMenuOption();
						while (!selectedOption.equals("n") && !selectedOption.equals("y")) {
							out.println("Invalid option, please try again.");
							selectedOption = getMenuOption();
						}
						if (selectedOption.equals("n")) {
							break;
						} else {
							customer = getUserLogin();
						}
					}
				} else {
					// New user
					customer = createUserLogin();
				}
			}

			bank.initialiseCustomer(customer);
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
		while(!request.equals("END")) {
			request = in.readLine();
			System.out.println("Request from " + customer.getKey());
			String response = bank.processRequest(customer, request);
			out.println(response);
		}
	}

	// Loads welcome text from file
	private void loadMainMenu() {
		try (BufferedReader br = new BufferedReader(new FileReader("server/mainmenu.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	private String getMenuOption() throws IOException {
		String option = in.readLine();
		return option;
	}

	private CustomerID createUserLogin() throws IOException {
		out.println("Welcome to user setup");
		out.println("Enter your firstname to generate a username:");
		String firstname = in.readLine();
		firstname = checkNullInput("Firstname", firstname);

		// Generate username
		String username = bank.generateUsername(firstname);
		out.printf("Thanks, your username is %s, please make a note of this.\n", username);

		out.println("Now enter a password:");
		String password = in.readLine();
		password = checkNullInput("Password", password);

		out.println("Thanks, please re-enter your password to confirm:");
		String passwordConfirmed = in.readLine();
		while (!password.equals(passwordConfirmed)) {
			out.println("Passwords do not match, please try again");
			out.println("Please enter a password:");
			password = in.readLine();
			password = checkNullInput("Password", password);
			out.println("Thanks, please re-enter your password to confirm:");
			passwordConfirmed = in.readLine();
		}
		NewBankServer.newBankDB.addLogin(username, password);

		out.println("Thanks, setup complete, logging in...");
		return new CustomerID(username);
	}

	private String checkNullInput(String field, String input) throws IOException {

		while (input.isEmpty() || input.isBlank()) {
			out.printf("%s cannot be empty, please try again\n", field);
			out.printf("Please enter your %s:\n", field);
			input = in.readLine();
		}
		return input;
	}

	private CustomerID getUserLogin() throws IOException {
		// ask for username
		out.println("Enter Username:");
		String userName = in.readLine();
		// ask for password
		out.println("Enter Password:");
		String password = in.readLine();
		out.println("Checking Details...");
		// authenticate user and get customer ID token from bank for use in subsequent requests
		// if the user is authenticated then get requests from the user and process them
		return bank.checkLogInDetails(userName, password);
	}
}
