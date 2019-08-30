package com.abr.quickme;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.abr.quickme.fragments.ChatsFragment;
import com.abr.quickme.fragments.FriendsFragment;
import com.abr.quickme.fragments.GroupsFragment;
import com.abr.quickme.fragments.RequestsFragment;
import com.abr.quickme.fragments.StoriesFragment;

class MainPagerAdapter extends FragmentPagerAdapter {

    MainPagerAdapter(FragmentManager fm) {
        super(fm);

    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            case 3:
                StoriesFragment storiesFragment = new StoriesFragment();
                return storiesFragment;
            case 4:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;
            default:
                ChatsFragment defaultFragment = new ChatsFragment();
                return defaultFragment;

        }
    }

    @Override
    public int getCount() {
        return 5;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "REQUESTS";
            case 1:
                return "CHATS";
            case 2:
                return "FRIENDS";
            case 3:
                return "STORIES";
            case 4:
                return "GROUPS";
            default:
                return null;
        }
    }
}
