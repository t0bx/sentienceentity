package de.t0bx.sentienceEntity.utils.item;

import de.t0bx.sentienceEntity.SentienceEntity;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class ItemProvider {
    private final Map<String, HeadData> headCache = new HashMap<>();

    private final ItemStack itemStack;

    public ItemProvider(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemProvider(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    public ItemProvider(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
    }

    public static ItemProvider createCustomSkull(String skullTexture) {
        ItemProvider builder = new ItemProvider(Material.PLAYER_HEAD);
        return builder.setSkullTexture(skullTexture);
    }

    public static ItemProvider createPlayerSkull(String playerName) {
        ItemProvider builder = new ItemProvider(Material.PLAYER_HEAD);
        return builder.setSkullOwner(playerName);
    }

    public ItemProvider setName(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.customName(MiniMessage.miniMessage().deserialize(name));
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider setLore(String... lore) {
        return setLore(Arrays.asList(lore));
    }

    public ItemProvider setLore(List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider setLoreComponent(String lore) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.lore(List.of(MiniMessage.miniMessage().deserialize(lore)));
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider addLore(String... lines) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.addAll(Arrays.asList(lines));
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemProvider setUnbreakable(boolean unbreakable) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider addEnchantment(Enchantment enchantment, int level) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider addEnchantments(Map<Enchantment, Integer> enchantments) {
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            addEnchantment(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public ItemProvider addStoredEnchantment(Enchantment enchantment, int level) {
        if (itemStack.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemStack.getItemMeta();
            if (meta != null) {
                meta.addStoredEnchant(enchantment, level, true);
                itemStack.setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemProvider addItemFlags(ItemFlag... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider removeItemFlags(ItemFlag... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.removeItemFlags(flags);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider setDurability(int durability) {
        if (durability >= 0) {
            itemStack.setDurability((short) durability);
        }
        return this;
    }

    public ItemProvider setLeatherArmorColor(Color color) {
        if (itemStack.getItemMeta() instanceof LeatherArmorMeta) {
            LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
            meta.setColor(color);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider setSkullTexture(String textureUrl) {
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            try {
                SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                if (skullMeta != null) {
                    PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
                    PlayerTextures textures = profile.getTextures();

                    if (textureUrl.startsWith("minecraft:")) {
                        textureUrl = textureUrl.substring("minecraft:".length());
                    }

                    URL url = new URL(textureUrl);
                    textures.setSkin(url);
                    profile.setTextures(textures);

                    skullMeta.setOwnerProfile(profile);
                    itemStack.setItemMeta(skullMeta);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public ItemProvider setSkullOwner(String playerName) {
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            if (skullMeta != null) {
                com.destroystokyo.paper.profile.PlayerProfile profile = Bukkit.createProfile(playerName);
                Bukkit.getServer().getScheduler().runTaskAsynchronously(
                        SentienceEntity.getInstance(), () -> profile.complete()
                );
                skullMeta.setOwnerProfile(profile);
                itemStack.setItemMeta(skullMeta);
            }
        }
        return this;
    }

    public ItemProvider setPotionType(PotionType potionType, boolean extended, boolean upgraded) {
        if (itemStack.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
            meta.setBasePotionType(potionType);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider addCustomPotionEffect(PotionEffect effect) {
        if (itemStack.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
            meta.addCustomEffect(effect, true);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider addCustomPotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles) {
        return addCustomPotionEffect(new PotionEffect(type, duration, amplifier, ambient, particles));
    }

    public ItemProvider setPotionColor(Color color) {
        if (itemStack.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
            meta.setColor(color);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider addAttributeModifier(Attribute attribute, AttributeModifier modifier) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addAttributeModifier(attribute, modifier);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider setPersistentData(String pluginName, String key, String value) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            NamespacedKey namespacedKey = new NamespacedKey(pluginName.toLowerCase(), key.toLowerCase());
            meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value.toLowerCase());
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider setPersistentDataInt(String pluginName, String key, int value) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            NamespacedKey namespacedKey = new NamespacedKey(pluginName.toLowerCase(), key);
            meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, value);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider setCustomModelData(int customModelData) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider clone() {
        return new ItemProvider(itemStack.clone());
    }

    public ItemProvider meta(Consumer<ItemMeta> metaConsumer) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            metaConsumer.accept(meta);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider setBookMeta(String title, String author) {
        if (itemStack.getItemMeta() instanceof BookMeta) {
            BookMeta meta = (BookMeta) itemStack.getItemMeta();
            meta.setTitle(title);
            meta.setAuthor(author);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemProvider addBookPages(Component... pages) {
        if (itemStack.getItemMeta() instanceof BookMeta) {
            BookMeta meta = (BookMeta) itemStack.getItemMeta();
            meta.addPages(pages);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemStack build() {
        return itemStack.clone();
    }

    public ItemStack toItemStack() {
        return build();
    }

    @Getter
    private static class HeadData {
        private final PlayerProfile profile;
        private final PlayerTextures textures;

        public HeadData(PlayerProfile profile, PlayerTextures textures) {
            this.profile = profile;
            this.textures = textures;
        }
    }
}