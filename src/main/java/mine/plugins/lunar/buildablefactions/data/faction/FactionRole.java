package mine.plugins.lunar.buildablefactions.data.faction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
public class FactionRole implements Serializable {

    public final UUID id = UUID.randomUUID();

    @Getter private String name;
    @Getter private ChatColor color;
}
