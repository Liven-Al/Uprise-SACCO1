import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Servertest2 {

    /* Provide the necessary database connection details-- */
    public static final String url = "jdbc:mysql://localhost/Uprise-SACCO";
    public static final String username = "root";
    public static final String password = "";
    private static Connection connection;
    private static Statement statement;
    private static int application_no;
    private static int total_loanrequested = 0;
    private static int memberdeposit = 0;
    private static int memberdeposits = 0;
    private static int totaldeposits = 0;
    private static int currentapplicationnumber = 0;
    private static int memberloanRequest = 0;
    private static ResultSet resultSet1 = null;
    private static ResultSet resultSet2 = null;
    private static ResultSet resultSet3 = null;
    private static ResultSet resultSet4 = null;
    private static ResultSet resultSet5 = null;




    public static void requestLoan(int memberID, int amount, int repayment_period) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();

            resultSet1 = statement
                    .executeQuery("SELECT COUNT(application_no) FROM loan_application WHERE status='pending'");
            int no_of_available_requests = 0;

            if (resultSet1.next()) {
                no_of_available_requests = resultSet1.getInt("COUNT(application_no)");
            }
            if (no_of_available_requests == 10) {
                String changeStatus = "UPDATE loan_application SET status = 'processing' WHERE status = 'pending'";
                statement.executeUpdate(changeStatus);
            }
            /* generate a random application_no value-- */
            Random random = new Random();
            application_no = 1000 + random.nextInt(9999);

            /* sql to handle loan request-- */
            String sql = "INSERT INTO loan_application(application_no,member_ID, amount, repayment_period) VALUES("
                    + application_no + "," + memberID + "," + amount + "," + repayment_period + ")";
            statement.executeUpdate(sql);
            System.out.println("Your loan application has been submitted successfully. Your application nummber is "
                    + application_no);

        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
    public static void distributeAndApproveLoans() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();

            // Calculate the total available funds
            resultSet2 = statement.executeQuery("SELECT SUM(account_balance) AS total_balance FROM member");
            int available_funds = 0;

            if (resultSet2.next()) {
                available_funds = resultSet2.getInt("total_balance") - 2000000; // Minus 2,000,000 for the condition
            }

            if (available_funds > 0) {
                // Fetch the list of loan applicants for processing
                resultSet3 = statement.executeQuery("SELECT * FROM loan_application WHERE status = 'processing'");
                List<Integer> loanRequests = new ArrayList<>();
                int totalLoanRequested = 0;

                while (resultSet3.next()) {
                    int requestedAmount = resultSet3.getInt("amount");
                    loanRequests.add(requestedAmount);
                    totalLoanRequested += requestedAmount;
                }

                if (totalLoanRequested <= available_funds) {
                    // Distribute loans among applicants based on contribution
                    resultSet4 = statement.executeQuery("SELECT member_ID, account_balance FROM member WHERE member_ID IN (SELECT member_ID FROM loan_application WHERE status = 'processing')");
                    List<Integer> memberIDs = new ArrayList<>();
                    List<Integer> memberDeposits = new ArrayList<>();
                    int totalMemberDeposits = 0;

                    while (resultSet4.next()) {
                        int memberID = resultSet4.getInt("member_ID");
                        int accountBalance = resultSet4.getInt("account_balance");
                        memberIDs.add(memberID);
                        memberDeposits.add(accountBalance);
                        totalMemberDeposits += accountBalance;
                    }

                    // Calculate the final loan amount for each member based on their deposit and requested amount
                    for (int i = 0; i < loanRequests.size(); i++) {
                        int requestedAmount = loanRequests.get(i);
                        int memberID = memberIDs.get(i);
                        int memberDeposit = memberDeposits.get(i);

                        double membershare = (double) requestedAmount / totalLoanRequested * available_funds;
                        double maxLoanAllowed = (3.0 / 4) * memberDeposit;
                        double finalLoanAmount = Math.min(requestedAmount, Math.min(membershare, maxLoanAllowed));

                        // Update the loan amount for each member in the loan_application table
                        String updateLoanQuery = "UPDATE loan_application SET amount = " + finalLoanAmount + " WHERE member_ID =" + memberID;
                        statement.executeUpdate(updateLoanQuery);
                    }
                }
            }

            // Close the resources
            resultSet2.close();
            resultSet3.close();
            resultSet4.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    finally {
    /*Close the resources */
     try {
            
            if (statement != null) statement.close();
            if (connection != null) connection.close();
            if (resultSet1 != null) resultSet1.close();
            if (resultSet2 != null) resultSet2.close();
            if (resultSet3 != null) resultSet3.close();
            if (resultSet4 != null) resultSet4.close();
            if (resultSet5 != null) resultSet5.close();
        
        
     } catch (Exception e) {
        e.printStackTrace();
     }      
    }
} 

    public static void main(String[] args) {
        requestLoan(90, 100000, 3);
        distributeandApproveLoans();

    }
}
