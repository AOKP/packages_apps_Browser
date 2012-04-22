/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.browser;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.android.browser.UI.ComboViews;
import com.android.browser.view.PieItem;
import com.android.browser.view.PieMenu.PieView.OnLayoutListener;
import com.android.browser.view.PieStackView;

import java.util.List;

/**
 * controller for Quick Controls pie menu
 */
public class PieControlPhone extends PieControlBase implements OnClickListener,
        OnMenuItemClickListener {

    private PhoneUi mUi;
    private PieItem mUrl;
    private PieItem mShowTabs;
    private PieItem mOptions;
    private PieItem mNewTab;
    private PieItem mBookmarks;
    private TabAdapter mTabAdapter;
    private PopupMenu mPopup;

    private boolean mUseExtControls;

    private PieItem mBack;
    private PieItem mForward;
    private PieItem mRefresh;
    private PieItem mClose;

    public PieControlPhone(Activity activity, UiController controller, PhoneUi ui, boolean useExtControls) {
        super(activity, controller);
        mUi = ui;
        mUseExtControls = useExtControls;
    }

    protected void populateMenu() {
        int levelShift=1;
        if (mUseExtControls) {
          levelShift++;
        }

        mUrl = makeItem(R.drawable.ic_web_holo_dark, 1);
        View tabs = makeTabsView();
        mShowTabs = new PieItem(tabs, levelShift);
        mTabAdapter = new TabAdapter(mActivity, mUiController);
        PieStackView stack = new PieStackView(mActivity);
        stack.setLayoutListener(new OnLayoutListener() {
            @Override
            public void onLayout(int ax, int ay, boolean left) {
                buildTabs();
            }
        });
        stack.setOnCurrentListener(mTabAdapter);
        stack.setAdapter(mTabAdapter);
        mShowTabs.setPieView(stack);
        mOptions = makeItem(com.android.internal.R.drawable.ic_menu_moreoverflow_normal_holo_dark,
                levelShift);

        mNewTab = makeItem(R.drawable.ic_new_window_holo_dark, levelShift);
        mBookmarks = makeItem(R.drawable.ic_bookmarks_holo_dark, 1);
 
        if (mUseExtControls) {
            // Scale the pie
            mPie.setRadiusScaling(70);

            mBack = makeItem(R.drawable.ic_back_holo_dark, 1);
            mRefresh = makeItem(R.drawable.ic_refresh_holo_dark, 2);
            mForward = makeItem(R.drawable.ic_forward_holo_dark, 2);
            mClose = makeItem(R.drawable.ic_close_window_holo_dark, 2);

            // level 1
            mPie.addItem(mBack);
            mPie.addItem(mUrl);
            mPie.addItem(mBookmarks);
            // level 2
            mPie.addItem(mForward);
            mPie.addItem(mRefresh);
            mPie.addItem(mOptions);
            mPie.addItem(mShowTabs);
            mPie.addItem(mNewTab);
            mPie.addItem(mClose);
        } else {
            // level 1
            mPie.addItem(mNewTab);
            mPie.addItem(mShowTabs);
            mPie.addItem(mUrl);
            mPie.addItem(mBookmarks);
            mPie.addItem(mOptions);
        }
        setClickListener(this, mUrl, mShowTabs, mOptions, mNewTab, mBookmarks);
        if (mUseExtControls) {
            setClickListener(this, mBack, mRefresh, mForward, mClose);
        }
        mPopup = new PopupMenu(mActivity, mUi.getTitleBar());
        Menu menu = mPopup.getMenu();
        mPopup.getMenuInflater().inflate(R.menu.browser, menu);
        mPopup.setOnMenuItemClickListener(this);
    }

    protected void showMenu() {
        mUiController.updateMenuState(mUiController.getCurrentTab(), mPopup.getMenu());
        mPopup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return mUiController.onOptionsItemSelected(item);
    }


    private void buildTabs() {
        final List<Tab> tabs = mUiController.getTabs();
        mUi.getActiveTab().capture();
        mTabAdapter.setTabs(tabs);
        PieStackView sym = (PieStackView) mShowTabs.getPieView();
        sym.setCurrent(mUiController.getTabControl().getCurrentPosition());

    }

    @Override
    public void onClick(View v) {
        Tab tab = mUiController.getTabControl().getCurrentTab();
        WebView web = tab.getWebView();
        if (mUrl.getView() == v) {
            mUi.editUrl(false);
        } else if (mShowTabs.getView() == v) {
            mUi.showNavScreen();
        } else if (mOptions.getView() == v) {
            showMenu();
        } else if (mNewTab.getView() == v) {
            mUiController.openTabToHomePage();
            mUi.editUrl(false);
        } else if (mBookmarks.getView() == v) {
            mUiController.bookmarksOrHistoryPicker(ComboViews.Bookmarks);
        }

        if (mBack != null) {
            if (mBack.getView() == v) {
                tab.goBack();
            } else if (mForward.getView() == v) {
                tab.goForward();
            } else if (mRefresh.getView() == v) {
                if (tab.inPageLoad()) {
                    web.stopLoading();
                } else {
                    web.reload();
                }
            } else if (mClose.getView() == v) {
                mUiController.closeCurrentTab();
            }
        }
    }

}
