package com.example.demo;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVUtilTest {

    @Test
    void convertirData(){
        List<Player> list = CsvUtilFile.getPlayers();
        assert list.size() == 18207;
    }

    @Test
    void filtrarPlayerMayoresA34() {
        List<Player> list = CsvUtilFile.getPlayers();
        Flux<Player> listFlux = Flux.fromStream(list.parallelStream()).cache();
        Mono<Map<String, Collection<Player>>> listFilter = listFlux
                .filter(player -> player.club.equals("Paris Saint-Germain") && player.age >= 34)
                .distinct()
                .collectMultimap(Player::getClub);

        System.out.println("Equipo: \n");
        listFilter.block().forEach((equipo, players) -> {
            System.out.println(equipo.toUpperCase(Locale.ROOT) + "\n");
            players.forEach(player -> {
                System.out.println("El Jugador es: " + player.name.toUpperCase(Locale.ROOT) + "\n"
                        + "Su edad es: " + player.age + " AÑOS"
                        + "\n" + "Su nacionalidad es: " + player.national.toUpperCase(Locale.ROOT) + "\n");
                assert player.club.equals("Paris Saint-Germain");
            });
        });
        assert listFilter.block().size() == 1;
    }


    @Test
    void filtrarRankingDeVictoriasPorNacionalidad() {
        List<Player> list = CsvUtilFile.getPlayers();
        Flux<Player> listFlux = Flux.fromStream(list.parallelStream()).cache();
        Mono<Map<String, Collection<Player>>> listFilter = listFlux
                .buffer(100)
                .flatMap(player1 -> listFlux
                        .filter(player2 -> player1.stream()
                                .anyMatch(nat -> nat.national.equals(player2.national))))
                .distinct()
                .sort((play, player) -> player.winners)
                .collectMultimap(Player::getNational);

        System.out.println("Por Nacionalidad: ");
        System.out.println(listFilter.block().size());
        listFilter.block().forEach((pais, players) -> {
            System.out.println("Pais: " + pais.toUpperCase(Locale.ROOT) + "\n" + "{");
            players.forEach(player -> {
                System.out.println("[ El Jugador es: " + player.name.toUpperCase(Locale.ROOT) + "\n" + " Este jugador tiene: " + player.winners +" VICTORIAS ]" + "\n");
            });
            System.out.println("}");
        });
    }



}
