package org.edx.mobile.test;

import android.support.annotation.Nullable;

import org.edx.mobile.view.Presenter;

public interface PresenterInjector {
    @Nullable
    Presenter<?> getPresenter();
}
