package dev.sbytmacke.onlyprofit.mappers;

import dev.sbytmacke.onlyprofit.dto.UserDTO;
import dev.sbytmacke.onlyprofit.models.UserEntity;
import dev.sbytmacke.onlyprofit.utils.GlobalStats;
import dev.sbytmacke.onlyprofit.utils.Statistics;

import java.text.DecimalFormat;
import java.util.*;

public class UserMapper {

    public Map<String, Integer> calculateUserAveragesByDayHour(List<UserEntity> allUsersEntity) {
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

            //Integer median = Statistics.calculateMedian(entry.getValue());
            Integer average = Statistics.calculateAverage(entry.getValue());

            userMedians.put(username, average);

            // Log para depuración
            //System.out.println("MEDIANA de " + username + " -> " + median);
            System.out.println("[MODE DAYHOUR] MEDIA de " + username + " -> " + average);
        }

        return userMedians;
    }

    public Map<String, Integer> calculateUserAveragesByDay(List<UserEntity> allUsersEntity) {
        Map<String, List<Integer>> userBetsByDay = new HashMap<>();

        for (UserEntity user : allUsersEntity) {
            String key = user.getUsername() + "-" + user.getDateBet().getDayOfWeek(); // Clave: usuario + día de la semana

            userBetsByDay.putIfAbsent(key, new ArrayList<>());
            userBetsByDay.get(key).add(user.getTimesBet());
        }

        // Sumar todas las apuestas por franja horaria
        Map<String, Integer> totalBetsByDay = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : userBetsByDay.entrySet()) {
            int sum = entry.getValue().stream().mapToInt(Integer::intValue).sum();
            totalBetsByDay.put(entry.getKey(), sum);
        }


        // Imprimir apuestas totales por franja horaria
        for (Map.Entry<String, Integer> entry : totalBetsByDay.entrySet()) {
            System.out.println("Apuestas de " + entry.getKey() + ": " + entry.getValue());
        }

        // Agrupar las sumas de apuestas por usuario (ignorando la franja horaria)
        Map<String, List<Integer>> userBetsTotal = new HashMap<>();
        for (Map.Entry<String, Integer> entry : totalBetsByDay.entrySet()) {
            String username = entry.getKey().split("-")[0]; // Obtener solo el nombre de usuario

            userBetsTotal.putIfAbsent(username, new ArrayList<>());
            userBetsTotal.get(username).add(entry.getValue());
        }

        // Calcular la media de apuestas por usuario
        Map<String, Integer> userAverages = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : userBetsTotal.entrySet()) {
            String username = entry.getKey();
            int average = Statistics.calculateAverage(entry.getValue()); // Calcula la media
            userAverages.put(username, average);
            System.out.println("[MODE DAY] MEDIA de " + username + " -> " + average);
        }

        return userAverages;
    }

    public Map<String, Integer> calculateUserAveragesByHour(List<UserEntity> allUsersEntity) {
        Map<String, List<Integer>> userBetsByHour = new HashMap<>();

        // Agrupar todas las apuestas de cada usuario por franja horaria
        for (UserEntity user : allUsersEntity) {
            String key = user.getUsername() + "-" + user.getTimeBet(); // Ejemplo: "VV-01:01-02:00"

            userBetsByHour.putIfAbsent(key, new ArrayList<>());
            userBetsByHour.get(key).add(user.getTimesBet());
        }

        // Sumar todas las apuestas por franja horaria
        Map<String, Integer> totalBetsByHour = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : userBetsByHour.entrySet()) {
            int sum = entry.getValue().stream().mapToInt(Integer::intValue).sum();
            totalBetsByHour.put(entry.getKey(), sum);
        }

        // Imprimir apuestas totales por franja horaria
        for (Map.Entry<String, Integer> entry : totalBetsByHour.entrySet()) {
            System.out.println("Apuestas de " + entry.getKey() + ": " + entry.getValue());
        }

        // Agrupar las sumas de apuestas por usuario (ignorando la franja horaria)
        Map<String, List<Integer>> userBetsTotal = new HashMap<>();
        for (Map.Entry<String, Integer> entry : totalBetsByHour.entrySet()) {
            String username = entry.getKey().split("-")[0]; // Obtener solo el nombre de usuario

            userBetsTotal.putIfAbsent(username, new ArrayList<>());
            userBetsTotal.get(username).add(entry.getValue());
        }

        // Calcular la media de apuestas por usuario
        Map<String, Integer> userAverages = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : userBetsTotal.entrySet()) {
            String username = entry.getKey();
            int average = Statistics.calculateAverage(entry.getValue()); // Calcula la media
            userAverages.put(username, average);
            System.out.println("[MODE HOUR] MEDIA de " + username + " -> " + average);
        }

        return userAverages;
    }

    public enum ModeEnum {
        DAY,
        HOUR,
        DAYHOUR
    }

    public List<UserDTO> convertUserEntitiesToDTOs(List<UserEntity> userEntities, List<UserEntity> allUsersEntity, ModeEnum mode) {
        if (userEntities.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Integer> userAverage = new HashMap<String, Integer>();

        if (mode == ModeEnum.DAY) {
            userAverage = calculateUserAveragesByDay(allUsersEntity);
        }
        if (mode == ModeEnum.HOUR) {
            userAverage = calculateUserAveragesByHour(allUsersEntity);
        }
        if (mode == ModeEnum.DAYHOUR) {
            userAverage = calculateUserAveragesByDayHour(allUsersEntity);
        }

        List<UserDTO> userDTOs = new ArrayList<>();
        List<Double> allBets = new ArrayList<>();
        List<Double> allSuccessRates = new ArrayList<>();

        for (UserEntity user : userEntities) {
            String username = user.getUsername();
            int userBets = user.getTimesBet();
            Integer averageBets = userAverage.get(username);

            if (averageBets != null && averageBets > 0) {
                double percentReliable = ((double) userBets / averageBets) * 100.0;

                UserDTO userDTO = new UserDTO(username, percentReliable, userBets, averageBets);
                userDTOs.add(userDTO);

                // Guardamos valores para calcular la mediana total
                allBets.add((double) userBets);
                allSuccessRates.add(percentReliable);

                // Log de depuración
                System.out.println("Usuario: " + username + ", Apuestas: " + userBets + ", Media: " + averageBets + ", PercentReliable: " + percentReliable);
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
