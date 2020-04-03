package com.trangdv.orderfood.view;

import com.trangdv.orderfood.model.User;

public interface ILoginView {
    void onLoginSuccess(User user);
    void onLoginError(String message);
}
