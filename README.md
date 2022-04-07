# NewBank

Welcome to SE2  Group 9's NewBank project. Within this project there are three main components. 
The 'server', the 'client' and the 'Database'. The database in this case is using a SQLite implementation, 
and takes a simple set up step to get you ready to run the application.

## Getting Set Up

To start, you will need to specify the Java SDK for the project. To do this:

- Go to File > Project Structure > Project, and then select OpenJDK 17.0.2 (or whichever version you have)
from the 'SDK' dropdown menu

You will also need to add the SQLite dependency to the project module to be able to connect to the database. 
To do this:

- Go to File > Project Structure > Modules
- Click the '+' button
- Select the .JAR file located in the projects root directory
'\\newbank\sqlite-jdbc-3.36.0.3.JAR'

## How to Run

1. First run NewBankServer
   1. You should then see the console successfully print the port the server is running on, and that it has
   connected to the NewBankDB successfully.
2. Then run ExampleClient
   1. If successful, you should see the welcome message and can interact with the application.

**Happy Banking!**