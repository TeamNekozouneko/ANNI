package net.nekozouneko.anni.vote;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VoteManager {

    private static final Map<String, Multimap<Object, OfflinePlayer>> vote = new HashMap<>();
    private static final Map<String, Set<Object>> choices = new HashMap<>();

    private VoteManager() { throw new ExceptionInInitializerError(); }

    public static void startVote(String id, Set<Object> choices) {
        Preconditions.checkArgument(id != null, "ID is null.");
        Preconditions.checkArgument(!isNowVoting(id), "Duplicate");

        vote.put(id, HashMultimap.create());
        VoteManager.choices.put(id, choices);
    }

    public static void vote(String id, OfflinePlayer player, Object obj) {
        Preconditions.checkArgument(id != null && player != null);
        if (choices.get(id) != null) {
            if (!choices.get(id).contains(obj))
                throw new IllegalArgumentException(obj + " is not contain choices");
        }

        new HashSet<>(vote.get(id).keySet()).forEach(obj1 -> vote.get(id).remove(obj1, player));
        vote.get(id).put(obj, player);
    }

    public static void updateChoices(String id, Set<Object> choices) {
        Preconditions.checkArgument(id != null);

        VoteManager.choices.put(id, choices);
    }

    public static boolean hasChoices(String id) {
        return isNowVoting(id) && choices.get(id) != null && !choices.get(id).isEmpty();
    }

    public static Set<Object> getChoices(String id) {
        return choices.get(id);
    }

    public static Multimap<Object, OfflinePlayer> endVote(String id) {
        choices.remove(id);
        return vote.remove(id);
    }

    public static Multimap<Object, OfflinePlayer> getResult(String id) {
        Preconditions.checkArgument(vote.containsKey(id));

        return ImmutableMultimap.copyOf(vote.get(id));
    }

    public static boolean isNowVoting(String id) {
        return vote.containsKey(id);
    }

}
