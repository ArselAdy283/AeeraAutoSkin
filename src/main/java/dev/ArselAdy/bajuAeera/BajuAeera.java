package dev.ArselAdy.bajuAeera;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BajuAeera extends JavaPlugin implements Listener {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final String WEB_URL = "https://upload-skin-aeera.vercel.app";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("BajuAeera_Plugin Aktif");
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (!item.hasItemMeta()) return;

        List<Component> lore = item.getItemMeta().lore();
        if (lore == null) return;

        List<String> loreLines = lore.stream()
                .map(line -> PlainTextComponentSerializer.plainText().serialize(line).toLowerCase().trim())
                .toList();

        fetchAndSetSkin(event.getPlayer(), loreLines);
    }

    private void sendUploadMessage(Player player, String jenis) {
        Component message = Component.text("Kamu belum memiliki " + jenis + "! Upload skinmu di ", NamedTextColor.RED)
                .append(Component.text(WEB_URL, NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl(WEB_URL)));
        player.sendMessage(message);
    }

    private void fetchAndSetSkin(Player player, List<String> loreLines) {
        String apiUrl = "https://upload-skin-aeera.vercel.app/api/skript-bajuaeera?nick=" + player.getName();

        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    Bukkit.getScheduler().runTask(this, () ->
                            player.sendMessage(Component.text("Gagal terhubung ke API Aeera (status " + response.statusCode() + ").", NamedTextColor.RED))
                    );
                    return;
                }

                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

                List<String> availableTypes = new ArrayList<>();
                if (json.has("available_types") && json.get("available_types").isJsonArray()) {
                    JsonArray typesArray = json.getAsJsonArray("available_types");
                    typesArray.forEach(el -> availableTypes.add(el.getAsString().toLowerCase().trim()));
                }

                if (availableTypes.isEmpty()) {
                    Bukkit.getScheduler().runTask(this, () ->
                            player.sendMessage(Component.text("API Aeera tidak mengirim daftar jenis baju.", NamedTextColor.RED))
                    );
                    return;
                }

                String matchedLore = null;
                for (String loreLine : loreLines) {
                    if (availableTypes.contains(loreLine)) {
                        matchedLore = loreLine;
                        break;
                    }
                }

                if (matchedLore == null) return;

                String finalMatchedLore = matchedLore;

                String originalKey = null;
                for (String key : json.keySet()) {
                    if (key.toLowerCase().trim().equals(finalMatchedLore)) {
                        originalKey = key;
                        break;
                    }
                }

                if (originalKey == null) {
                    String finalLore = finalMatchedLore;
                    Bukkit.getScheduler().runTask(this, () -> {
                        // Khusus baju pribadi → clear skin
                        if (finalLore.equals("baju pribadi")) {
                            player.performCommand("skin clear");
                        } else {
                            sendUploadMessage(player, finalLore);
                        }
                    });
                    return;
                }

                JsonObject skinData = json.getAsJsonObject(originalKey);

                String skinUrl = "";
                String lengan = "classic";

                if (skinData.has("skin") && !skinData.get("skin").isJsonNull()) {
                    skinUrl = skinData.get("skin").getAsString().trim();
                }

                if (skinData.has("lengan") && !skinData.get("lengan").isJsonNull()) {
                    String temp = skinData.get("lengan").getAsString().toLowerCase().trim();
                    if (temp.equals("slim") || temp.equals("classic")) lengan = temp;
                }

                String finalSkinUrl = skinUrl;
                String finalLengan = lengan;
                String finalOriginalKey = originalKey;

                Bukkit.getScheduler().runTask(this, () -> {
                    if (!finalSkinUrl.isEmpty()) {
                        player.performCommand("skin url " + finalSkinUrl + " " + finalLengan);
                    } else {
                        if (finalMatchedLore.equals("baju pribadi")) {
                            player.performCommand("skin clear");
                        } else {
                            sendUploadMessage(player, finalOriginalKey);
                        }
                    }
                });

            } catch (Exception e) {
                Bukkit.getScheduler().runTask(this, () ->
                        player.sendMessage(Component.text("Gagal terhubung ke API Aeera.", NamedTextColor.RED))
                );
                e.printStackTrace();
            }
        });
    }
}