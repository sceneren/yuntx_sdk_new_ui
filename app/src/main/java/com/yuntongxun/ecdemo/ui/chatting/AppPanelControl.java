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
package com.yuntongxun.ecdemo.ui.chatting;

import android.content.Context;

import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.ui.SDKCoreHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天插件功能控制器
 *
 * @author Jorstin Chan@容联•云通讯
 * @version 4.0
 * @date 2014-12-10
 */
public class AppPanelControl {

    private Context mContext;

    public static boolean isShowVoipCall = true;
    public static boolean isRobot = false;

    public int[] cap = new int[]{R.string.app_panel_pic,
            R.string.app_panel_tackpic, R.string.app_panel_short_video,
            R.string.app_panel_location,
            R.string.attach_red_packet};
    public int[] caprot = new int[]{R.string.robot_chat,
            R.string.robot_laungh,
            R.string.robot_question,
            R.string.robot_wearth};


    public int[] capVoip = new int[]{R.string.app_panel_pic, R.string.app_panel_tackpic, R.string.app_panel_short_video
            , R.string.app_panel_location, R.string.attach_red_packet, R.string.app_panel_read_after_fire,
            R.string.app_panel_voice, R.string.app_panel_video};

    /**
     *
     */
    public AppPanelControl() {
        mContext = CCPAppManager.getContext();
    }


    public static void setShowVoipCall(boolean isShowVoipCall) {
        AppPanelControl.isShowVoipCall = isShowVoipCall;
    }

    public static boolean isRobot() {
        return isRobot;
    }

    public static void setIsRobot(boolean isRobot) {
        AppPanelControl.isRobot = isRobot;
    }

    /**
     * @return
     */
    public List<Capability> getCapability() {
        List<Capability> capabilities = new ArrayList<Capability>();

        if(isRobot){
            for (int i = 0; i < caprot.length; i++) {
                Capability capability = getCapability(caprot[i]);
                capabilities.add(capabilities.size(), capability);
            }
            return capabilities;
        }


        if (isShowVoipCall && SDKCoreHelper.getInstance().isSupportMedia()) {
            for (int i = 0; i < capVoip.length; i++) {
                Capability capability = getCapability(capVoip[i]);
                capabilities.add(capabilities.size(), capability);
            }
        } else {
            for (int i = 0; i < cap.length; i++) {
                Capability capability = getCapability(cap[i]);
                capabilities.add(capabilities.size(), capability);
            }
        }
        return capabilities;
    }

    /**
     * @param resid
     * @return
     */
    private Capability getCapability(int resid) {
        Capability capability = null;
        switch (resid) {
            case R.string.app_panel_pic:
                capability = new Capability(getContext().getString(
                        R.string.app_panel_pic), R.drawable.chat_icon_xiangce_normal);
                break;
            case R.string.app_panel_tackpic:
                capability = new Capability(getContext().getString(
                        R.string.app_panel_tackpic), R.drawable.chat_icon_paishe_normal);
                break;
            case R.string.app_panel_short_video:
                capability = new Capability(getContext().getString(
                        R.string.app_panel_short_video), R.drawable.chat_icon_duanshipin_normal);
                break;
            case R.string.app_panel_file:
                capability = new Capability(getContext().getString(
                        R.string.app_panel_file), R.drawable.capability_file_icon);
                break;
            case R.string.app_panel_voice:

                capability = new Capability(getContext().getString(
                        R.string.app_panel_voice), R.drawable.chat_icon_yuyinliaotian_normal);
                break;
            case R.string.app_panel_video:

                capability = new Capability(getContext().getString(
                        R.string.app_panel_video), R.drawable.chat_icon_shipintonghua_normal);
                break;
            case R.string.app_panel_read_after_fire:

                capability = new Capability(getContext().getString(
                        R.string.app_panel_read_after_fire), R.drawable.chat_icon_fenshao_normal);
                break;
            case R.string.app_panel_location:

                capability = new Capability(getContext().getString(
                        R.string.app_panel_location), R.drawable.chat_icon_dingwei_normal);
                break;
            //红包按钮
            case R.string.robot_chat:
                capability = new Capability(getContext().getString(
                        R.string.robot_chat), R.drawable.chat);
                break;
            case R.string.robot_laungh:
                capability = new Capability(getContext().getString(
                        R.string.robot_laungh), R.drawable.joking);
                break;
            case R.string.robot_question:
                capability = new Capability(getContext().getString(
                        R.string.robot_question), R.drawable.question);
                break;
            case R.string.robot_wearth:
                capability = new Capability(getContext().getString(
                        R.string.robot_wearth), R.drawable.weather);
                break;
            case R.string.attach_red_packet:
                capability = new Capability(getContext().getString(
                        R.string.attach_red_packet), R.drawable.file);
                break;

            default:
                break;
        }
        capability.setId(resid);
        return capability;
    }

    /**
     * @return
     */
    private Context getContext() {
        if (mContext == null) {
            mContext = ECApplication.getInstance().getApplicationContext();
        }
        return mContext;
    }
}
