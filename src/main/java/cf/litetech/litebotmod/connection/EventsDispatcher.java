package cf.litetech.litebotmod.connection;

import net.minecraft.block.Block;
import net.minecraft.server.network.ServerPlayerEntity;

public class EventsDispatcher {
    public void onServerStart() {
        new EventBuilder(EventBuilder.Events.EVENT).setName("on_server_start").dispatchEvent();
    }

    public void onServerStop() {
        new EventBuilder(EventBuilder.Events.EVENT).setName("on_server_stop").dispatchEvent();
    }

    public void onTick() {
        new EventBuilder(EventBuilder.Events.EVENT).setName("on_tick").dispatchEvent();
    }

    public void onPlayerJoin(ServerPlayerEntity player) {
        new EventBuilder(EventBuilder.Events.EVENT)
                .setName("on_player_join")
                .setPlayer(player)
                .dispatchEvent();
    }

    public void onPlayerLeave(ServerPlayerEntity player) {
        new EventBuilder(EventBuilder.Events.EVENT)
                .setName("on_player_leave")
                .setPlayer(player)
                .dispatchEvent();
    }

    public void onPlayerDeath(ServerPlayerEntity player) {
        new EventBuilder(EventBuilder.Events.EVENT)
                .setName("on_player_death")
                .setPlayer(player)
                .dispatchEvent();
    }

    public void onPlayerBreaksBlock(ServerPlayerEntity player, Block block) {
        new EventBuilder(EventBuilder.Events.EVENT)
                .setName("on_player_breaks_block")
                .setPlayer(player)
                .addArg("block", block.toString())
                .dispatchEvent();
    }

    public void onMessage(ServerPlayerEntity sender, String message) {
        new EventBuilder(EventBuilder.Events.EVENT)
                .setName("on_message")
                .setPlayer(sender)
                .addArg("message", message)
                .dispatchEvent();
    }

}
