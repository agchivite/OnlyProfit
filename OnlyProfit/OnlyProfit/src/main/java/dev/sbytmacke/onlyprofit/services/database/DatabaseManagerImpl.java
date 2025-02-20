package dev.sbytmacke.onlyprofit.services.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import dev.sbytmacke.onlyprofit.constants.DatabaseConstants;

public class DatabaseManagerImpl implements DatabaseManager {
    private final String connectionString = DatabaseConstants.CONNECTION_STRING.getString();
    private final String databaseName = DatabaseConstants.DATABASE_NAME.getString();
    private MongoDatabase database = null;
    private MongoClient mongoClient = null;

    @Override
    public MongoDatabase connectDatabase() {

        mongoClient = MongoClients.create(connectionString);
        database = mongoClient.getDatabase(databaseName);

        return database;
    }

    @Override
    public void closeDatabase() {
        mongoClient.close();
    }

    @Override
    public MongoDatabase getDatabase() {
        return database;
    }

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }
}
