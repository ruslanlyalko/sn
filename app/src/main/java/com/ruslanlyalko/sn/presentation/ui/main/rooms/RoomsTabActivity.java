package com.ruslanlyalko.sn.presentation.ui.main.rooms;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.ruslanlyalko.sn.R;
import com.ruslanlyalko.sn.common.DateUtils;
import com.ruslanlyalko.sn.presentation.base.BaseActivity;
import com.ruslanlyalko.sn.presentation.ui.main.rooms.frag.RoomFragment;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;

public class RoomsTabActivity extends BaseActivity {

    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;
    private Date mCurrentDate = new Date();
    private SectionsPagerAdapter mSectionsPagerAdapter;

    public static Intent getLaunchIntent(final Context launchIntent) {
        return new Intent(launchIntent, RoomsTabActivity.class);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_room_tab;
    }

    @Override
    protected void setupView() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        setTitle(getString(R.string.title_activity_rooms_tab, DateUtils.toString(mCurrentDate, "dd.MM  EEEE")));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_choose_date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mCurrentDate);
            DatePickerDialog dialog = DatePickerDialog.newInstance((datePicker, year, month, day)
                            -> {
                        mCurrentDate = DateUtils.getDate(mCurrentDate, year, month, day);
                        setTitle(getString(R.string.title_activity_rooms_tab, DateUtils.toString(mCurrentDate, "dd.MM  EEEE")));
                        mSectionsPagerAdapter.setDate(mCurrentDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show(getFragmentManager(), "date");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rooms_tab, menu);
        return true;
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        RoomFragment room0;
        RoomFragment room1;
        RoomFragment room2;
        RoomFragment room3;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        RoomFragment getRoom0() {
            if (room0 == null)
                room0 = RoomFragment.newInstance(0);
            return room0;
        }

        RoomFragment getRoom1() {
            if (room1 == null)
                room1 = RoomFragment.newInstance(1);
            return room1;
        }

        RoomFragment getRoom2() {
            if (room2 == null)
                room2 = RoomFragment.newInstance(2);
            return room2;
        }

        RoomFragment getRoom3() {
            if (room3 == null)
                room3 = RoomFragment.newInstance(3);
            return room3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return getRoom0();
                case 1:
                    return getRoom1();
                case 2:
                    return getRoom2();
                default:
                    return getRoom3();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_room_1);
                case 1:
                    return getString(R.string.tab_room_2);
                case 2:
                    return getString(R.string.tab_room_3);
                default:
                    return getString(R.string.tab_room_4);
            }
        }

        public void setDate(final Date currentDate) {
            getRoom0().setDate(currentDate);
            getRoom1().setDate(currentDate);
            getRoom2().setDate(currentDate);
            getRoom3().setDate(currentDate);
        }
    }
}
