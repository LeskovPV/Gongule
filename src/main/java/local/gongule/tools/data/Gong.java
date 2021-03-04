package local.gongule.tools.data;

import local.gongule.utils.logging.Loggible;

import java.io.Serializable;

public class Gong implements Serializable, Loggible {

    public String name = "";
    public int amount = 1;

    public Gong() {
    }

    public Gong(String name) {
        this.name = name;
    }

    public Gong(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }

}
