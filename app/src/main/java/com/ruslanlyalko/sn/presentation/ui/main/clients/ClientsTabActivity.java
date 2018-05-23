package com.ruslanlyalko.sn.presentation.ui.main.clients;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;
import com.ruslanlyalko.sn.presentation.ui.main.clients.birth.BirthActivity;
import com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.ContactsFragment;
import com.ruslanlyalko.sn.presentation.ui.main.clients.contacts.edit.ContactEditActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class ClientsTabActivity extends BaseActivity implements OnFilterListener {

    private static final int TAB_ADULT = 0;
    private static final int TAB_CHILD = 1;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.appbar) AppBarLayout mAppbar;
    @BindView(R.id.tabs) TabLayout mTabs;
    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.main_content) CoordinatorLayout mMainContent;
    private String mFilterName = "";
    private String mFilterPhone = "";

    public static Intent getLaunchIntent(final Context context) {
        return new Intent(context, ClientsTabActivity.class);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_clients_tab;
    }

    @Override
    protected void setupView() {
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabs));
        mTabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_choose_date) {
            startActivity(BirthActivity.getLaunchIntent(this));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rooms_tab, menu);
        return true;
    }

    @OnClick(R.id.fab)
    public void onFabClicked() {
        startActivity(ContactEditActivity.getLaunchIntent(this, mFilterName, mFilterPhone));
    }

    @Override
    public void onFilterChanged(final String name, final String phone) {
        mFilterName = name;
        mFilterPhone = phone;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TAB_ADULT:
                    return ContactsFragment.newInstance(position, false);
                case TAB_CHILD:
                    return ContactsFragment.newInstance(position, false);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
