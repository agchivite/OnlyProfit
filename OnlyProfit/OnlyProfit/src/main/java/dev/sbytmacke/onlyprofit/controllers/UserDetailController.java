package dev.sbytmacke.onlyprofit.controllers;

import dev.sbytmacke.onlyprofit.dto.UserDTO;
import dev.sbytmacke.onlyprofit.models.UserEntity;
import dev.sbytmacke.onlyprofit.utils.DayHourComparator;
import dev.sbytmacke.onlyprofit.utils.GlobalStats;
import dev.sbytmacke.onlyprofit.viewmodel.UserViewModel;
import dev.sbytmacke.onlyprofit.utils.Statistics;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

public class UserDetailController {

    private int rowIndex = 0;
    private UserViewModel userViewModel;

    @FXML
    private GridPane gridPane;
    @FXML
    private Label usernameLabel;

    private static Map<Integer, List<Double>> getMapSuccessByDay(List<UserEntity> bets) {
        Map<Integer, List<Double>> mapSuccessRateByDay = new HashMap<>();

        for (UserEntity bet : bets) {
            int dayOfWeek = bet.getDateBet().getDayOfWeek().getValue();

            // Contar aciertos y apuestas por día, el índice 0 es aciertos y el 1 es el total de apuestas
            mapSuccessRateByDay.putIfAbsent(dayOfWeek, new ArrayList<>(Arrays.asList(0.0, 0.0)));
            double successRate = Boolean.TRUE.equals(bet.getReliable()) ? 1.0 : 0.0;
            List<Double> successAndBets = mapSuccessRateByDay.get(dayOfWeek);
            successAndBets.set(0, successAndBets.get(0) + successRate);
            successAndBets.set(1, successAndBets.get(1) + 1);
            mapSuccessRateByDay.put(dayOfWeek, successAndBets);
        }

        return mapSuccessRateByDay;
    }

    private Map<String, List<Double>> getMapSuccessByHour(List<UserEntity> bets) {
        Map<String, List<Double>> mapSuccessRateByHour = new HashMap<>();

        for (UserEntity bet : bets) {
            String betHour = bet.getTimeBet();

            // Contar aciertos y apuestas por hora, el índice 0 es aciertos y el 1 es el total de apuestas
            mapSuccessRateByHour.putIfAbsent(betHour, new ArrayList<>(Arrays.asList(0.0, 0.0)));
            double successRate = Boolean.TRUE.equals(bet.getReliable()) ? 1.0 : 0.0;
            List<Double> successAndBets = mapSuccessRateByHour.get(betHour);
            successAndBets.set(0, successAndBets.get(0) + successRate);
            successAndBets.set(1, successAndBets.get(1) + 1);
            mapSuccessRateByHour.put(betHour, successAndBets);
        }

        return mapSuccessRateByHour;
    }

    public void init(UserViewModel userViewModel, UserDTO user) {
        this.userViewModel = userViewModel;

        usernameLabel.setText(user.getUsername());

        List<UserEntity> listAllBetsOnlyByOneUser = userViewModel.getAllBetsByUser(user.getUsername());
        setBest(listAllBetsOnlyByOneUser);

        // ======= CAMBIAR
        /*int totalBets = listAllBetsOnlyByOneUser.size();
        setBestDay(listAllBetsOnlyByOneUser, totalBets);
        setBestHour(listAllBetsOnlyByOneUser, totalBets);
        setBestDayHour(listAllBetsOnlyByOneUser, totalBets);*/
        // =======
    }

    public Map<String, Integer> calculateMedianUserEachDayTime(List<UserEntity> listAllBetsOnlyByOneUser) {
        Map<String, List<Integer>> userBets = new HashMap<>();

        // Agrupar TODAS las apuestas de del usuario sin importar la franja horaria
        for (UserEntity user : listAllBetsOnlyByOneUser) {
            String username = user.getUsername();
            userBets.putIfAbsent(username, new ArrayList<>());
            userBets.get(username).add(user.getTimesBet());
        }

        // Calcular la mediana de apuestas de cada usuario
        Map<String, Integer> userMedians = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : userBets.entrySet()) {
            String username = entry.getKey();

            //Integer median = calculateMedian(entry.getValue());
            Integer average = Statistics.calculateAverage(entry.getValue());

            userMedians.put(username, average);

            // Log para depuración
            //System.out.println("MEDIANA de " + username + " -> " + median);
            System.out.println("[UserDetailController] MEDIA de " + username + " -> " + average);
        }

