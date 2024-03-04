package net.nekozouneko.anni.kit;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.team.ANNITeam;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum ANNIKit {

    ACROBAT(new AcrobatKit()),
    ASSAULT(new AssaultKit()),
    BOW(new BowKit()),
    DEFAULT(new DefaultKit()),
    DEFENSE(new DefenseKit()),
    MOCHI_MOCHI(new MochiMochiKit()),
    MINER(new MinerKit()),
    SCOUTER(new ScouterKit()),
    WORKER(new WorkerKit()),
    VAMPIRE(new VampireKit()),
    MUSASABI(new MusasabiKit());

    private static final Map<String, ANNIKit> ID_MAP = new HashMap<>();

    static {
        ID_MAP.put(ACROBAT.getKit().getId(), ACROBAT);
        ID_MAP.put(ASSAULT.getKit().getId(), ASSAULT);
        ID_MAP.put(BOW.getKit().getId(), BOW);
        ID_MAP.put(DEFAULT.getKit().getId(), DEFAULT);
        ID_MAP.put(DEFENSE.getKit().getId(), DEFENSE);
        ID_MAP.put(MOCHI_MOCHI.getKit().getId(), MOCHI_MOCHI);
        ID_MAP.put(MINER.getKit().getId(), MINER);
        ID_MAP.put(SCOUTER.getKit().getId(), SCOUTER);
        ID_MAP.put(WORKER.getKit().getId(), WORKER);
        ID_MAP.put(VAMPIRE.getKit().getId(), VAMPIRE);
        ID_MAP.put(MUSASABI.getKit().getId(), MUSASABI);
    }

    private final AbstractKit kit;

    private ANNIKit(AbstractKit kit) {
        this.kit = kit;
    }

    public AbstractKit getKit() {
        return kit;
    }

    public static ANNIKit getKitById(String id) {
        return ID_MAP.getOrDefault(id, DEFAULT);
    }

    public static AbstractKit getAbsKitOrCustomById(String id) {
        return ANNIPlugin.getInstance().getCustomKitManager().getKit(id) != null ? ANNIPlugin.getInstance().getCustomKitManager().getKit(id) : getKitById(id).getKit();
    }

    public static boolean isDefaultKit(AbstractKit kit) {
        return Arrays.stream(values())
                .map(ANNIKit::getKit)
                .anyMatch(kit::equals);
    }

    public static ANNIKit get(AbstractKit kit) {
        for (ANNIKit k : values()) {
            if (k.getKit().equals(kit)) return k;
        }
        return DEFAULT;
    }

    public static ItemStack[] teamColor(AbstractKit kit, ANNITeam team) {
        ItemStack[] arr = Arrays.copyOf(kit.getKitContents(), kit.getKitContents().length);
        for (ItemStack is : arr) {
            if (is == null) continue;

            if (is.getItemMeta() instanceof LeatherArmorMeta) {
                LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
                switch (team) {
                    case RED:
                        lam.setColor(Color.RED);
                        break;
                    case BLUE:
                        lam.setColor(Color.BLUE);
                        break;
                    case GREEN:
                        lam.setColor(Color.GREEN);
                        break;
                    case YELLOW:
                        lam.setColor(Color.YELLOW);
                        break;
                }
                is.setItemMeta(lam);
            }

            ItemMeta im = is.getItemMeta();
            PersistentDataContainer c = im.getPersistentDataContainer();

            NamespacedKey key = new NamespacedKey(ANNIPlugin.getInstance(), "kit-item");

            if (!c.has(key, PersistentDataType.INTEGER))
                c.set(key, PersistentDataType.INTEGER, 1);

            is.setItemMeta(im);
        }

        return arr;
    }

}
