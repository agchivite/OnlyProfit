package dev.sbytmacke.onlyprofit.mappers;

import dev.sbytmacke.onlyprofit.dto.UserDTO;
import dev.sbytmacke.onlyprofit.models.UserEntity;
import dev.sbytmacke.onlyprofit.utils.GlobalStats;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class UserMapper {

    public List<UserDTO> convertUserEntitiesToDTOs(List<UserEntity> userEntities) {
        ArrayList<UserDTO> userDTOs = new ArrayList<>();

        for (UserEntity userEntity : userEntities) {
            String username = userEntity.getUsername();
            int totalBets = userEntity.getTimesBet();
            int totalSuccessfulBets = userEntity.getTimesBet();
            double percentSuccess = calculatePercentUserByGlobal(totalBets);

            UserDTO userDTO = new UserDTO(username, percentSuccess, totalBets, totalSuccessfulBets);
            userDTOs.add(userDTO);
        }

        return userDTOs;
    }

    private double calculatePercentUserByGlobal(int totalBets) {
        if (totalBets > 0) {
            int globalMedianStat = GlobalStats.medianTotalBets;
            double percentValue = ((double) totalBets / globalMedianStat) * 100;
            DecimalFormat df = new DecimalFormat("#.##"); // Establecemos el formato a dos decimales
            return percentValue; //df.format(percentValue); // Redondeamos el valor y se convierte a String
        } else {
            return 0.0; // Evitar división por cero
        }
    }
}
