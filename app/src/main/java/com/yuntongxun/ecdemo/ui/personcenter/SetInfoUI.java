package com.yuntongxun.ecdemo.ui.personcenter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.ECCircumscription;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.ECPreferenceSettings;
import com.yuntongxun.ecdemo.common.utils.ECPreferences;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.common.view.wheelview.OnWheelScrollListener;
import com.yuntongxun.ecdemo.common.view.wheelview.WheelView;
import com.yuntongxun.ecdemo.common.view.wheelview.adapter.NumericWheelAdapter;
import com.yuntongxun.ecdemo.core.ClientUser;
import com.yuntongxun.ecdemo.storage.ContactSqlManager;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecdemo.ui.BaseActivity;
import com.yuntongxun.ecdemo.ui.RestServerDefines;
import com.yuntongxun.ecdemo.ui.chatting.IMChattingHelper;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecdemo.ui.phonemodel.HttpMethods;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.PersonInfo;
import com.yuntongxun.ecsdk.SdkErrorCode;

import org.json.JSONObject;

import java.io.InvalidClassException;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by luhuashan on 17/8/23.
 * email huashan2007@sina.cn
 */
public class SetInfoUI extends BaseActivity {


    public static String Type = "type";
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.et_single)
    EditText etSingle;
    @BindView(R.id.et_mult)
    EditText etMult;
    @BindView(R.id.cb_nan)
    CheckBox cbNan;
    @BindView(R.id.re_nan)
    RelativeLayout reNan;
    @BindView(R.id.cb_nv)
    CheckBox cbNv;
    @BindView(R.id.re_nv)
    RelativeLayout reNv;
    @BindView(R.id.ll_sex)
    LinearLayout llSex;


    private LayoutInflater inflater = null;
    private WheelView year;
    private WheelView month;
    private WheelView day;
    private int mYear = 1996;
    private int mMonth = 0;
    private int mDay = 1;

    LinearLayout ll;
    TextView tv1;

    View view = null;

    boolean isMonthSetted = false, isDaySetted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }


    static enum TType {
        Nick,
        Sign,
        BeiZhu,
        Sex,
        Age,
    }


    @Override
    protected void initView(Bundle savedInstanceState) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.set_info;
    }

    private int type;

    private String friendId;

    @Override
    protected void initWidgetAciotns() {

        type = getIntent().getIntExtra(Type, 0);

        friendId = getIntent().getStringExtra("friendId");
        String mark = getIntent().getStringExtra("mark");

        String title = "";

        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        ll = (LinearLayout) findViewById(R.id.ll);

        RelativeLayout re = (RelativeLayout) findViewById(R.id.re_age);

        PersonInfo personInfo = CCPAppManager.getPersonInfo();

        if (type == TType.Nick.ordinal()) {
            title = "昵称";
            etMult.setVisibility(View.GONE);
            llSex.setVisibility(View.GONE);
            re.setVisibility(View.GONE);

            etSingle.setText(personInfo.getNickName());

        } else if (type == TType.Sign.ordinal()) {
            re.setVisibility(View.GONE);
            llSex.setVisibility(View.GONE);
            etSingle.setVisibility(View.GONE);
            title = "签名";
            etMult.setText(personInfo.getSign());
        } else if (type == TType.BeiZhu.ordinal()) {
            re.setVisibility(View.GONE);
            llSex.setVisibility(View.GONE);
            etMult.setVisibility(View.GONE);
            title = "修改备注";
            etSingle.setText(mark);
            etSingle.setSelection(mark.length());
        } else if (type == TType.Sex.ordinal()) {
            re.setVisibility(View.GONE);
            etMult.setVisibility(View.GONE);
            etSingle.setVisibility(View.GONE);
            PersonInfo p = CCPAppManager.getPersonInfo();
            if (p.getSex() == PersonInfo.Sex.MALE) {
                cbNv.setChecked(false);
                cbNan.setChecked(true);
            } else {
                cbNan.setChecked(false);
                cbNv.setChecked(true);
            }
            title = "性别";


            cbNan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        cbNv.setChecked(false);
                        cbNan.setChecked(true);
                    } else {
                        cbNv.setChecked(true);
                    }

                }
            });
            cbNv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        cbNv.setChecked(true);
                        cbNan.setChecked(false);
                    } else {
                        cbNan.setChecked(true);

                    }
                }
            });


        } else if (type == 4) {
            title = "年龄";
            ll.addView(getDataPick());
            tv1 = (TextView) findViewById(R.id.tv1);//年龄
            etMult.setVisibility(View.GONE);
            etSingle.setVisibility(View.GONE);
            llSex.setVisibility(View.GONE);
        }

        initTooleBar(titleBar, true, title);


        titleBar.setMySettingText("保存").setSettingTextOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String single = etSingle.getText().toString().trim();
                final String mult = etMult.getText().toString().trim();
                if (type == 4) {
                    setAge(date);
                    return;
                }


                if (type == 0 ) {
                    if (TextUtils.isEmpty(single)) {
                        ToastUtil.showMessage("请输入内容!");
                        return;
                    }
                }
                if (type == 1 && TextUtils.isEmpty(mult)) {
                    ToastUtil.showMessage("请输入内容!");
                    return;
                }
                boolean isCheck = cbNan.isChecked() || cbNv.isChecked();

                if (type == 3 && !isCheck) {

                    ToastUtil.showMessage("请选择性别");
                    return;
                }

                if (type == 3) {
                    handleSetSex(cbNan.isChecked());
                    return;
                }
                setResult(type == 1 ? mult : single, type);
            }
        });
    }

    private void handleSetSex(final boolean checked) {


        PersonInfo personInfo = CCPAppManager.getPersonInfo();



        if (checked) {
            personInfo.setSex(PersonInfo.Sex.MALE);
        } else {
            personInfo.setSex(PersonInfo.Sex.FEMALE);
        }

        ECDevice.setPersonInfo(personInfo, new ECDevice.OnSetPersonInfoListener() {
            @Override
            public void onSetPersonInfoComplete(ECError e, int version) {
                IMChattingHelper.getInstance().mServicePersonVersion = version;
//                dismissPostingDialog();
                if (SdkErrorCode.REQUEST_SUCCESS == e.errorCode) {

                    ToastUtil.showMessage("设置成功");
                    try {
                        ClientUser clientUser = CCPAppManager.getClientUser();
                        if (clientUser != null) {
                            clientUser.setSex(checked ? PersonInfo.Sex.MALE.ordinal() : PersonInfo.Sex.FEMALE.ordinal());
                            clientUser.setpVersion(version);
                            CCPAppManager.setClientUser(clientUser);
                            ECContacts contacts = new ECContacts();
                            contacts.setClientUser(clientUser);
                            ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_REGIST_AUTO, clientUser.toString(), true);
                            ContactSqlManager.insertContact(contacts, clientUser.getSex());
                        }
                        finish();

                    } catch (InvalidClassException e1) {
                        e1.printStackTrace();
                    }
                    return;
                }
                ToastUtil.showMessage("设置失败,请检查网络");
            }
        });

    }

    private void setResult(final String text, final int t) {


        if (t == 2) {//备注
            if (text.length() > ECCircumscription.GROUP_CARD) {
                ToastUtil.showMessage("抱歉、备注名字数超过最大限制");
                return;
            }

            handleSetFriendMark(friendId, text);
            return;
        }


        PersonInfo personInfo = CCPAppManager.getPersonInfo();

        if (t == 0) {//昵称
            personInfo.setNickName(text);
            CCPAppManager.getClientUser().setUserName(text);
            if (text != null && text.length() > 12) {
                ToastUtil.showMessage("抱歉、您输入字数超过了最大限制");
                return;
            }


        } else if (t == 1) {//签名
            personInfo.setSign(text);
            CCPAppManager.getClientUser().setSignature(text);
            if (text != null && text.length() > 30) {
                ToastUtil.showMessage("抱歉、您输入字数超过了最大限制");
                return;
            }
        }

        ECDevice.setPersonInfo(personInfo, new ECDevice.OnSetPersonInfoListener() {
            @Override
            public void onSetPersonInfoComplete(ECError e, int version) {
                IMChattingHelper.getInstance().mServicePersonVersion = version;
//                dismissPostingDialog();
                if (SdkErrorCode.REQUEST_SUCCESS == e.errorCode) {

                    ToastUtil.showMessage("设置成功");

                    try {
                        ClientUser clientUser = CCPAppManager.getClientUser();
                        if (clientUser != null) {


                            if (t == 0) {//nick
                                clientUser.setUserName(text);
                                ECApplication.nick = text;
                            } else if (t == 1) {
                                ECApplication.sign = text;
                                clientUser.setSignature(text);
                            }
                            clientUser.setpVersion(version);
                            CCPAppManager.setClientUser(clientUser);
                            ECContacts contacts = new ECContacts();
                            contacts.setClientUser(clientUser);
                            ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_REGIST_AUTO, clientUser.toString(), true);
                            ContactSqlManager.insertContact(contacts, clientUser.getSex());//更新昵称
                            finish();
                        }

                    } catch (InvalidClassException e1) {
                        e1.printStackTrace();
                    }
                    return;
                }
                ToastUtil.showMessage("设置失败,请检查网络");
            }
        });
    }

    private void setAge(final String text) {
        PersonInfo personInfo = CCPAppManager.getPersonInfo();

        personInfo.setBirth(text);

        ECDevice.setPersonInfo(personInfo, new ECDevice.OnSetPersonInfoListener() {
            @Override
            public void onSetPersonInfoComplete(ECError e, int version) {
                IMChattingHelper.getInstance().mServicePersonVersion = version;
                if (SdkErrorCode.REQUEST_SUCCESS == e.errorCode) {

                    ToastUtil.showMessage("设置成功");

                    try {
                        ClientUser clientUser = CCPAppManager.getClientUser();
                        if (clientUser != null) {
                            clientUser.setpVersion(version);
                            clientUser.setBirth(text);
                            CCPAppManager.setClientUser(clientUser);
                            ECContacts contacts = new ECContacts();
                            contacts.setClientUser(clientUser);
                            ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_REGIST_AUTO, clientUser.toString(), true);
                        }
                        finish();

                    } catch (InvalidClassException e1) {
                        e1.printStackTrace();
                    }
                    return;
                }
                ToastUtil.showMessage("设置失败,请检查网络");
            }
        });
    }

    private void handleSetFriendMark(final String friendId, final String text) {


        showCommonProcessDialog();
        Observer<Object> subscriber = new Observer<Object>() {
            @Override
            public void onComplete() {
                LogUtil.e("onCompleted");
                dismissCommonPostingDialog();
            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.showMessage("设置失败");
                LogUtil.e(e.toString());
                dismissCommonPostingDialog();
            }

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object movieEntity) {
                if (movieEntity != null) {
                    LogUtil.e(movieEntity.toString());
                    ResponseBody body = (ResponseBody) movieEntity;
                    try {
                        String s = new String(body.bytes());
                        String error = "";
                        JSONObject j = new JSONObject(s);
                        if (j != null && j.has("statusMsg")) {
                            error = j.getString("statusMsg");
                        }
                        if (DemoUtils.isTrue(s)) {
                            ToastUtil.showMessage("设置成功");
                            FriendMessageSqlManager.updateFriendByRemark(friendId, text);
                            Intent intent = new Intent();
                            intent.putExtra("remark", text);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            ToastUtil.showMessage(error);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getSig(time);
        JSONObject map = HttpMethods.buildFriendRemark(CCPAppManager.getUserId(), friendId, text);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
        HttpMethods.getInstance(time).setFriendRemark(subscriber, RestServerDefines.APPKER, url, body);
    }


    public String getSig(String stime) {
        String s = RestServerDefines.APPKER + CCPAppManager.getAppToken() + stime;
        return getMessageDigest(s.getBytes());
    }

    public static String getMessageDigest(byte[] input) {
        char[] source = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(input);
            byte[] digest = mDigest.digest();
            int length = digest.length;
            char[] result = new char[length * 2];
            int j = 0;
            for (byte l : digest) {
                result[(j++)] = source[(l >>> 4 & 0xF)];
                result[(j++)] = source[(l & 0xF)];
            }
            return new String(result);
        } catch (Exception e) {
        }
        return null;
    }


    private View getDataPick() {
        Calendar c = Calendar.getInstance();
        int norYear = c.get(Calendar.YEAR);
//		int curMonth = c.get(Calendar.MONTH) + 1;//通过Calendar算出的月数要+1
//		int curDate = c.get(Calendar.DATE);

        int curYear = mYear;
        int curMonth = mMonth + 1;
        int curDate = mDay;

        view = inflater.inflate(R.layout.wheel_date_picker, null);

        year = (WheelView) view.findViewById(R.id.year);
        NumericWheelAdapter numericWheelAdapter1 = new NumericWheelAdapter(this, 1950, norYear);
        numericWheelAdapter1.setLabel("年");
        year.setViewAdapter(numericWheelAdapter1);
        year.setCyclic(false);//是否可循环滑动
        year.addScrollingListener(scrollListener);

        month = (WheelView) view.findViewById(R.id.month);
        NumericWheelAdapter numericWheelAdapter2 = new NumericWheelAdapter(this, 1, 12, "%02d");
        numericWheelAdapter2.setLabel("月");
        month.setViewAdapter(numericWheelAdapter2);
        month.setCyclic(false);
        month.addScrollingListener(scrollListener);

        day = (WheelView) view.findViewById(R.id.day);
        initDay(curYear, curMonth);
        day.setCyclic(false);

        year.setVisibleItems(7);//设置显示行数
        month.setVisibleItems(7);
        day.setVisibleItems(7);

        year.setCurrentItem(curYear - 1950);
        month.setCurrentItem(curMonth - 1);
        day.setCurrentItem(curDate - 1);


        return view;
    }

    OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {

        }

        @Override
        public void onScrollingFinished(WheelView wheel) {
            int n_year = year.getCurrentItem() + 1950;//年
            int n_month = month.getCurrentItem() + 1;//月

            initDay(n_year, n_month);

            String birthday = new StringBuilder().append((year.getCurrentItem() + 1950)).append("-").append((month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1) : (month.getCurrentItem() + 1)).append("-").append(((day.getCurrentItem() + 1) < 10) ? "0" + (day.getCurrentItem() + 1) : (day.getCurrentItem() + 1)).toString();
            tv1.setText("年龄             " + calculateDatePoor(birthday) + "岁");
        }
    };

    private void initDay(int arg1, int arg2) {
        NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(this, 1, getDay(arg1, arg2), "%02d");
        numericWheelAdapter.setLabel("日");
        day.setViewAdapter(numericWheelAdapter);
    }

    private String date = "1996-01-01";

    /**
     * 根据日期计算年龄
     *
     * @param birthday
     * @return
     */
    public final String calculateDatePoor(String birthday) {
        try {

            if (TextUtils.isEmpty(birthday)) {
                return "0";
            }

            date = birthday;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date birthdayDate = sdf.parse(birthday);
            String currTimeStr = sdf.format(new Date());
            Date currDate = sdf.parse(currTimeStr);
            if (birthdayDate.getTime() > currDate.getTime()) {
                return "0";
            }
            long age = (currDate.getTime() - birthdayDate.getTime())
                    / (24 * 60 * 60 * 1000) + 1;
            String year = new DecimalFormat("0.00").format(age / 365f);
            if (TextUtils.isEmpty(year)) {
                return "0";
            }
            return String.valueOf(new Double(year).intValue());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "0";
    }

    /**
     * @param year
     * @param month
     * @return
     */
    private int getDay(int year, int month) {
        int day = 30;
        boolean flag = false;
        switch (year % 4) {
            case 0:
                flag = true;
                break;
            default:
                flag = false;
                break;
        }
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                day = 31;
                break;
            case 2:
                day = flag ? 29 : 28;
                break;
            default:
                day = 30;
                break;
        }
        return day;
    }


}
