package net.nekozouneko.anni.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Tuple<T, U> {

    private final T first;
    private final U second;

}
