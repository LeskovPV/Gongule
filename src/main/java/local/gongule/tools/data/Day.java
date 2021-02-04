package local.gongule.tools.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.time.*;
import java.util.List;

public class Day implements Serializable {

    public static class Event implements Serializable {
        public LocalTime time;
        public String name;
        public int gongIndex;
        public Event(LocalTime time, String name, int gongIndex) {
            this.time = time;
            this.name = name;
            this.gongIndex = gongIndex;
        }
    }

    public String name = "";
    public List<Event> events = new ArrayList(0);

    public Day(String name) {
        this.name = name;
    }

}
