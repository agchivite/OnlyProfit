package dev.sbytmacke.onlyprofit.models;

import java.time.LocalDate;


//  Martes 7am-8am -> 20 usuarios en esta franja han apostado100veces ->  da un 60% porque han acertado 60 veces
//  Con un ratio mínimo de 3 personas por franja (si no, no des resultado)
//  % de tíos han acertado más veces en esa franja (individualmente y en grupo)

public class UserEntity {
    // Dos casos de uso
    // 1. Que coincidan en el mismo día de la semana y franja horaria (este es más preciso y seguro al apostar)
    // 2. Que coincidan en la misma franja horaria solamente

    private final String username;
    private final LocalDate dateBet; // La fecha tenemos que hacer el cálculo para los que corresponden al mismo día de la semana
    private final String timeBet;
    private final Boolean reliable;
    private Integer timesBet;

    public UserEntity(String username, LocalDate dateBet, String timeBet, Boolean reliable, Integer timesBet) {
        this.username = username;
        this.dateBet = dateBet;
        this.timeBet = timeBet;
        this.reliable = reliable;
        this.timesBet = timesBet;
    }

    public String getUsername() {
        return username;
    }

    public LocalDate getDateBet() {
        return dateBet;
    }

    public String getTimeBet() {
        return timeBet;
    }

    public Boolean getReliable() {
        return reliable;
    }

    public Integer getTimesBet(){return  timesBet;}

    public  void setTimesBet(Integer timesBet  ){
        this.timesBet = timesBet;
    }
}
