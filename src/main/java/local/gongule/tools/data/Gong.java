package local.gongule.tools.data;

import local.gongule.tools.Log;

import java.io.Serializable;

public class Gong implements Serializable {

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

    public void play() {
        Log.printInfo("Paying '" + name + "'");
    }

}
