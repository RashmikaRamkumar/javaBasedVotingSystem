package com.jaisanth.voting;

import com.jaisanth.database.DatabaseConnection;
import com.jaisanth.models.Candidate;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.*;

public class VotingSystem {

    private static List<Candidate> candidates = new ArrayList<>();
    private static final String PASSWORD = "Reveal_Password"; // Use a constant for the password
    private static MongoCollection<Document> collection;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Create a new collection for each run
        String collectionName = "Votes_" + System.currentTimeMillis();
        collection = DatabaseConnection.getDatabase().getCollection(collectionName);

        printBoxed("Welcome to the Online Voting System!");

        // Ask for the number of candidates
        printBoxed("Enter the number of candidates: ");
        int numCandidates = Integer.parseInt(scanner.nextLine());

        // Input candidate names
        for (int i = 1; i <= numCandidates; i++) {
            printBoxed("Enter name of candidate " + i + ": ");
            String name = scanner.nextLine();
            candidates.add(new Candidate(name));
        }

        printBoxed("Voting system is ready for voting!");

        // Start the voting process
        while (true) {
            printBoxed("Enter your vote (candidate number) or type 'exit' to finish voting: ");
            String input = scanner.nextLine();

            // Check if user wants to exit the voting
            if (input.equalsIgnoreCase("exit")) {
                if (confirmExit(scanner)) {
                    System.exit(0);  // Exit the application
                }
                continue; // Continue the loop if the user does not want to exit
            }

            if (isNumeric(input)) {
                int vote = Integer.parseInt(input);
                if (vote > 0 && vote <= candidates.size()) {
                    Candidate selectedCandidate = candidates.get(vote - 1);
                    selectedCandidate.incrementVotes();
                    printBoxed("You have voted for " + selectedCandidate.getName() + "!");

                    // Store vote in MongoDB
                    storeVoteInDatabase(selectedCandidate.getName(), "Voter_" + UUID.randomUUID()); // Use UUID for unique voter ID
                } else {
                    printBoxed("Invalid candidate number. Try again.");
                }
            } else {
                printBoxed("Invalid input. Please enter a number or 'exit'.");
            }
        }
    }

    // Store vote in MongoDB
    private static void storeVoteInDatabase(String candidateName, String voterId) {
        Document voteDocument = new Document("candidate", candidateName)
                .append("voterId", voterId); // Store voter ID or any unique identifier
        
        collection.insertOne(voteDocument);
    }

    // Helper method to check if a string is numeric
    public static boolean isNumeric(String str) {
        return str.matches("\\d+"); // Regex to check if str consists of digits only
    }

    // Confirm exit action
    private static boolean confirmExit(Scanner scanner) {
        printBoxed("Are you sure you want to exit the application? (yes/no): ");
        String response = scanner.nextLine();
        return response.equalsIgnoreCase("yes");
    }

    // Ask user if they want to publish results and handle password verification
    private static void publishResults(Scanner scanner) {
        printBoxed("Do you want to publish the results? (yes/no): ");
        String response = scanner.nextLine();

        if (response.equalsIgnoreCase("yes")) {
            printBoxed("Enter password to reveal voting results: ");
            String passwordInput = scanner.nextLine();

            if (passwordInput.equals(PASSWORD)) {
                displayVotingResults();
                askForNewSession(scanner);
            } else {
                printBoxed("Wrong password! Results will not be published.");
                askForNewSession(scanner);
            }
        } else {
            printBoxed("Results will not be published.");
            askForNewSession(scanner);
        }
    }

    // Display the voting results from MongoDB
    public static void displayVotingResults() {
        System.out.println("\n-----------------");
        System.out.println("| Voting Results: |");
        System.out.println("-----------------");
        
        // Retrieve and aggregate votes from MongoDB
        List<Document> results = collection.aggregate(Arrays.asList(
                new Document("$group", new Document("_id", "$candidate").append("totalVotes", new Document("$sum", 1)))
        )).into(new ArrayList<>());

        String winner = null;
        int maxVotes = 0;

        for (Document result : results) {
            String candidateName = result.getString("_id");
            int totalVotes = result.getInteger("totalVotes");
            System.out.println("| " + candidateName + ": " + totalVotes + " votes |");

            // Determine the winner
            if (totalVotes > maxVotes) {
                maxVotes = totalVotes;
                winner = candidateName;
            }
        }

        if (winner != null) {
            System.out.println("\nWinner: " + winner + " with " + maxVotes + " votes!");
        } else {
            System.out.println("\nNo votes were cast.");
        }

        System.out.println("-----------------");
    }

    private static void askForNewSession(Scanner scanner) {
        printBoxed("\nDo you want to start a new voting session? (yes/no): ");
        String newSessionResponse = scanner.nextLine();

        if (newSessionResponse.equalsIgnoreCase("yes")) {
            candidates.clear();  // Clear previous candidates
            main(new String[0]);  // Restart the voting process
        } else {
            printBoxed("Thank you for participating! Exiting application.");
            System.exit(0);  // End application
        }
    }

    private static void printBoxed(String message) {
        int length = message.length();
        
        System.out.println("-------------------------------");
        System.out.println("| " + message + " ".repeat(Math.max(0, length - message.length() - 2)) + "|");
        System.out.println("-------------------------------");
    }
}
