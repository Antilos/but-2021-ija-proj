package ija.ija2020.proj.calendar;

import java.util.function.Function;

public abstract class EventAction<T, R> {
    Function<T, R> action;


}
