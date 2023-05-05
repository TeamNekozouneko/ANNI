package net.nekozouneko.anniv2.gson;

import com.google.gson.InstanceCreator;

import java.lang.reflect.Type;
import java.util.EnumMap;

public class EnumMapInstanceCreator<K extends Enum<K>, V> implements InstanceCreator<EnumMap<K, V>> {

    private final Class<K> enumType;

    public EnumMapInstanceCreator(Class<K> enumType) {
        this.enumType = enumType;
    }

    @Override
    public EnumMap<K, V> createInstance(Type type) {
        return new EnumMap<>(enumType);
    }

}
