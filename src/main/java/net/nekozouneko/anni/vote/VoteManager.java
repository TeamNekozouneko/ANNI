package net.nekozouneko.anni.vote;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class VoteManager {

    public VoteManager(Set<String> choices) {
        this.choices.addAll(choices);
    }

    private final Map<OfflinePlayer, String> votes = new HashMap<>();
    private final Set<String> choices = new HashSet<>();

    public void vote(Player player, String choice) {
        if (!choices.contains(choice)) return;

        votes.put(player, choice);
    }

    public void clear() {
        votes.clear();
    }

    public void updateChoices(Set<String> choices) {
        this.choices.clear();
        this.choices.addAll(choices);

        new HashMap<>(votes).forEach((player, vote) -> {
            if (!this.choices.contains(vote)) this.votes.remove(player);
        });
    }

    public Set<String> getChoices() {
        return Collections.unmodifiableSet(choices);
    }

    public String getResult() {
        return getSortedResults().get(0).getKey();
    }

    public Map<String, Integer> getResults() {
        Map<String, Integer> map = new HashMap<>();

        votes.values().forEach(choice -> {
            map.put(choice, map.getOrDefault(choice, 0) + 1);
        });

        return map;
    }

    @SuppressWarnings("unchecked")
    public List<Map.Entry<String, Integer>> getSortedResults() {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(getResults().entrySet());
        list.sort(Comparator.comparingInt(entry -> ((Map.Entry<String, Integer>) entry).getValue()).reversed());

        return list;
    }

    public boolean isEmpty() {
        return votes.isEmpty();
    }

}
