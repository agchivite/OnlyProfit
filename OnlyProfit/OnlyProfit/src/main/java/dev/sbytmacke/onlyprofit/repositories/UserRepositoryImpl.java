package dev.sbytmacke.onlyprofit.repositories;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import dev.sbytmacke.onlyprofit.dto.UserDTO;
import dev.sbytmacke.onlyprofit.mappers.UserMapper;
import dev.sbytmacke.onlyprofit.models.UserEntity;
import dev.sbytmacke.onlyprofit.services.database.DatabaseManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

public class UserRepositoryImpl implements UserRepository<UserEntity, String> {

    private static final String COLLECTION_NAME = "users_bet";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_DATE_BET = "dateBet";
    private static final String FIELD_TIME_BET = "timeBet";
    private static final String FIELD_RELIABLE = "reliable";
    private static final String FIELD_TIMES_BET = "timeBets";

    private final DatabaseManager databaseManager;
    Logger logger = LoggerFactory.getLogger(getClass());

    public UserRepositoryImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    private static UserEntity mapDocumentToEntity(Document document) {
        String username = document.getString(FIELD_USERNAME);
        LocalDate dateBet = LocalDate.parse(document.getString(FIELD_DATE_BET));
        String timeBet = document.getString(FIELD_TIME_BET);
        Boolean reliable = document.getBoolean(FIELD_RELIABLE);
        Integer timesBet = document.getInteger(FIELD_TIMES_BET);
        int timesBetValue = (timesBet != null) ? timesBet : 0;
        return new UserEntity(username, dateBet, timeBet, reliable, timesBetValue);
    }

    @Override
    public UserEntity addItem(UserEntity userEntity) {
        String concatLog = "Adding user " + userEntity;
        logger.info(concatLog);

        // Conectar a la base de datos
        databaseManager.connectDatabase();

        // Obtener la colección de usuarios
        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        // Crear un documento a partir del usuario
        Document userDocument = new Document(FIELD_USERNAME, userEntity.getUsername())
                .append(FIELD_DATE_BET, userEntity.getDateBet().toString())
                .append(FIELD_TIME_BET, userEntity.getTimeBet())
                .append(FIELD_RELIABLE, userEntity.getReliable())
                .append(FIELD_TIMES_BET, userEntity.getTimesBet());

        // Insertar el documento en la colección
        collection.insertOne(userDocument);

        // Cerrar la conexión
        databaseManager.closeDatabase();

        return userEntity;
    }

    @Override
    public List<UserDTO> getAll() {
        logger.info("Finding all users");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        Document query = new Document();

        Map<String, UserEntity> userMap = new HashMap<>();

        MongoCursor<Document> cursor = collection.find(query).iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            UserEntity userEntity = mapDocumentToEntity(document);

            userMap.merge(userEntity.getUsername(), userEntity, (existing, newUser) -> {
                existing.setTimesBet(existing.getTimesBet() + newUser.getTimesBet());
                return existing;
            });
        }

        cursor.close();
        databaseManager.closeDatabase();

        List<UserEntity> uniqueUsersList = new ArrayList<>(userMap.values());

        UserMapper userMapper = new UserMapper();