        return userMedians;
    }

    public void setBest(List<UserEntity> allBetsOneUser) {
        Map<String, Integer> userMedians = calculateMedianUserEachDayTime(allBetsOneUser);

        // Buscar todas las apuestas de mi usuario, coger el primero de useerMedians
        String userToSend = userMedians.keySet().iterator().next();
        Integer userMedian = userMedians.get(userToSend);

        List<UserEntity> filteredUserEntities = allBetsOneUser.stream()
                .filter(user -> user.getUsername().equals(userToSend))
                .collect(Collectors.toList());

        // Con filteredUserEntities tengo todas las apuestas de mi usuario y ahora buscamos donde haya mñas apuestas

        String bestDayHour = "";
        Integer bestTimesBet = userMedian;
        for (UserEntity filteredUserEntity : filteredUserEntities)  {
            if (filteredUserEntity.getTimesBet() > bestTimesBet) {
                bestTimesBet = filteredUserEntity.getTimesBet();
                bestDayHour = filteredUserEntity.getDateBet().getDayOfWeek() + " " + filteredUserEntity.getTimeBet();
            }
        }

        // Ahora comprobamos si tiene otros dias iguales
        List<String> bestDays = new ArrayList<>();
        for (UserEntity filteredUserEntity : filteredUserEntities)  {
            if (filteredUserEntity.getTimesBet() == bestTimesBet) {
                bestDays.add(filteredUserEntity.getDateBet().getDayOfWeek() + " " + filteredUserEntity.getTimeBet());
            }
        }

        Label correspondenceLabel = new Label("Mejor Día y Hora:");
        Insets labelMargins = new Insets(0, 0, 0, 15);
        correspondenceLabel.setPadding(labelMargins);
        gridPane.add(correspondenceLabel, 0, rowIndex);

        for (String hourDay : bestDays) {
            Label hourDayLabel = new Label(hourDay);
            gridPane.add(hourDayLabel, 1, rowIndex);
            rowIndex++;
        }
    }

    private void setBestDayHour(List<UserEntity> listAllBetsOnlyByOneUser, int totalBets) {
        int uniqueHoursDayWithBets = countUniqueHoursDayWithBets(listAllBetsOnlyByOneUser);
        int averageBetsByOneUser = uniqueHoursDayWithBets > 0 ? Math.round((float) totalBets / uniqueHoursDayWithBets) : 0;
        List<String> bestHourDays = getBestDayHour(listAllBetsOnlyByOneUser, averageBetsByOneUser);
        bestHourDays.sort(new DayHourComparator());

        Label correspondenceLabel = new Label("Mejor/es Día y Hora:");
        Insets labelMargins = new Insets(0, 0, 0, 15);
        correspondenceLabel.setPadding(labelMargins);
        gridPane.add(correspondenceLabel, 0, rowIndex);

        for (String hourDay : bestHourDays) {
            Label hourDayLabel = new Label(hourDay);
            Double percent = Double.valueOf(hourDay.split(" %")[0].trim());
            if (percent >= GlobalStats.goodAverageAllUsersSuccessRate) {
                hourDayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #79ba0f");
            } else if (percent < GlobalStats.averageSuccessRate) {
                hourDayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e3b007");
            }
            gridPane.add(hourDayLabel, 1, rowIndex);
            rowIndex++;
        }
    }

    private void setBestHour(List<UserEntity> listAllBetsOnlyByOneUser, int totalBets) {
        int uniqueHoursWithBets = countUniqueHoursWithBets(listAllBetsOnlyByOneUser);
        int averageBetsPerHourByOneUser = uniqueHoursWithBets > 0 ? Math.round((float) totalBets / uniqueHoursWithBets) : 0;
        List<String> bestHours = getBestHour(listAllBetsOnlyByOneUser, averageBetsPerHourByOneUser);

        Label correspondenceLabel = new Label("Mejor/es Hora/s:");
        Insets labelMargins = new Insets(0, 0, 0, 15);
        correspondenceLabel.setPadding(labelMargins);
        gridPane.add(correspondenceLabel, 0, rowIndex);

        if (bestHours.isEmpty()) {
            Label noDataLabel = new Label("No tiene...");
            gridPane.add(noDataLabel, 1, rowIndex);
            rowIndex++;

            // Fila extra
            Label emptyLabel = new Label("");
            gridPane.add(emptyLabel, 0, rowIndex++);

            return;
        }

        // Etiquetas de hora en la misma columna y última fila
        for (String hour : bestHours) {
            Label hourLabel = new Label(hour);
            Double percent = Double.valueOf(hour.split(" %")[0].trim());
            if (percent >= GlobalStats.goodAverageAllUsersSuccessRate) {
                hourLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #79ba0f");
            } else if (percent < GlobalStats.averageSuccessRate) {
                hourLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e3b007");
            }
            gridPane.add(hourLabel, 1, rowIndex);
            rowIndex++;
        }

        // Fila extra
        Label emptyLabel = new Label("");
        gridPane.add(emptyLabel, 0, rowIndex++);

        //bestHour.setText(String.join("\n", bestHourNumber));
    }

    private void setBestDay(List<UserEntity> listAllBetsOnlyByOneUser, int totalBets) {
        int uniqueDaysWithBets = countUniqueDaysOfWeekWithBets(listAllBetsOnlyByOneUser);
        int averageBetsPerDayByOneUser = uniqueDaysWithBets > 0 ? Math.round((float) totalBets / uniqueDaysWithBets) : 0;
        List<String> bestDays = getBestDay(listAllBetsOnlyByOneUser, averageBetsPerDayByOneUser);

        Label correspondenceLabel = new Label("Mejor/es Día/s:");
        Insets labelMargins = new Insets(0, 0, 0, 15);
        correspondenceLabel.setPadding(labelMargins);
        gridPane.add(correspondenceLabel, 0, 0);

        if (bestDays.isEmpty()) {
            Label noDataLabel = new Label("No tiene...");
            gridPane.add(noDataLabel, 1, rowIndex);
            rowIndex++;

            // Fila extra
            Label emptyLabel = new Label("");
            gridPane.add(emptyLabel, 0, rowIndex++);

            return;
        }


        // Agregar las etiquetas al GridPane
        for (String day : bestDays) {
            Label dayLabel = new Label(day);

            Double percent = Double.valueOf(day.split(" %")[0].trim());
            if (percent >= GlobalStats.goodAverageAllUsersSuccessRate) {
                dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #79ba0f");
            } else if (percent < GlobalStats.averageSuccessRate) {
                dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e3b007");
            }

            gridPane.add(dayLabel, 1, rowIndex);
            rowIndex++;
        }

        // Fila extra
        Label emptyLabel = new Label("");
        gridPane.add(emptyLabel, 0, rowIndex++);

        //bestDay.setText(String.join("\n", bestDays));
    }

    private List<String> getBestDayHour(List<UserEntity> listAllBetsOnlyByOneUser, int averageBetsByOneUser) {
        Map<String, List<Double>> mapSuccessRateByDayHour = getMapSuccessByDayHour(listAllBetsOnlyByOneUser);

        // Filtramos los mejores días para ver si corresponden con el promedio de apuestas
        List<String> filteredDaysByAverageBetsPerDayHour = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : mapSuccessRateByDayHour.entrySet()) {
            String dayOfWeekAndHour = entry.getKey();
            List<Double> successAndBets = entry.getValue();
            double successRate = successAndBets.get(0) / successAndBets.get(1) * 100;
            double betsOnDayHour = successAndBets.get(1);

            System.out.println("dayOfWeekAndHour: " + dayOfWeekAndHour + ", successRate: " + successRate + ", betsOnDay: " + betsOnDayHour + ", averageBetsByOneUser: " + averageBetsByOneUser + ", averageAllUsersSuccessRate: " + GlobalStats.goodAverageAllUsersSuccessRate);
            if (betsOnDayHour >= averageBetsByOneUser && betsOnDayHour >= GlobalStats.medianTotalBets && successRate >= GlobalStats.goodAverageAllUsersSuccessRate) {
                successRate = Math.round(successRate * 100.0) / 100.0;
                String percentAndDayOfWeekAndHour = successRate + " % - " + dayOfWeekAndHour;
                filteredDaysByAverageBetsPerDayHour.add(percentAndDayOfWeekAndHour);
            }
        }

        System.out.println("Media de apuestas por usuario: " + averageBetsByOneUser + ", mediana de aciertos de todos los usuarios: " + GlobalStats.goodAverageAllUsersSuccessRate);
        System.out.println("Mediana general de total apuestas: " + GlobalStats.medianTotalBets);

        // TODO: pensar un mejor filtro cuando no haya ninguno, por ahora lo dejo con que el algoritmo considerar que ningun dato tiene valor para ser el mejor
        // Si no hay ninguno, escogemos los que tengan más aciertos, siempre que sea valioso comparando con el resto de usuarios
    /*    if (filteredDaysByAverageBetsPerDayHour.isEmpty()) {
            for (Map.Entry<String, List<Double>> entry : mapSuccessRateByDayHour.entrySet()) {
                String dayOfWeekAndHour = entry.getKey();
                List<Double> successAndBets = entry.getValue();
                double successRate = successAndBets.get(0) / successAndBets.get(1) * 100;

                if (successRate > averageAllUsersSuccessRate) {
                    filteredDaysByAverageBetsPerDayHour.add(dayOfWeekAndHour);
                }
            }
        }*/

        return filteredDaysByAverageBetsPerDayHour;
    }

    private Map<String, List<Double>> getMapSuccessByDayHour(List<UserEntity> listAllBetsOnlyByOneUser) {
        Map<String, List<Double>> mapSuccessRateByDayHour = new HashMap<>();

        for (UserEntity bet : listAllBetsOnlyByOneUser) {
            String betHour = bet.getTimeBet();
            DayOfWeek betDay = bet.getDateBet().getDayOfWeek();

            String betDayOfWeekSpanish = switch (betDay.toString()) {
                case "MONDAY" -> "LUNES";
                case "TUESDAY" -> "MARTES";
                case "WEDNESDAY" -> "MIÉRCOLES";
                case "THURSDAY" -> "JUEVES";
                case "FRIDAY" -> "VIERNES";
                case "SATURDAY" -> "SÁBADO";
                case "SUNDAY" -> "DOMINGO";
                default -> "ERROR";
            };
            String betHourAndDay = betDayOfWeekSpanish + " de " + betHour;

            // Contar aciertos y apuestas por hora, el índice 0 es aciertos y el 1 es el total de apuestas
            mapSuccessRateByDayHour.putIfAbsent(betHourAndDay, new ArrayList<>(Arrays.asList(0.0, 0.0)));
            double successRate = Boolean.TRUE.equals(bet.getReliable()) ? 1.0 : 0.0;
            List<Double> successAndBets = mapSuccessRateByDayHour.get(betHourAndDay);
            successAndBets.set(0, successAndBets.get(0) + successRate);
            successAndBets.set(1, successAndBets.get(1) + 1);
            mapSuccessRateByDayHour.put(betHourAndDay, successAndBets);
        }

        return mapSuccessRateByDayHour;
    }

    private int countUniqueHoursDayWithBets(List<UserEntity> listAllBetsOnlyByOneUser) {
        Set<String> uniqueHours = new HashSet<>();

        for (UserEntity bet : listAllBetsOnlyByOneUser) {
            DayOfWeek dayOfWeek = bet.getDateBet().getDayOfWeek();
            uniqueHours.add(dayOfWeek + " " + bet.getTimeBet());
        }

        return uniqueHours.size();
    }


    private List<String> getBestDay(List<UserEntity> bets, int averageBetsPerDayByOneUser) {
        Map<Integer, List<Double>> mapSuccessRateByDay = getMapSuccessByDay(bets);

        // Filtramos los mejores días para ver si corresponden con el promedio de apuestas
        List<String> filteredDaysByAverageBetsPerDay = new ArrayList<>();
        for (Map.Entry<Integer, List<Double>> entry : mapSuccessRateByDay.entrySet()) {
            int dayOfWeekNumber = entry.getKey();
            List<Double> successAndBets = entry.getValue();
            double successRate = successAndBets.get(0) / successAndBets.get(1) * 100;
            double betsOnDay = successAndBets.get(1);

            String betDayOfWeekSpanish = switch (DayOfWeek.of(dayOfWeekNumber).toString()) {
                case "MONDAY" -> "LUNES";
                case "TUESDAY" -> "MARTES";
                case "WEDNESDAY" -> "MIÉRCOLES";
                case "THURSDAY" -> "JUEVES";
                case "FRIDAY" -> "VIERNES";
                case "SATURDAY" -> "SÁBADO";
                case "SUNDAY" -> "DOMINGO";
                default -> "ERROR";
            };

            System.out.println("dayOfWeekNumber: " + betDayOfWeekSpanish + ", successRate: " + successRate + ", betsOnDay: " + betsOnDay + ", averageBetsByOneUser: " + averageBetsPerDayByOneUser + ", averageAllUsersSuccessRate: " + GlobalStats.goodAverageAllUsersSuccessRate);
            if (betsOnDay >= averageBetsPerDayByOneUser && betsOnDay >= GlobalStats.medianTotalBets && successRate >= GlobalStats.goodAverageAllUsersSuccessRate) {
                successRate = Math.round(successRate * 100.0) / 100.0;
                String percentAndDayOfWeek = successRate + " % - " + betDayOfWeekSpanish;
                filteredDaysByAverageBetsPerDay.add(percentAndDayOfWeek);
            }
            System.out.println("Media de apuestas por usuario: " + averageBetsPerDayByOneUser + ", mediana de aciertos de todos los usuarios: " + GlobalStats.goodAverageAllUsersSuccessRate);
            System.out.println("Mediana succesRate: " + GlobalStats.medianTotalBets);
        }

        // TODO: pensar un mejor filtro cuando no haya ninguno, por ahora lo dejo con que el algoritmo considerar que ningun dato tiene valor para ser el mejor
        // Si no hay ninguno, escogemos el que tengan más aciertos, siempre que sea valioso comparando con el resto de usuarios
  /*      if (filteredDaysByAverageBetsPerDay.isEmpty()) {
            for (Map.Entry<Integer, List<Double>> entry : mapSuccessRateByDay.entrySet()) {
                int dayOfWeekNumber = entry.getKey();
                List<Double> successAndBets = entry.getValue();
                double successRate = successAndBets.get(0) / successAndBets.get(1) * 100;

                if (successRate > averageAllUsersSuccessRate) {
                    filteredDaysByAverageBetsPerDay.add(dayOfWeekNumber);
                }
            }
        }*/

        return filteredDaysByAverageBetsPerDay;
    }

    private int countUniqueDaysOfWeekWithBets(List<UserEntity> bets) {
        Set<DayOfWeek> uniqueDays = new HashSet<>();

        for (UserEntity bet : bets) {
            DayOfWeek dayOfWeek = bet.getDateBet().getDayOfWeek();
            uniqueDays.add(dayOfWeek);
        }

        return uniqueDays.size();
    }

    private List<String> getBestHour(List<UserEntity> bets, int averageBetsPerHourByOneUser) {
        Map<String, List<Double>> mapSuccessRateByHour = getMapSuccessByHour(bets);

        // Filtramos los mejores días para ver si corresponden con el promedio de apuestas
        List<String> filteredHours = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : mapSuccessRateByHour.entrySet()) {
            String hourEntry = entry.getKey();
            List<Double> successAndBets = entry.getValue();
            double successRate = successAndBets.get(0) / successAndBets.get(1) * 100;
            double betsOnHour = successAndBets.get(1);

            if (betsOnHour >= averageBetsPerHourByOneUser && betsOnHour >= GlobalStats.medianTotalBets && successRate >= GlobalStats.goodAverageAllUsersSuccessRate) {
                successRate = Math.round(successRate * 100.0) / 100.0;
                String percentHour = successRate + " % - " + hourEntry;
                filteredHours.add(percentHour);
            }
        }

        // TODO: pensar un mejor filtro cuando no haya ninguno, por ahora lo dejo con que el algoritmo considerar que ningun dato tiene valor para ser el mejor
        // Si no hay ninguno, escogemos los que tengan más aciertos, siempre que sea valioso comparando con el resto de usuarios
  /*      if (filteredHours.isEmpty()) {
            for (Map.Entry<String, List<Double>> entry : mapSuccessRateByHour.entrySet()) {
                String hourEntry = entry.getKey();
                List<Double> successAndBets = entry.getValue();
                double successRate = successAndBets.get(0) / successAndBets.get(1) * 100;

                if (successRate > averageAllUsersSuccessRate) {
                    filteredHours.add(hourEntry);
                }
            }
        }*/

        return filteredHours;
    }

    private int countUniqueHoursWithBets(List<UserEntity> bets) {
        Set<String> uniqueHours = new HashSet<>();

        for (UserEntity bet : bets) {
            uniqueHours.add(bet.getTimeBet());
        }

        return uniqueHours.size();
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) usernameLabel.getScene().getWindow();
        stage.close();
    }
}
