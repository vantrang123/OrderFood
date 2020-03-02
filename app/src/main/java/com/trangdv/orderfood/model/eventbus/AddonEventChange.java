package com.trangdv.orderfood.model.eventbus;

import com.trangdv.orderfood.model.Addon;

public class AddonEventChange {
    private boolean isAdd;
    private Addon addon;

    public AddonEventChange(boolean isAdd, Addon addon) {
        this.isAdd = isAdd;
        this.addon = addon;
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(boolean add) {
        isAdd = add;
    }

    public Addon getAddon() {
        return addon;
    }

    public void setAddon(Addon addon) {
        this.addon = addon;
    }
}
