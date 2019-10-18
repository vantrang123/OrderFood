package com.trangdv.orderfood.listener;

public interface OnDatabaseChangedListeners {
    void onNewDatabaseEntryAdded();
    void onNewDatabaseEntryRemoved();
    void onNewDatabaseEntryRenamed();
}