        return userMapper.convertUserEntitiesToDTOs(uniqueUsersList, getAllEntity());
    }

    @Override
    public List<UserDTO> getAllByTime(String newTime) {
        logger.info("getAllByTime");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        Bson filter = Filters.eq(FIELD_TIME_BET, newTime);
        FindIterable<Document> result = collection.find(filter);

        Map<String, UserEntity> userMap = new HashMap<>();

        MongoCursor<Document> cursor = result.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            UserEntity user = mapDocumentToEntity(document);

            userMap.merge(user.getUsername(), user, (existing, newUser) -> {
                existing.setTimesBet(existing.getTimesBet() + newUser.getTimesBet());
                return existing;
            });
        }

        cursor.close();
        databaseManager.closeDatabase();

        UserMapper userMapper = new UserMapper();
        return userMapper.convertUserEntitiesToDTOs(new ArrayList<>(userMap.values()), getAllEntity());
    }

    @Override
    public List<UserDTO> getAllByDate(Integer newDate) {
        logger.info("getAllByDate");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        FindIterable<Document> result = collection.find();

        Map<String, UserEntity> userMap = new HashMap<>();

        MongoCursor<Document> cursor = result.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            LocalDate documentDateBet = LocalDate.parse(document.getString(FIELD_DATE_BET));
            int documentDayOfWeek = documentDateBet.getDayOfWeek().getValue();

            if (documentDayOfWeek == newDate) {
                UserEntity user = mapDocumentToEntity(document);
                userMap.merge(user.getUsername(), user, (existing, newUser) -> {
                    existing.setTimesBet(existing.getTimesBet() + newUser.getTimesBet());
                    return existing;
                });
            }
        }

        cursor.close();
        databaseManager.closeDatabase();

        UserMapper userMapper = new UserMapper();
        return userMapper.convertUserEntitiesToDTOs(new ArrayList<>(userMap.values()), getAllEntity());
    }

    @Override
    public List<UserDTO> getAllByDateTime(String newTime, Integer newDate) {
        logger.info("getAllByDateTime");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        Bson filter = Filters.eq(FIELD_TIME_BET, newTime);
        FindIterable<Document> result = collection.find(filter);

        Map<String, UserEntity> userMap = new HashMap<>();

        MongoCursor<Document> cursor = result.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            LocalDate documentDateBet = LocalDate.parse(document.getString(FIELD_DATE_BET));
            int documentDayOfWeek = documentDateBet.getDayOfWeek().getValue();

            if (documentDayOfWeek == newDate) {
                UserEntity user = mapDocumentToEntity(document);
                userMap.merge(user.getUsername(), user, (existing, newUser) -> {
                    existing.setTimesBet(existing.getTimesBet() + newUser.getTimesBet());
                    return existing;
                });
            }
        }

        cursor.close();
        databaseManager.closeDatabase();

        UserMapper userMapper = new UserMapper();
        return userMapper.convertUserEntitiesToDTOs(new ArrayList<>(userMap.values()), getAllEntity());
    }

    @Override
    public List<UserEntity> getAllEntity() {
        logger.info("Finding all users entity");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        // Consulta sin filtro
        Document query = new Document();

        // Itera sobre los resultados de la consulta
        List<UserEntity> usersList = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find(query).iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            UserEntity userEntity = mapDocumentToEntity(document);
            usersList.add(userEntity);
        }

        cursor.close();
        databaseManager.closeDatabase();

        return usersList;
    }

    @Override
    public List<UserEntity> getAllEntityByTime(String newTime) {
        logger.info("getAllEntityByTime");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        // Crear un filtro para encontrar documentos con el valor de "timeBet" igual a newTime
        Bson filter = Filters.eq(FIELD_TIME_BET, newTime);
        FindIterable<Document> result = collection.find(filter); // Consulta

        List<UserEntity> usersFiltered = new ArrayList<>();

        MongoCursor<Document> cursor = result.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            UserEntity user = mapDocumentToEntity(document);
            usersFiltered.add(user);
        }

        cursor.close();
        databaseManager.closeDatabase();

        return usersFiltered;
    }

    @Override
    public List<UserEntity> getAllEntityByDate(LocalDate newDate) {
        logger.info("getAllEntityByDate");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        Bson filter = Filters.eq(FIELD_DATE_BET, newDate.toString());
        FindIterable<Document> result = collection.find(filter); // Consulta

        List<UserEntity> usersFiltered = new ArrayList<>();

        MongoCursor<Document> cursor = result.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            UserEntity user = mapDocumentToEntity(document);
            usersFiltered.add(user);
        }

        cursor.close();
        databaseManager.closeDatabase();

        return usersFiltered;
    }

    @Override
    public List<UserEntity> getAllEntityByDateTime(String newTime, LocalDate newDate) {

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        Bson filter = Filters.and(
                Filters.eq(FIELD_TIME_BET, newTime),
                Filters.eq(FIELD_DATE_BET, newDate.toString())
        );

        FindIterable<Document> result = collection.find(filter); // Consulta

        ArrayList<UserEntity> usersFiltered = new ArrayList<>();

        MongoCursor<Document> cursor = result.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            UserEntity user = mapDocumentToEntity(document);
            usersFiltered.add(user);
        }

        cursor.close();
        databaseManager.closeDatabase();

        return usersFiltered;
    }

    @Override
    public Integer getGlobalTotalBetsByTime(String newTime) {
        logger.info("getGlobalTotalBetsByTime");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        Bson filter = Filters.and(Filters.eq(FIELD_TIME_BET, newTime));
        long result = collection.countDocuments(filter); // Consulta

        databaseManager.closeDatabase();

        return Math.toIntExact(result);
    }

    @Override
    public Integer getGlobalTotalBetsByDateTime(String newTime, Integer newDate) {
        logger.info("getGlobalTotalBetsByDateTime");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        Bson filter = Filters.and(Filters.eq(FIELD_TIME_BET, newTime));

        FindIterable<Document> result = collection.find(filter); // Consulta

        ArrayList<UserEntity> usersFiltered = new ArrayList<>();

        MongoCursor<Document> cursor = result.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            LocalDate documentDateBet = LocalDate.parse(document.getString(FIELD_DATE_BET));
            int documentDayOfWeek = documentDateBet.getDayOfWeek().getValue();

            if (documentDayOfWeek == newDate) {
                UserEntity user = mapDocumentToEntity(document);
                usersFiltered.add(user);
            }
        }

        cursor.close();
        databaseManager.closeDatabase();

        return Math.toIntExact(usersFiltered.size());
    }

    @Override
    public List<String> getAllUsernamesWithoutRepeat() {
        logger.info("getAllUsernames");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        Set<String> usernames = new HashSet<>();

        // Consulta para obtener todos los documentos
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                String username = document.getString(FIELD_USERNAME);
                usernames.add(username);
            }
        }

        // Convierte el HashSet en una List
        return new ArrayList<>(usernames);
    }

    @Override
    public Integer getGlobalTotalBetsByDate(Integer newDate) {
        logger.info("getGlobalTotalBetsByDate");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        FindIterable<Document> result = collection.find(); // Todos los documentos

        ArrayList<UserEntity> usersFiltered = new ArrayList<>();

        MongoCursor<Document> cursor = result.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            LocalDate documentDateBet = LocalDate.parse(document.getString(FIELD_DATE_BET));
            int documentDayOfWeek = documentDateBet.getDayOfWeek().getValue();

            if (documentDayOfWeek == newDate) {
                UserEntity user = mapDocumentToEntity(document);
                usersFiltered.add(user);
            }
        }

        cursor.close();
        databaseManager.closeDatabase();

        return Math.toIntExact(usersFiltered.size());
    }

    public void deleteItem(UserEntity item) {
        logger.info("deleteItem");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        // Construir un filtro basado en todos los campos del objeto item
        Bson filter = Filters.and(
                Filters.eq(FIELD_TIME_BET, item.getTimeBet()),
                Filters.eq(FIELD_DATE_BET, item.getDateBet().toString()), // Cuidado que me llega el valor como LocalDate y en la base de datos lo almacenamos como String
                Filters.eq(FIELD_USERNAME, item.getUsername()),
                Filters.eq(FIELD_RELIABLE, item.getReliable())
        ); // Debería hacer el filtro con el ObjectId, pero la cagué aquí

        DeleteResult deleteResult = collection.deleteOne(filter); // Solamente uno, por si alguno tiene lo mismo

        if (deleteResult.getDeletedCount() == 1) {
            logger.info("Usuario eliminado con éxito.");
        } else {
            logger.error("No se pudo eliminar el usuario.");
        }

        databaseManager.closeDatabase();
    }

    @Override
    public void updateUsername(String oldUsername, String newUsername) {
        logger.info("updateUsername " + oldUsername + " to " + newUsername);

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        // Crear un filtro para encontrar los documentos con el nombre de usuario antiguo
        Bson filter = Filters.eq(FIELD_USERNAME, oldUsername);

        // Crear una actualización para cambiar el valor del nombre de usuario
        Bson update = Updates.set(FIELD_USERNAME, newUsername);

        // Ejecutar la actualización en todos los documentos que coincidan con el filtro
        UpdateResult updateResult = collection.updateMany(filter, update);

        if (updateResult.getModifiedCount() > 0) {
            logger.info("Usuarios actualizados con éxito: " + updateResult.getModifiedCount());
        } else {
            logger.info("No se encontraron usuarios para actualizar con el nombre de usuario antiguo: " + oldUsername);
        }

        databaseManager.closeDatabase();
    }

    @Override
    public List<Document> backupData() {
        logger.info("backupData");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        FindIterable<Document> result = collection.find();

        List<Document> usersDocuments = new ArrayList<>();

        MongoCursor<Document> cursor = result.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();

            // Crear un nuevo documento sin el campo "_id" (ObjectId)
            Document filteredDocument = new Document();
            filteredDocument.put(FIELD_USERNAME, document.getString(FIELD_USERNAME));
            filteredDocument.put(FIELD_DATE_BET, document.getString(FIELD_DATE_BET));
            filteredDocument.put(FIELD_TIME_BET, document.getString(FIELD_TIME_BET));
            filteredDocument.put(FIELD_RELIABLE, document.getBoolean(FIELD_RELIABLE));

            usersDocuments.add(filteredDocument);
        }

        cursor.close();
        databaseManager.closeDatabase();

        return usersDocuments;
    }

    @Override
    public List<UserEntity> getAllBetsByUser(String username) {
        logger.info("getAllBetsByUser");

        databaseManager.connectDatabase();

        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        // Crear un filtro para encontrar los documentos con el nombre de usuario antiguo
        Bson filter = Filters.eq(FIELD_USERNAME, username);

        FindIterable<Document> result = collection.find(filter); // Consulta

        ArrayList<UserEntity> usersFiltered = new ArrayList<>();

        MongoCursor<Document> cursor = result.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            UserEntity user = mapDocumentToEntity(document);
            usersFiltered.add(user);
        }

        cursor.close();
        databaseManager.closeDatabase();

        return usersFiltered;
    }

    @Override
    public UserEntity userExists(UserEntity user) {
        UserEntity existingUser = null;

        databaseManager.connectDatabase();
        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        Bson filter = Filters.and(
                Filters.eq(FIELD_TIME_BET, user.getTimeBet()),
                Filters.eq(FIELD_DATE_BET, user.getDateBet().toString()),
                Filters.eq(FIELD_USERNAME, user.getUsername())
        );

        FindIterable<Document> result = collection.find(filter);
        MongoCursor<Document> cursor = result.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            existingUser = mapDocumentToEntity(document);
        }

        cursor.close();
        databaseManager.closeDatabase();

        return existingUser;
    }

    @Override
    public void newBetToExistingUser(UserEntity user) {
        databaseManager.connectDatabase();
        MongoCollection<Document> collection = databaseManager.getDatabase().getCollection(COLLECTION_NAME);

        Bson filter = Filters.and(
                Filters.eq(FIELD_TIME_BET, user.getTimeBet()),
                Filters.eq(FIELD_DATE_BET, user.getDateBet().toString()),
                Filters.eq(FIELD_USERNAME, user.getUsername())
        );

        Bson update = Updates.inc(FIELD_TIMES_BET, 1);

        UpdateResult updateResult = collection.updateOne(filter, update);

        databaseManager.closeDatabase();
    }
}
