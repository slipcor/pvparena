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
        final Method mGetDataWatcher = cHandle.getClass().getMethod(
                "getDataWatcher");
        final Object cWatcher = mGetDataWatcher.invoke(cHandle);
        final Method mWatch = cWatcher.getClass().getMethod("watch", int.class,
                Object.class);
        mWatch.invoke(cWatcher, 9, (byte) 0);
    }
}
