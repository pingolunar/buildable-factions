package mine.plugins.lunar.buildablefactions.cmds.faction;

import mine.plugins.lunar.buildablefactions.cmds.arg.FactionBannerArg;
import mine.plugins.lunar.buildablefactions.data.world.BannerState;

public class FactionUnclaimArg extends FactionBannerArg {

    public FactionUnclaimArg() {
        super(BannerState.UNCLAIM);
    }

}
