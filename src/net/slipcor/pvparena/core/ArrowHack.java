package net.slipcor.pvparena.core;

import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ArrowHack {
    public ArrowHack(final Player player) throws NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException {
        final Method mGetHandle = player.getClass().getMethod("getHandle");
        final Object cHandle = mGetHandle.invoke(player);

        try {
            // >= 1.10
            final Method setArrowCount = cHandle.getClass().getMethod("f", int.class);
            setArrowCount.invoke(cHandle, 0);
        } catch (NoSuchMethodException e) {
            // 1.9
            final Method setOlderArrowCount = cHandle.getClass().getMethod("k", int.class);
            setOlderArrowCount.invoke(cHandle, 0);
        }

    }
}
