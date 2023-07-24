package mine.plugins.lunar.buildablefactions.cmds.arg;

public abstract class NoFactionOnlyArg extends FactionOnlinePlayerArg {

    public NoFactionOnlyArg(String name) {
        this(name, null);
    }

    public NoFactionOnlyArg(String name, String[] argsInfo) {
        this(name, argsInfo, null);
    }

    public NoFactionOnlyArg(String name, String[] argsInfo, String permission) {
        super(name, argsInfo, permission);
    }

}
