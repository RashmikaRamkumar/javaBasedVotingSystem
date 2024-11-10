package com.jaisanth.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DatabaseConnection {
    private static MongoDatabase database;

    public static MongoDatabase getDatabase() {
    if (database == null) {
        try {
            MongoClient client = MongoClients.create("mongodb://localhost:27017");  // MongoDB URI
            database = client.getDatabase("VotingDB");
            System.out.println("Database created");
        } catch (Exception e) {
            System.err.println("Failed to connect to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
    return database;
}

}
