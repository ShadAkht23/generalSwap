package com.shard.generalswap;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.CreateGroupEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VoiceChatPlugin implements VoicechatPlugin {

    private final SwapPlugin plugin;

    public VoiceChatPlugin(SwapPlugin plugin) {
        this.plugin = plugin; // reference to your main plugin
    }
    @Override
    public String getPluginId() {
        return "generalswap";
    }

    @Override
    public void initialize(VoicechatApi api) {
        plugin.setVoicechatServerApi(api); // optionally give main plugin access too
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);

        registration.registerEvent(CreateGroupEvent.class, e -> {
            System.out.println("uiser created a gorup");
        });
    }

    public void onMicrophone(MicrophonePacketEvent event) {
        //System.out.println("d");
        if (!plugin.getGameManager().gameStarted())
            return;
        if (event.getSenderConnection() == null) {
            return;
        }
        // Cast the generic player object of the voice chat API to an actual bukkit player
        // This object should always be a bukkit player object on bukkit based servers
        if (!(event.getSenderConnection().getPlayer().getPlayer() instanceof Player player)) {
            return;
        }
        event.cancel();
        VoicechatServerApi api = event.getVoicechat();

        if (plugin.getGameManager().isSwappedOut(player.getUniqueId())) {
            return;
        }

        // Iterating over every player on the server
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            // Don't send the audio to the player that is broadcasting
            if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            VoicechatConnection connection = api.getConnectionOf(onlinePlayer.getUniqueId());
            // Check if the player is actually connected to the voice chat
            if (connection == null) {
                continue;
            }
            // check they not in a box
            if (!plugin.getGameManager().isSwappedOut(onlinePlayer.getUniqueId())) {
                // Send a static audio packet of the microphone data to the connection of each player
                api.sendStaticSoundPacketTo(connection, event.getPacket().toStaticSoundPacket());
            }
        }
    }

}
