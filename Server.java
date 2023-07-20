import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Random;
public class Server {
    private static Connection connection;
    
        
    /* login --edwin */
    public static boolean login(String username, String password) {
        
        PreparedStatement statement = null;
        ResultSet resultSet = null;

    try {
    /*databse connection */
        String url = "jdbc:mysql://localhost/Uprise-SACCO";
        String dbUsername = "root";
        String dbPassword = "";
    /*Register JDBC driver */
        Class.forName("com.mysql.cj.jdbc.Driver");
    /* Open a connection to the database*/
        connection = DriverManager.getConnection(url, dbUsername, dbPassword);
    /*SQL statement to check login credentials */
        String sql = "SELECT COUNT(*) FROM Member WHERE username = ? AND password = ?";
        statement = connection.prepareStatement(sql);
        statement.setString(1, username);
        statement.setString(2, password);  
        
    /*execute query and retrieve result */
        resultSet = statement.executeQuery();
        if (resultSet.next()){
            int count = resultSet.getInt(1);
            return count > 0;
        }  
    } catch (Exception e) {
        e.printStackTrace();
    } finally{
    /*close resources */
    try {
        if (resultSet != null) resultSet.close();
        if (statement != null) statement.close();
        if (connection != null) connection.close();
    } catch (Exception e) {
        e.printStackTrace();
    }    
    }  
    return false;
    }

    /*deposit method --Vanessa */

    
    /* checkStatement --pius */


    /* loan request -- allan */
    public static int requestLoan(int memberID, int amount, int payment_period){
        
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        int application_no = 0;
        int member_ID = 0;
        int memberDeposits = 0;

        PreparedStatement loanRequestStatement = null;
        PreparedStatement approvalStatement = null;
         try {
       
    /*  Provide the necessary database connection details--*/
            String url = "jdbc:mysql://localhost/Uprise-SACCO";
            String username = "root";
            String password = "";
            
    /*  Register the JDBC driver--*/
            Class.forName("com.mysql.cj.jdbc.Driver");

            
    /*  Open a connection to the database---*/
            connection = DriverManager.getConnection(url, username, password);  
        
    /*  generate a random application_no value-- */ 
            Random random = new Random();
            application_no = 1000 + random.nextInt(9999);            
            
    /*  sql to handle loan request-- */
        String sql = "INSERT INTO Loan(application_no,member_ID, amount, repayment_period) VALUES(?, ?, ?, ?)";
        statement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);

    /*  calculate equitable loan distribution-- */
        int totalFunds = getTotalFunds_fromDatabase(connection);
        
    /*  get deposits for the specific member-- */
         memberDeposits = getTotalDeposits_fromDatabase(connection, memberID); 
         double percentage = (double) memberDeposits / totalFunds;
         int equitableAmount = (int) (amount * percentage);


    /*  insert loan request into loanRequestApproval table after compiling-- */
    
        String loanRequestSql = "INSERT INTO LoanRequest_approval(application_no, member_ID, amount, repayment_period) VALUES (?, ?, ?, ?)";
        loanRequestStatement  = connection.prepareStatement(loanRequestSql);
        loanRequestStatement.setInt(1, application_no);
        loanRequestStatement.setInt(2, member_ID);
        loanRequestStatement.setInt(3, equitableAmount);
        loanRequestStatement.setInt(4, payment_period);
        loanRequestStatement.executeUpdate();
        
        

    /*set the parameter values */
        statement.setInt(1, application_no);
        statement.setInt(2, memberID);
        statement.setInt(3, amount);
        statement.setInt(4, payment_period); 
    
       
    /*execute the query and retrieve generated keys*/
        statement.executeUpdate();
        generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            application_no = generatedKeys.getInt(1);
        }
    
