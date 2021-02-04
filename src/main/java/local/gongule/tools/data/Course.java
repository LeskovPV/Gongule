package local.gongule.tools.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Course implements Serializable {

    public String name = "";
    public List<Day> schedule = new ArrayList(0);

}
