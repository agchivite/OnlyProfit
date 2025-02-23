package dev.sbytmacke.onlyprofit.mappers;

import dev.sbytmacke.onlyprofit.dto.UserDTO;
import dev.sbytmacke.onlyprofit.models.UserEntity;
import dev.sbytmacke.onlyprofit.utils.GlobalStats;
import dev.sbytmacke.onlyprofit.utils.Statistics;

import java.text.DecimalFormat;
import java.util.*;

public class UserMapper {

    public Map<String, Integer> calculateUserMedians(List<UserEntity> allUsersEntity) {
        Map<String, List<Integer>> userBets = new HashMap<>();

        // Agrupar TODAS las apuestas de cada usuario sin importar la franja horaria
        for (UserEntity user : allUsersEntity) {
            String username = user.getUsername();
            userBets.putIfAbsent(username, new ArrayList<>());
            userBets.get(username).add(user.getTimesBet());
        }

        // Calcular la mediana de apuestas de cada usuario
        Map<String, Integer> userMedians = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : userBets.entrySet()) {
            String username = entry.getKey();
            System.out.println("ENTRY " + entry.getValue());

            //Integer median = Statistics.calculateMedian(entry.getValue());
            Integer average = Statistics.calculateAverage(entry.getValue());

            userMedians.put(username, average);

            // Log para depuración
            //System.out.println("MEDIANA de " + username + " -> " + median);
            System.out.println("MEDIA de " + username + " -> " + average);
        }

        return userMedians;
    }

    public List<UserDTO> convertUserEntitiesToDTOs(List<UserEntity> userEntities, List<UserEntity> allUsersEntity) {
        if (userEntities.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Integer> userMedians = calculateUserMedians(allUsersEntity);
        List<UserDTO> userDTOs = new ArrayList<>();
        List<Double> allBets = new ArrayList<>();
        List<Double> allSuccessRates = new ArrayList<>();

        for (UserEntity user : userEntities) {
            String username = user.getUsername();
            int userBets = user.getTimesBet();
            Integer medianBets = userMedians.get(username);

            if (medianBets != null && medianBets > 0) {
                double percentReliable = ((double) userBets / medianBets) * 100.0;

                UserDTO userDTO = new UserDTO(username, percentReliable, userBets, medianBets);
                userDTOs.add(userDTO);

                // Guardamos valores para calcular la mediana total
                allBets.add((double) userBets);
                allSuccessRates.add(percentReliable);

                // Log de depuración
                System.out.println("Usuario: " + username + ", Apuestas: " + userBets + ", Mediana: " + medianBets + ", PercentReliable: " + percentReliable);
            }
        }

        List<Integer> inteAllBets = new ArrayList<Integer>();
        for (int i = 0; i < allBets.size(); i++) {
            double doubleValue = allBets.get(i);
            int intValue = (int) Math.round(doubleValue);
            inteAllBets.add(intValue);
        }

        GlobalStats.medianTotalBets = (int) Statistics.calculateMedian(inteAllBets);
        GlobalStats.averageSuccessRate = allSuccessRates.stream().mapToDouble(val -> val).average().orElse(0.0);

        double portionSuccessRate = GlobalStats.averageSuccessRate / 3;
        GlobalStats.badAverageAllUsersSuccessRate = GlobalStats.averageSuccessRate - portionSuccessRate;
        GlobalStats.goodAverageAllUsersSuccessRate = GlobalStats.averageSuccessRate + portionSuccessRate;
        System.out.println("badThirdSuccessRate: " + GlobalStats.badAverageAllUsersSuccessRate);
        System.out.println("averageSuccessRate: " + GlobalStats.averageSuccessRate);
        System.out.println("goodSuccessRate: " + GlobalStats.goodAverageAllUsersSuccessRate);

        return userDTOs;
    }


    /*public List<UserDTO> convertUserEntitiesToDTOs(List<UserEntity> userEntities) {
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
    }*/


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