    /*close resources */
        generatedKeys.close();
        statement.close();
        loanRequestStatement.close();
        connection.close();
        
    } catch (Exception e) {
         e.printStackTrace();
    }finally {
    /*Close the resources */
     try {
            if (generatedKeys != null) generatedKeys.close();
            if (statement != null) statement.close();
            if (loanRequestStatement != null) loanRequestStatement.close();
            if (approvalStatement != null) approvalStatement.close();
            if (connection != null) connection.close();
        
     } catch (Exception e) {
        e.printStackTrace();
     }      
    }
    return application_no;
} 
private static int getMemberID(String username, String password) {
    int memberID = 0; // Initialize the memberID variable

    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
        // Provide the necessary database connection details
        String url = "jdbc:mysql://localhost/Uprise-SACCO";
        String dbUsername = "root";
        String dbPassword = "";

        // Register the JDBC driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Open a connection to the database
        connection = DriverManager.getConnection(url, dbUsername, dbPassword);

        // Prepare the SQL statement to retrieve the member_ID
        String sql = "SELECT member_ID FROM Member WHERE username = ? AND password = ?";
        statement = connection.prepareStatement(sql);
        statement.setString(1, username);
        statement.setString(2, password);

        // Execute the query and retrieve the result
        resultSet = statement.executeQuery();
        if (resultSet.next()) {
            memberID = resultSet.getInt("member_ID");
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        // Close the resources
        try {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    return memberID;
} 
    
    /*method for getting total funds in SACCO */
    private static int getTotalFunds_fromDatabase(Connection connection){
        int totalFunds = 0;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

    try {
        String sql = "SELECT DepositAmount from Sacco";
        statement = connection.prepareStatement(sql);
        resultSet = statement.executeQuery();
    if (resultSet.next()){
        totalFunds = resultSet.getInt("DepositAmount");
    }
    
    } catch (SQLException e) {
        e.printStackTrace();

    } finally {
    /*  Close resources-- */
       try {
        if (resultSet != null) resultSet.close();
        if (statement != null) statement.close();
        
       } catch (SQLException e) {
        e.printStackTrace();
       }  
    }  
    return totalFunds; 
    } 
    /*  method for capturing the number of loan requests-- */
    private static int getNumLoanRequest_fromDatabase(Connection connection){
        int numLoanRequest = 0;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

    try {
        String sql = "SELECT COUNT(application_no) FROM Loan";
        statement = connection.prepareStatement(sql);
        resultSet = statement.executeQuery();
        if (resultSet.next()){
            numLoanRequest=resultSet.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    } finally{                         
    /*  close resources-- */      
    try {
        if (resultSet != null) resultSet.close();
        if (statement != null) statement.close();
        
    } catch (SQLException e) {
        e.printStackTrace();
      }
    }     
       return numLoanRequest;
    }  
    /*  method for returning deposits made per member-- */
    private static int getTotalDeposits_fromDatabase(Connection connection, int member_ID) {

        int memberDeposits = 0;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

    try {
        String sql = ("SELECT amount from Deposit WHERE member_ID = ?");
        statement = connection.prepareStatement(sql);
        statement.setInt(1, member_ID);
        resultSet = statement.executeQuery();

        if (resultSet.next()) {
            memberDeposits = resultSet.getInt("amount");
        }

    } catch (SQLException e) {
        e.printStackTrace();
    } finally{

    /*  close resources-- */
    try {
        if (resultSet != null) resultSet.close();
        if (statement != null) statement.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }    

    }
    return memberDeposits;
    }



    /* loan request status -- taras*/
    

    public static void main(String[] args) {
        int memberID = 0;
        Connection connection = null;
         // Provide the necessary database connection details
        String url = "jdbc:mysql://localhost/Uprise-SACCO";
        String username = "root";
        String password = "";
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("Server is running. Waiting for a client to connect...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected.");

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
       
            int application_no = 0;
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] command = inputLine.split(" ");
                if (command.length > 1 && command.length < 5) {
                   switch (command[0]) {
                    case "login": 
                        if(login(command[1], command[2]) == true) {
                            out.println("Successfully logged in");
                            memberID = getMemberID(command[1], command[2]);
                        } else {
                            out.println("login failed");
                        }
                        break;
                    case "deposit":
                        /* call the deposit method here */

                    case "CheckStatement":
                        /* call the CheckStatement method here */

                    case "requestLoan":
                        
                         /*  Parse the amount and payment_period from the command --*/
                            int amount = Integer.parseInt(command[1]);
                            int payment_period = Integer.parseInt(command[2]);
                         
                         /*  check availability of funds-- */  
                            connection = DriverManager.getConnection(url, username, password);
                            int totalFunds = getTotalFunds_fromDatabase(connection);
                            if (totalFunds < 2000000){
                            out.println("Insufficient Funds, Loan Request can not be processed");
                            break;
                        } 
                        /*   check number of loan requests */  
                            
                            int numLoanRequest = getNumLoanRequest_fromDatabase(connection);
                        if (numLoanRequest >= 10) {
                            
                             out.println("Maximum loan requests reached. Loan Request cannot be processed.");
                                break;
                            
                        } else {
                            application_no = requestLoan(memberID, amount, payment_period);

                            out.println("Loan Request received successfully" +"," +"Application number: "+application_no);
                            
                        }
                        break;

                    case "LoanRequestStatus":
                        /* call the LoanRequestStatus method here */

                   }
                } else {
                    out.println("Invalid command");
                }
            }

            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
          catch (SQLException e) {
            e.printStackTrace();
          }
    }
}
