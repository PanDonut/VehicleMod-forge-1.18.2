package com.mrcrayfish.framework_embedded.network.message.handshake;

import java.util.function.IntSupplier;

/**
 * Author: MrCrayfish
 */
public abstract class LoginIndexHolder implements IntSupplier
{
    private int loginIndex;

    public void setLoginIndex(final int loginIndex)
    {
        this.loginIndex = loginIndex;
    }

    public int getLoginIndex()
    {
        return this.loginIndex;
    }

    @Override
    public int getAsInt()
    {
        return this.getLoginIndex();
    }
}
