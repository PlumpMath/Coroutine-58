package com.nepxion.coroutine.framework.core;

/**
 * <p>Title: Nepxion Coroutine</p>
 * <p>Description: Nepxion Coroutine For Distribution</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: Nepxion</p>
 * @author Neptune
 * @email 1394997@qq.com
 * @version 1.0
 */

import com.nepxion.coroutine.common.constant.CoroutineConstants;
import com.nepxion.coroutine.common.spi.SpiLoader;

public class CoroutineLoader {
    private static final CoroutineLauncher COROUTINE_LAUNCHER = SpiLoader.loadDelegateFromProperties(CoroutineLauncher.class, CoroutineConstants.COROUTINE_LAUNCHER_ELEMENT_NAME);

    public static CoroutineLauncher load() {
        return COROUTINE_LAUNCHER;
    }
}