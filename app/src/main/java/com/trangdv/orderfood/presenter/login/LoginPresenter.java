package com.trangdv.orderfood.presenter.login;

import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.view.ILoginView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LoginPresenter implements ILoginPresenter{
    ILoginView iLoginView;
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;

    public LoginPresenter(ILoginView iLoginView, IAnNgonAPI anNgonAPI, CompositeDisposable compositeDisposable) {
        this.iLoginView = iLoginView;
        this.anNgonAPI = anNgonAPI;
        this.compositeDisposable = compositeDisposable;
    }

    @Override
    public void onLogin(String phoneNumber, String password) {
        compositeDisposable.add(
                anNgonAPI.getUser(Common.API_KEY,
                        phoneNumber,
                        password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(userModel -> {
                                    if (userModel.isSuccess()) {
                                        iLoginView.onLoginSuccess(userModel.getResult().get(0));
                                    } else {
                                        iLoginView.onLoginError(userModel.getMessage());
                                    }
                                },
                                throwable -> {
                                    iLoginView.onLoginError(throwable.getMessage());
                                }
                        ));
    }
}
