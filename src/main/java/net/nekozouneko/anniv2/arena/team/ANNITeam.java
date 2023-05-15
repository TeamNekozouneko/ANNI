package net.nekozouneko.anniv2.arena.team;

import com.google.gson.annotations.SerializedName;
import net.nekozouneko.anniv2.ANNIPlugin;

public enum ANNITeam {

    @SerializedName("red")
    RED("team.red.display", "team.red.prefix"),
    @SerializedName("blue")
    BLUE("team.blue.display", "team.blue.prefix"),
    @SerializedName("green")
    GREEN("team.green.display", "team.green.prefix"),
    @SerializedName("yellow")
    YELLOW("team.yellow.display", "team.yellow.prefix");

    private final String name;
    private final String prefix;

    private ANNITeam(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    public String getTeamName() {
        return ANNIPlugin.getInstance().getMessageManager().build(name);
    }

    public String getTeamPrefix() {
        return ANNIPlugin.getInstance().getMessageManager().build(prefix);
    }

}
