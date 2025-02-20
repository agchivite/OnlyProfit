package dev.sbytmacke.onlyprofit.repositories;

import dev.sbytmacke.onlyprofit.dto.UserDTO;
import dev.sbytmacke.onlyprofit.models.UserEntity;
import org.bson.Document;

import java.time.LocalDate;
import java.util.List;

public interface UserRepository<U, N> extends Repository<U, N> {

    List<UserDTO> getAllByTime(N newTime);

    List<UserDTO> getAllByDate(Integer newDate);

    List<UserDTO> getAllByDateTime(String newTime, Integer newDate);

    List<U> getAllEntity();

    List<U> getAllEntityByTime(N newTime);

    List<U> getAllEntityByDate(LocalDate newDate);

    List<U> getAllEntityByDateTime(N newTime, LocalDate newDate);


    Integer getGlobalTotalBetsByTime(String newTime);

    Integer getGlobalTotalBetsByDateTime(String newTime, Integer newDate);

    Integer getGlobalTotalBetsByDate(Integer newDate);

    List<N> getAllUsernamesWithoutRepeat();

    void deleteItem(U user);

    void updateUsername(N oldUsername, N newUsername);

    List<Document> backupData();

    List<U> getAllBetsByUser(N username);

    UserEntity userExists(UserEntity user);

    void newBetToExistingUser(UserEntity username);
}

