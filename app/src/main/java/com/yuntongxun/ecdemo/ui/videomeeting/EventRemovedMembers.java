package com.yuntongxun.ecdemo.ui.videomeeting;

import java.util.ArrayList;

/**
 * Created by smileklvens on 2017/8/28.
 */

class EventRemovedMembers {

    ArrayList<MultiVideoMember> removedMembers;

    public EventRemovedMembers(ArrayList<MultiVideoMember> mRemoveMember) {
        this.removedMembers = mRemoveMember;
    }

    public ArrayList<MultiVideoMember> getRemovedMembers() {
        return removedMembers;
    }
}
