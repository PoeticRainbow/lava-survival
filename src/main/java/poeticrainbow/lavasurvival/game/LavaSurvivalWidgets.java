package poeticrainbow.lavasurvival.game;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import poeticrainbow.lavasurvival.game.phases.LavaSurvivalActive;
import xyz.nucleoid.plasmid.game.common.widget.BossBarWidget;
import xyz.nucleoid.plasmid.game.common.widget.SidebarWidget;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LavaSurvivalWidgets {
    private static SidebarWidget sidebar;
    private static BossBarWidget bossbar;
    private static LavaSurvivalActive game;

    public LavaSurvivalWidgets(LavaSurvivalActive g) {
        game = g;
        sidebar = new SidebarWidget(Text.translatable("gameType.lavasurvival.lavasurvival").formatted(Formatting.BOLD, Formatting.GOLD));
        sidebar.setUpdateRate(20);
        bossbar = new BossBarWidget(Text.translatable("gameType.lavasurvival.lavasurvival").formatted(Formatting.BOLD, Formatting.GOLD));
        bossbar.setProgress(0.0f);
    }

    public void updateWidgets(HashMap<UUID, Integer> playerScores) {
        if (playerScores == null) {
            return;
        }

        sidebar.clearLines();

        sidebar.addLines(Text.literal(""));
        sidebar.addLines(Text.translatable("sidebar.lavasurvival.players_left", playerScores.size()));
        sidebar.addLines(Text.literal(""));

        var server = game.getGameSpace().getServer();
        for (Map.Entry<UUID, Integer> entry : playerScores.entrySet()) {
            var uuid = entry.getKey();
            var score = entry.getValue();

            var player = server.getPlayerManager().getPlayer(uuid);

//            sidebar.addLines(Text.translatable("sidebar.lavasurvival.placement", player.getName(), score).formatted(Formatting.RED));
        }

        var style = Style.EMPTY.withColor(Colors.RED).withBold(true);
        if (game.getTimeLeft() > 0) {
            bossbar.setTitle(Text.translatable("bossbar.lavasurvival.title", game.getTimeLeft()).setStyle(style));
        } else {
            bossbar.setTitle(Text.translatable("message.lavasurvival.win"));
        }
        bossbar.setStyle(BossBar.Color.RED, BossBar.Style.PROGRESS);
        bossbar.setProgress(game.getGameProgress());

    }

    public void addPlayer(ServerPlayerEntity player) {
        sidebar.addPlayer(player);
        bossbar.addPlayer(player);
    }

    public void removePlayer(ServerPlayerEntity player) {
        sidebar.removePlayer(player);
        bossbar.removePlayer(player);
    }

    public void destroy() {
        bossbar.close();
        sidebar.close();
    }
}
