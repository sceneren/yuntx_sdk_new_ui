package com.yuntongxun.ecdemo.common.utils;

import android.text.TextUtils;

import com.yuntongxun.ecdemo.ui.contact.ECContacts;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 联系人搜索
 * 联系人搜索
 */
public class PatchMgr {


    /**
     * 按号码-拼音搜索联系人
     *
     * @param str List<ECContacts>
     */
    public static List<ECContacts> search(String str,
                                          List<ECContacts> allContacts) {
        List<ECContacts> contactList = new ArrayList<>();
        // 如果搜索条件以0 1 +开头则按号码搜索
        if (str.startsWith("0") || str.startsWith("1") || str.startsWith("+")) {
            for (ECContacts contact : allContacts) {
                if (contact.getContactid() != null) {
                    if (contact.getContactid().contains(str) && !contactList.contains(contact)) {
                        contactList.add(contact);
                    }
                }
                if (contact.getNickname() != null) {
                    if (contact.getNickname().contains(str) && !contactList.contains(contact)) {
                        contactList.add(contact);
                    }
                }
            }

            return contactList;
        }

        String result = "";
        for (ECContacts contact : allContacts) {
            if (TextUtils.isEmpty(contact.getContactid())) {
                continue;
            }
            // 先将输入的字符串转换为拼音
            result = PingYinUtil.getPingYin(str);
            if (contains(contact, result)) {
                contactList.add(contact);
            } else if (contact.getContactid().contains(str)) {
                contactList.add(contact);
            }
        }
        return contactList;
    }

    /**
     * 根据拼音搜索
     * 搜索条件是否大于6个字符
     *
     * @return
     */
    public static boolean contains(ECContacts contact, String search) {
        if (TextUtils.isEmpty(contact.getNickname())) {
            return false;
        }

        boolean flag = false;

        // 简拼匹配,如果输入在字符串长度大于6就不按首字母匹配了
        if (search.length() < 6) {
            String firstLetters = PingYinUtil.getPingYin(contact.getNickname()).substring(0, 1).toUpperCase();
            ;
            // 不区分大小写
            Pattern firstLetterMatcher = Pattern.compile(search,
                    Pattern.CASE_INSENSITIVE);
            flag = firstLetterMatcher.matcher(firstLetters).find();
        }

        if (!flag) { // 如果简拼已经找到了，就不使用全拼了
            // 不区分大小写
            Pattern pattern2 = Pattern
                    .compile(search, Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = pattern2.matcher(PingYinUtil.getPingYin(contact.getNickname()));
            flag = matcher2.find();
        }

        return flag;
    }

}
