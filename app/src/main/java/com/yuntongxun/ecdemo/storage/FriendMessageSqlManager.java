/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.yuntongxun.ecdemo.storage;

import android.content.ContentValues;
import android.database.Cursor;

import com.yuntongxun.ecdemo.pojo.Friend;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.im.friend.ECFriendMessageBody;
import com.yuntongxun.ecsdk.im.friend.FriendInner;

import java.util.ArrayList;
import java.util.List;


/**
 * 好友关系数据库
 *
 * @author Jorstin Chan@容联•云通讯
 * @version 4.0
 * @date 2014-12-31
 */
public class FriendMessageSqlManager extends AbstractSQLManager {

    public static final int NOTICE_MSG_TYPE = 1000;
    public static final String CONTACT_ID = "10089";

    private static FriendMessageSqlManager instance;

    private FriendMessageSqlManager() {
        super();
    }

    public static FriendMessageSqlManager getInstance() {
        if (instance == null) {
            instance = new FriendMessageSqlManager();
        }
        return instance;
    }

    public static long insertFriend(Friend notice) {


        ContentValues values = new ContentValues();
        if (notice != null) {
            values.put(FriendColumn.Friend_ID, notice.getUseracc());
            values.put(FriendColumn.Friend_Nick, notice.getNickName());
            values.put(FriendColumn.Friend_Pic, notice.getAvatar());
            values.put(FriendColumn.Friend_remark, notice.getRemarkName());
            values.put(FriendColumn.Friend_ship, notice.getFriendState());
            long r = getInstance().sqliteDB().insert(DatabaseHelper.TABLES_NAME_FRIENDS, null, values);
            return r;
        }
        return -1;
    }


