package ija.ija2020.proj.calendar;

import java.time.LocalTime;
import java.util.function.Consumer;
import java.util.function.Function;

public class Event{
    private LocalTime aTime;
    private int priority;
    private Consumer<LocalTime> action;

    public Event(LocalTime aTime, int priority, Consumer<LocalTime> action) {
        this.aTime = aTime;
        this.priority = priority;
        this.action = action;
    }

    public LocalTime getActivationTime() {
        return aTime;
    }

    public int getPriority() {
        return priority;
    }

    public Consumer<LocalTime> getAction() {
        return action;
    }

    public void performAction(LocalTime t){
        this.action.accept(t);
    }
}
