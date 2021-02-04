package local.gongule.tools.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Course implements Serializable {

    public String name = "";
    public List<Integer> dayIndexes = new ArrayList(0);

    public Course(String name) {
        this.name = name;
    }

}
