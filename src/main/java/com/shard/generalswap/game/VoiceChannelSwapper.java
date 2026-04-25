package com.shard.generalswap.game;

import com.shard.generalswap.SwapPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class VoiceChannelSwapper {


    public static void swapIn(UUID player) {
        Bukkit.getScheduler().runTaskAsynchronously(SwapPlugin.get(), () -> {
            movePlayer(player, "IN");
        });
    }

    public static void swapOut(UUID player) {
        Bukkit.getScheduler().runTaskAsynchronously(SwapPlugin.get(), () -> {
            movePlayer(player, "OUT");
        });
    }

    private static void movePlayer(UUID player, String channelId) {
        try {
            Player play = Bukkit.getPlayer(player);
            System.out.println("Trying to move player: " + (play != null ? play.getName() : player.toString()) + " to channel " + channelId);
            URL url = new URL("http://localhost:8080/move");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // 🔥 ADD THESE
            conn.setConnectTimeout(2000); // 2s max to connect
            conn.setReadTimeout(2000);    // 2s max to wait for response
            conn.setRequestProperty("Content-Type", "application/json");

            String json = """
                    {
                        "uuid":"%s",
                        "channel":"%s"
                    }
                    """.formatted(player.toString(), channelId);

            conn.getOutputStream().write(json.getBytes());

            int code = conn.getResponseCode();

            InputStream stream = (code == 200)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String response = new String(stream.readAllBytes());
            System.out.println("Response: " + response);


        } catch (ConnectException e) {
            System.err.println("HHTP Connection Refused");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}