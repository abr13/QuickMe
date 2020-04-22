package com.abr.quickme;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.abr.quickme.fragments.ChatsFragment;
import com.abr.quickme.fragments.FriendsFragment;
import com.abr.quickme.fragments.RequestsFragment;
import com.abr.quickme.fragments.VideoListFragment;

class MainPagerAdapter extends FragmentPagerAdapter {

    MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            case 2:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 3:
                VideoListFragment videochatsfragment = new VideoListFragment();
                return videochatsfragment;
            default:
                ChatsFragment defaultFragment = new ChatsFragment();
                return defaultFragment;

        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "CHATS";
            case 1:
                return "FRIENDS";
            case 2:
                return "REQUESTS";
            case 3:
                return "VIDEO CALLS";
            default:
                return "CHATS";
        }
    }
}
