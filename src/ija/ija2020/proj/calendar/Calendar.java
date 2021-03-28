package ija.ija2020.proj.calendar;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Calendar {

    PriorityQueue<Event> queue;

    public Calendar() {
        this.queue = new PriorityQueue<Event>(new EventComparator());
    }

    class EventComparator implements Comparator<Event> {
        @Override
        public int compare(Event o1, Event o2) {
            if(o1.getActivationTime().isBefore(o2.getActivationTime())){
                return 1;
            }else if(o1.getActivationTime().isAfter(o2.getActivationTime())){
                return -1;
            }else{ //identical activation time
                if(o1.getPriority() < o2.getPriority()){
                    return 1;
                }else if (o1.getPriority() > o2.getPriority()){
                    return -1;
                }else{
                    return 0;
                }
            }
        }
    }

    public Event getNextEvent(){
        return queue.poll();
    }

    public void insertEvent(Event e){
        queue.add(e);
    }
}
