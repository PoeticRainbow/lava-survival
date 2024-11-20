package poeticrainbow.lavasurvival.util;

import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;

import java.util.List;

public interface ActivityManager {
    List<GameRuleType> getDeniedActivities();
    List<GameRuleType> getAllowedActivities();

    default void setupDeniedActivities(GameActivity activity) {
        getAllowedActivities().forEach(activity::deny);
    }

    default void setupAllowedActivities(GameActivity activity) {
        getDeniedActivities().forEach(activity::allow);
    }
}