    public static boolean isFriendExist(String userId) {
        String sql = "select friend_id from " + DatabaseHelper.TABLES_NAME_FRIENDS + " where friend_id ='" + userId + "'";
        try {
            Cursor cursor = getInstance().sqliteDB().rawQuery(sql, null);
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static int delFriend(String userId) {
        if (!isFriendExist(userId)) {
            return -1;
        }
        try {
            return getInstance().sqliteDB().delete(DatabaseHelper.TABLES_NAME_FRIENDS, "friend_id = ?", new String[]{userId});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * 添加好友通知，成为或者删除好友
     *
     * @param userId
     * @param nick
     * @param ship
     * @param url
     * @return
     */
    // TODO: 2017/10/21 校验
    public static long insertFriendByUserId(String userId, String nick, String ship, String url) {
        if (isFriendExist(userId)) {
            updateState(userId, ship);
            return 0;
        }

        ContentValues values = new ContentValues();
        if (userId != null) {
            values.put(FriendColumn.Friend_ID, userId);
            values.put(FriendColumn.Friend_Nick, nick);
            values.put(FriendColumn.Friend_ship, ship);
            values.put(FriendColumn.Friend_Pic, url);
            long r = getInstance().sqliteDB().insert(DatabaseHelper.TABLES_NAME_FRIENDS, null, values);
            return r;
        }
        return -1;
    }

    public static long insertOrUpdateFriendByUserId(String userId, String nick, String ship, String url, String remarkName) {
        if (isFriendExist(userId)) {
            updateAll(userId, nick, ship, url, remarkName);
            return 0;
        }

        ContentValues values = new ContentValues();
        if (userId != null) {
            values.put(FriendColumn.Friend_ID, userId);
            values.put(FriendColumn.Friend_Nick, nick);
            values.put(FriendColumn.Friend_ship, ship);
            values.put(FriendColumn.Friend_Pic, url);
            values.put(FriendColumn.Friend_remark, remarkName);
            long r = getInstance().sqliteDB().insert(DatabaseHelper.TABLES_NAME_FRIENDS, null, values);
            return r;
        }
        return -1;
    }

    public static int getCount(String ship) {
        try {
            String sql = "select friend_id from friends where ship = '" + ship + "'";
            Cursor cursor = getInstance().sqliteDB().rawQuery(sql, null);
            if (cursor != null && cursor.getCount() > 0) {
                return cursor.getCount();
            }
            return 0;
        } catch (Exception e) {
        }
        return 0;
    }


    public static void updateAll(String userId, String nick, String ship, String url, String remarkName) {

        if (isFriendExist(userId)) {

            ContentValues values = new ContentValues();
            values.put(FriendColumn.Friend_Nick, nick);
            values.put(FriendColumn.Friend_ship, ship);
            values.put(FriendColumn.Friend_Pic, url);
            values.put(FriendColumn.Friend_remark, remarkName);

            getInstance().sqliteDB().update(DatabaseHelper.TABLES_NAME_FRIENDS, values, " friend_id ='" + userId + "'", null);
        }
    }

    public static void updateFriendByRemark(String userId, String remarkName) {

        if (isFriendExist(userId)) {
            ContentValues values = new ContentValues();

            values.put(FriendColumn.Friend_remark, remarkName);

            getInstance().sqliteDB().update(DatabaseHelper.TABLES_NAME_FRIENDS, values, " friend_id ='" + userId + "'", null);
        }
    }


    public static List<Friend> queryFriendsNotice() {
        ArrayList<Friend> mArrayList = new ArrayList<Friend>();
        try {
            String sql = "select * from " + DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE_FRIEND;
            Cursor cursor = getInstance().sqliteDB().rawQuery(sql, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    Friend group = new Friend();
                    group.setUseracc(cursor.getString(6));
                    group.setNickName(cursor.getString(1));
                    group.setFriendState(cursor.getString(5));
                    mArrayList.add(group);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mArrayList;
    }


    public static List<Friend> queryFriends() {
        ArrayList<Friend> mArrayList = new ArrayList<Friend>();
        try {
            String sql = "select * from " + DatabaseHelper.TABLES_NAME_FRIENDS + " WHERE ship = 1";
            Cursor cursor = getInstance().sqliteDB().rawQuery(sql, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    Friend group = new Friend();
                    group.setUseracc(cursor.getString(1));
                    group.setNickName(cursor.getString(2));
                    group.setFriendState(cursor.getString(5));
                    group.setAvatar(cursor.getString(3));
                    group.setRemarkName(cursor.getString(4));
                    mArrayList.add(group);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mArrayList;
    }

    //    查询头像
    public static synchronized String queryURLByID(String userId) {
        try {
            String url = "";
            String sql = "select * from " + DatabaseHelper.TABLES_NAME_FRIENDS + " where friend_id ='" + userId + "'";
            Cursor cursor = getInstance().sqliteDB().rawQuery(sql, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    url = (cursor.getString(cursor.getColumnIndex(FriendColumn.Friend_Pic)));
                }
                cursor.close();
            }
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Friend queryFriendById(String userId) {

        try {
            String sql = "select * from " + DatabaseHelper.TABLES_NAME_FRIENDS + " where friend_id ='" + userId + "'";
            Cursor cursor = getInstance().sqliteDB().rawQuery(sql, null);
            if (cursor != null && cursor.getCount() > 0) {

                cursor.moveToFirst();
                Friend friend = new Friend();
                friend.setUseracc(cursor.getString(1));
                friend.setNickName(cursor.getString(2));
                friend.setFriendState(cursor.getString(5));
                friend.setAvatar(cursor.getString(3));
                friend.setRemarkName(cursor.getString(4));
                return friend;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //  查询好友备注
    public static String queryMarkByID(String userId) {
        String remark = "";
        String sql = "select * from " + DatabaseHelper.TABLES_NAME_FRIENDS + " where friend_id ='" + userId + "'";
        Cursor cursor = getInstance().sqliteDB().rawQuery(sql, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                remark = (cursor.getString(cursor.getColumnIndex(FriendColumn.Friend_remark)));
            }
            cursor.close();
        }
        return remark;
    }


    public static  synchronized boolean isFriend(String account) {
        String sql = "select friend_id from " + DatabaseHelper.TABLES_NAME_FRIENDS + " where friend_id ='" + account + "'";
        Cursor cursor = getInstance().sqliteDB().rawQuery(sql, null);
        boolean isFriend = false;
        if (cursor != null && cursor.getCount() > 0) {
            isFriend = true;
            cursor.close();
        }
        return isFriend;
    }


    //
    public static long insertFriendNoticeMsg(ECMessage notice) {


        ECFriendMessageBody friendMessageBody = (ECFriendMessageBody) notice.getBody();
        FriendInner inner = friendMessageBody.getInner();
        ContentValues values = new ContentValues();
        if (notice != null) {
            values.put(SystemNoticeColumnFriend.NOTICE_NICKNAME_SEND, notice.getNickName());
            values.put(SystemNoticeColumnFriend.NOTICE_RECE, notice.getTo());//no #
            values.put(SystemNoticeColumnFriend.NOTICE_MSG, inner.getMsg());
            values.put(SystemNoticeColumnFriend.NOTICE_SUBTYPE, inner.getSubType().ordinal() + "");
            values.put(SystemNoticeColumnFriend.NOTICE_SEND, notice.getForm());

            long r = getInstance().sqliteDB().insert(DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE_FRIEND, null, values);

            return r;
        }
        return -1;
    }


    public static long updateState(String account, String state) {
        ContentValues values = new ContentValues();
        values.put(FriendColumn.Friend_ship, state);
        return getInstance().sqliteDB().update(DatabaseHelper.TABLES_NAME_FRIENDS, values, " friend_id ='" + account + "'", null);
    }


    /**
     * 情况群组通知消息
     */
    public static void delSessions() {
        getInstance().sqliteDB().delete(DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE, null, null);
    }

    public static void setSessionRead() {

    }

    public static void registerMsgObserver(OnMessageChange observer) {
        getInstance().registerObserver(observer);
    }

    public static void unregisterMsgObserver(OnMessageChange observer) {
        getInstance().unregisterObserver(observer);
    }

    public static void notifyMsgChanged(String session) {
        getInstance().notifyChanged(session);
    }

    public static void reset() {
        getInstance().release();
    }

    @Override
    protected void release() {
        super.release();
        instance = null;
    }


}
