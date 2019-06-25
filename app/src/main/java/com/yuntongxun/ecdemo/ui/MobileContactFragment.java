
package com.yuntongxun.ecdemo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.GetImageUtils;
import com.yuntongxun.ecdemo.common.utils.ImageLoader;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.PatchMgr;
import com.yuntongxun.ecdemo.common.utils.SharedPreferencesUtils;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.SearchEditText;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.core.ContactsCache;
import com.yuntongxun.ecdemo.core.comparator.PyComparator;
import com.yuntongxun.ecdemo.pojo.Friend;
import com.yuntongxun.ecdemo.storage.ContactSqlManager;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecdemo.ui.chatting.base.EmojiconTextView;
import com.yuntongxun.ecdemo.ui.contact.BladeView;
import com.yuntongxun.ecdemo.ui.contact.ContactDetailActivity;
import com.yuntongxun.ecdemo.ui.contact.ContactLogic;
import com.yuntongxun.ecdemo.ui.contact.CustomSectionIndexer;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecdemo.ui.contact.MobileContactSelectActivity;
import com.yuntongxun.ecdemo.ui.contact.NewFrendAct;
import com.yuntongxun.ecdemo.ui.contact.PinnedHeaderListView;
import com.yuntongxun.ecdemo.ui.friend.AddFriendActivity;
import com.yuntongxun.ecdemo.ui.group.DiscussionAct;
import com.yuntongxun.ecdemo.ui.group.GroupRouterUI;
import com.yuntongxun.ecdemo.ui.personcenter.FriendInfoUI;
import com.yuntongxun.ecdemo.ui.phonemodel.HttpMethods;
import com.yuntongxun.ecdemo.ui.settings.EditConfigureActivity;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static android.app.Activity.RESULT_OK;

/**
 * onresume 获取好友信息实缓存
 */

public class MobileContactFragment extends LazyFrament {
    private static final java.lang.String TAG = "MobileContactFragment";
    @BindView(R.id.address_contactlist)
    PinnedHeaderListView mListView;
    @BindView(R.id.mLetterListView)
    BladeView mLetterListView;
    @BindView(R.id.loading_tips_area)
    LinearLayout loadingTipsArea;
    @BindView(R.id.title_bar)
    TitleBar titleBar;
    @BindView(R.id.search)
    SearchEditText searchView;

    @BindView(R.id.empty)
    TextView empty;


    /**
     * 当前联系人列表类型（查看、联系人选择）
     */
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_SELECT = 2;
    public static final int TYPE_GROUP_MEMBERS = 3;


    public TitleBar getTitleBar() {
        return titleBar;
    }


    /**
     * 列表类型
     */
    private int mType;
    private String[] sections = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
            "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
            "Y", "Z", "#"};
    private static final String ALL_CHARACTER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
    /**
     * 每个字母最开始的位置
     */
    private HashMap<String, Integer> mFirstLetters;
    /**
     * 当前选择联系人位置
     */
    public static ArrayList<Integer> positions = new ArrayList<Integer>();
    /**
     * 每个首字母对应的position
     */
    private String[] mLetterPos;
    private List<ECContacts> contacts;
    private ContactListFragment.OnContactClickListener mClickListener;
    /**
     * 每个姓氏第一次出现对应的position
     */
    private int[] counts;
    private String mSortKey = "#";
    private CustomSectionIndexer mCustomSectionIndexer;
    private ContactsAdapter mAdapter;
    /**
     * 选择联系人
     */
    private View mSelectCardItem;


    final private View.OnClickListener mSelectClickListener
            = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MobileContactFragment.this.getActivity(), EditConfigureActivity.class);
            intent.putExtra(EditConfigureActivity.EXTRA_EDIT_TITLE, getString(R.string.edit_add_contacts));
            intent.putExtra(EditConfigureActivity.EXTRA_EDIT_HINT, getString(R.string.edit_add_contacts));
            startActivityForResult(intent, 0xa);
        }
    };

    // 设置联系人点击事件通知
    private final AdapterView.OnItemClickListener onItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            int headerViewsCount = mListView.getHeaderViewsCount();
            if (position < headerViewsCount) {
                return;
            }
            int _position = position - headerViewsCount;

            if (mAdapter == null || mAdapter.getItem(_position) == null) {
                return;
            }
            if (mType != TYPE_NORMAL) {
                // 如果是选择联系人模式
                Integer object = Integer.valueOf(_position);
                if (positions.contains(object)) {
                    positions.remove(object);
                } else {
                    positions.add(object);
                }
                notifyClick(positions.size());
                mAdapter.notifyDataSetChanged();
                return;
            }

            ECContacts contacts = mAdapter.getItem(_position);
            if (contacts == null || contacts.getId() == -1) {
                ToastUtil.showMessage(R.string.contact_none);
                return;
            }

            Intent intent = new Intent(getActivity(), FriendInfoUI.class);
            intent.putExtra(ContactDetailActivity.MOBILE, contacts.getContactid());
            intent.putExtra(ContactDetailActivity.DISPLAY_NAME, contacts.getNickname());
            startActivity(intent);
        }
    };


    public static MobileContactFragment newInstance() {
        MobileContactFragment fragment = new MobileContactFragment();
        return fragment;
    }

    public static MobileContactFragment newInstance(int type) {
        MobileContactFragment f = new MobileContactFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        f.setArguments(args);
        return f;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mobile_contacts_activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void initView(Bundle savedInstanceState) {
        titleBar.setMyCenterTitle("联系人");

        titleBar.setMySettingIcon(R.drawable.addressbook_navbtn_addfriend)
                .setSettingIconOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(AddFriendActivity.class);
                    }
                });
        contacts = new ArrayList<>();

        mType = getArguments() != null ? getArguments().getInt("type") : TYPE_NORMAL;
        if (positions == null) {
            positions = new ArrayList<Integer>();
        }


        mAdapter = new ContactsAdapter(getActivity());

    }

    @Override
    protected void initWidgetActions() {

        getFriends();


        showLetter(mLetterListView);
        mLetterListView.setOnItemClickListener(new BladeView.OnItemClickListener() {
            @Override
            public void onItemClick(String s) {
                if (s != null && ALL_CHARACTER != null && mCustomSectionIndexer != null) {
                    int section = ALL_CHARACTER.indexOf(s);
                    int position = mCustomSectionIndexer.getPositionForSection(section);
                    if (position != -1) {
                        if (position != 0) {
                            position = position + 1;
                        }
                        mListView.setSelection(position);
                    }
                }
            }
        });

        searchView.setOnSearchClickListener(new SearchEditText.OnSearchClickListener() {
            @Override
            public void onSearchClick(String s) {

                // TODO: 2017/11/7  改成sql查询
                if (contacts.isEmpty()) {
                    return;
                }
                if (!TextUtils.isEmpty(s)) {
                    doFilter(s);
                } else {
                    if(positions!=null){
                        positions.clear();
                    }
                    mAdapter.setData(contacts, mCustomSectionIndexer);
                }
            }
        });
    }

    public void doFilter(CharSequence s) {
        if (contacts.isEmpty()) {
            return;
        }
        final String keyword = s.toString().toLowerCase();

        final List<ECContacts> matchContactses = PatchMgr.search(keyword, contacts);
        ECHandlerHelper.postRunnOnUI(new Runnable() {
            @Override
            public void run() {
                if (!matchContactses.isEmpty()) {

                    mAdapter.setData(matchContactses, mCustomSectionIndexer);
                    positions.clear();
                } else {
                    mListView.setVisibility(View.GONE);
                    mLetterListView.setVisibility(View.GONE);
                    empty.setVisibility(View.VISIBLE);
                }
            }
        });

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof MobileContactSelectActivity) || mType == TYPE_NORMAL) {
            return;
        }
        try {
            mClickListener = (ContactListFragment.OnContactClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnContactClickListener");
        }
    }

    private void notifyClick(int count) {
        if (mClickListener != null) {
            mClickListener.onContactClick(count);
        }
    }

    /**
     * 选择的联系人
     */
    public String getChatuser() {
        StringBuilder selectUser = new StringBuilder();
        for (Integer position : positions) {
            ECContacts item = mAdapter.getItem(position);
            ContactSqlManager.insertContact(item);
            if (item != null) {
                selectUser.append(item.getContactid()).append(",");
            }
        }

        if (selectUser.length() > 0) {
            selectUser.substring(0, selectUser.length() - 1);
        }
        return selectUser.toString();
    }

    public ArrayList<ECContacts> getSelectedEcontacts() {
        ArrayList<ECContacts> contactsesList = new ArrayList<ECContacts>();
        for (Integer position : positions) {
            ECContacts item = mAdapter.getItem(position);
            ContactSqlManager.insertContact(item);
            if (item != null) {
                contactsesList.add(item);
            }
        }
        return contactsesList;
    }

    public String getChatuserName() {
        StringBuilder selectUser = new StringBuilder();
        for (Integer position : positions) {
            ECContacts item = mAdapter.getItem(position);
            ContactSqlManager.insertContact(item);
            if (item != null) {
                if (TextUtils.isEmpty(item.getNickname())) {
                    selectUser.append(item.getContactid()).append(",");
                } else {
                    selectUser.append(item.getNickname()).append(",");
                }
            }
        }

        if (selectUser.length() > 0) {
            selectUser.substring(0, selectUser.length() - 1);
        }
        return selectUser.toString();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerReceiver(new String[]{ContactsCache.ACTION_ACCOUT_INIT_CONTACTS, SDKCoreHelper.ACTION_SDK_FRIENDNUM});
        if (mListView != null) {
            mListView.setAdapter(null);
        }
        mListView.setOnItemClickListener(onItemClickListener);
        initContactListView();
    }


    private void getFriends() {

        Observer<Object> subscriber = new Observer<Object>() {
            @Override
            public void onComplete() {
                LogUtil.e("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                LogUtil.e(e.toString());
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
                        LogUtil.e("好友列表", "" + s);
                        String error = "";
                        JSONObject j = new JSONObject(s);
                        if (j != null && j.has("statusMsg")) {
                            error = j.getString("statusMsg");
                        }

                        if (DemoUtils.isTrue(s)) {
                            ArrayList<Friend> l = DemoUtils.getFriends(s);
                            if (l != null && l.size() > 0) {
                                for (Friend i : l) {
                                    if (i != null) {
                                        FriendMessageSqlManager.insertOrUpdateFriendByUserId(DemoUtils.splite(i.getUseracc())
                                                , i.getNickName(), i.getFriendState(), i.getAvatar(), i.getRemarkName());
                                    }
                                }
                            }
                        } else {
                        }
                        loadingTipsArea.setVisibility(View.GONE);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setListViewData();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        String time = DateUtil.formatNowDate(new Date());
        String url = getSig(time);
        JSONObject map = HttpMethods.buildGetFriends(CCPAppManager.getUserId(), "");
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
        HttpMethods.getInstance(time).getFriends(subscriber, RestServerDefines.APPKER, url, body);

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


    /**
     * 初始化联系人列表
     */
    private void initContactListView() {
        if (mListView != null && mSelectCardItem != null) {
            mListView.removeHeaderView(mSelectCardItem);
            mListView.setAdapter(null);
        }
//        contacts = ContactsCache.getInstance().getContacts();//通讯录
//        if (contacts == null){
//            return;
//        }
//
//
//
//        counts = new int[sections.length];
//        for (ECContacts c : contacts){
//            String firstCharacter = c.getSortKey();
//            int index = ALL_CHARACTER.indexOf(firstCharacter);
//            counts[index]++;
//        }
//        if (contacts != null && !contacts.isEmpty()) {
//            mSortKey = contacts.get(0).getSortKey();
//        }
//        mCustomSectionIndexer = new CustomSectionIndexer(sections, counts);
        if (mType == TYPE_NORMAL) {//添加头布局
            addHeadView();
        }
//        mAdapter = new ContactsAdapter(getActivity());
//        mListView.setAdapter(mAdapter);
//        mAdapter.setData(contacts, mCustomSectionIndexer);
//        mListView.setOnScrollListener(mAdapter);
//        //設置頂部固定頭部
//        mListView.setPinnedHeaderView(LayoutInflater.from(getActivity()).inflate(
//                R.layout.header_item_cator, mListView, false));
//        loadingTipsArea.setVisibility(View.GONE);
    }

    private TextView friendNum;

    private void addHeadView() {
        View headView = View.inflate(getActivity(), R.layout.contact_head, null);

        friendNum = (TextView) headView.findViewById(R.id.tv_friend_num);

        int num = (int) SharedPreferencesUtils.getParam(CCPAppManager.getContext(), SharedPreferencesUtils.FRIEND_NUM, -1);
        if (num > 0) {
            friendNum.setVisibility(View.VISIBLE);
            friendNum.setText(num + "");
        } else {
            friendNum.setVisibility(View.GONE);
        }

        HeaderItemClickListener clickListener = new HeaderItemClickListener();
        headView.findViewById(R.id.tv_head_newfriend).setOnClickListener(clickListener);
        headView.findViewById(R.id.tv_head_group).setOnClickListener(clickListener);
        headView.findViewById(R.id.tv_head_discussion).setOnClickListener(clickListener);
        headView.findViewById(R.id.tv_head_file_assistant).setOnClickListener(clickListener);


        if (mListView.getHeaderViewsCount() < 1) {
            mListView.addHeaderView(headView);
        }
    }

    protected class HeaderItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_head_newfriend:
                    // 新好友
                    startActivity(NewFrendAct.class);
                    break;
                case R.id.tv_head_group:
                    // 群组
                    startActivity(GroupRouterUI.class);
                    break;
                case R.id.tv_head_discussion:
                    startActivity(DiscussionAct.class);
                    break;
                case R.id.tv_head_file_assistant:
                    CCPAppManager.startChattingAction(getActivity(), RestServerDefines.FILE_ASSISTANT, "");
                    break;
                default:
                    break;
            }
        }

    }

    private boolean isAdded(String userId) {
        if (contacts != null && contacts.size() > 0) {
            for (ECContacts item : contacts) {
                if (item != null && userId.equalsIgnoreCase(item.getContactid())) {
                    return true;
                }
            }
        }
        return false;
    }


    private void setListViewData() {
        List<Friend> list = FriendMessageSqlManager.queryFriends();
        if (contacts != null) {
            contacts.clear();
        }

        if (list != null && list.size() > 0) {
            for (Friend it : list) {
                if (contacts != null) {
                    String contact_id = it.getUseracc();
                    ECContacts c = new ECContacts(contact_id);

                    c.setNickname(AvatorUtil.getInstance().getMarkName(contact_id));
                    c.setPhotoUrl(it.getAvatar());
                    ContactLogic.pyFormat(c);
                    contacts.add(c);
                }
            }
        }
        ECContacts self = new ECContacts(CCPAppManager.getUserId());
        self.setNickname(CCPAppManager.getClientUser().getUserName());
        ContactLogic.pyFormat(self);

        boolean isHas =  false;
        if(contacts!=null){
            for(ECContacts item:contacts){
                if(item!=null&&item.getContactid().equalsIgnoreCase(CCPAppManager.getUserId())){
                    isHas = true;
                }
            }
        }
        if(!isHas){
            contacts.add(self);
        }

        //排序
        if (contacts != null) {
            Collections.sort(contacts, new PyComparator());
        }

        counts = new int[sections.length];
        for (ECContacts c : contacts) {
            String firstCharacter = c.getSortKey();
            int index = ALL_CHARACTER.indexOf(firstCharacter);
            counts[index]++;
        }
        if (contacts != null && !contacts.isEmpty()) {
            mSortKey = contacts.get(0).getSortKey();
        }
        mCustomSectionIndexer = new CustomSectionIndexer(sections, counts);
        if (mAdapter != null && isVisible()) {
            if (isVisible()) {
                counts = new int[sections.length];
                for (ECContacts c : contacts) {
                    String firstCharacter = c.getSortKey();
                    int index = ALL_CHARACTER.indexOf(firstCharacter);
                    counts[index]++;
                }
                if (contacts != null && !contacts.isEmpty()) {
                    mSortKey = contacts.get(0).getSortKey();
                }
                mCustomSectionIndexer = new CustomSectionIndexer(sections, counts);
                mAdapter = new ContactsAdapter(getActivity());
                mAdapter.setData(contacts, mCustomSectionIndexer);
                mListView.setAdapter(mAdapter);
                mAdapter.setData(contacts, mCustomSectionIndexer);
                mListView.setOnScrollListener(mAdapter);

                if (contacts != null && contacts.size() < 1) {
                    View emptyView = getActivity().findViewById(R.id.empty_tip_tv);
                    emptyView.setVisibility(View.VISIBLE);
                    mListView.setEmptyView(emptyView);
                }
                mListView.setPinnedHeaderView(LayoutInflater.from(getActivity()).inflate(
                        R.layout.header_item_cator, mListView, false));
                loadingTipsArea.setVisibility(View.GONE);

                mAdapter.notifyDataSetChanged();
            }
        }


    }


    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void fetchData() {
        getFriends();
        showLetter(mLetterListView);

        LogUtil.d(TAG, "onresume");

        setListViewData();
        int num = FriendMessageSqlManager.getCount("0");
        if (friendNum != null) {
            if (num > 0) {
                friendNum.setVisibility(View.VISIBLE);
                friendNum.setText(num + "");
            } else {
                friendNum.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0xa) {
            if (data == null) {
                return;
            }
        } else if (resultCode != RESULT_OK) {
            LogUtil.d("onActivityResult: bail due to resultCode=" + resultCode);
            return;
        }
        if (requestCode == 0xa) {
            String result_data = data.getStringExtra("result_data");
            if (TextUtils.isEmpty(result_data) || result_data.trim().length() == 0) {
                ToastUtil.showMessage(R.string.mobile_list_empty);
                return;
            }
            CCPAppManager.startChattingAction(MobileContactFragment.this.getActivity(), result_data, result_data, true);
        }
    }


    private void showLetter(BladeView mLetterListView) {
        if (mLetterListView == null) {
            return;
        }
        boolean showBanView = (contacts != null && !contacts.isEmpty());
        mLetterListView.setVisibility(showBanView ? View.VISIBLE : View.GONE);

    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (positions != null) {
            positions.clear();
            positions = null;
        }
        if (mLetterListView != null) {
            mLetterListView.removeDis();
        }
    }

    @Override
    protected void handleReceiver(Context context, Intent intent) {
        super.handleReceiver(context, intent);
        if (ContactsCache.ACTION_ACCOUT_INIT_CONTACTS.equals(intent.getAction())) {
            LogUtil.d("handleReceiver ACTION_ACCOUT_INIT_CONTACTS");
        } else if (SDKCoreHelper.ACTION_SDK_FRIENDNUM.equalsIgnoreCase(intent.getAction())) {
            LogUtil.d("刷新联系人好友通知");
            if (friendNum != null) {
                int num = FriendMessageSqlManager.getCount("0");
                if (num > 0) {
                    friendNum.setVisibility(View.VISIBLE);
                    friendNum.setText(num + "");
                } else {
                    friendNum.setVisibility(View.GONE);
                }
            }
            setListViewData();
        }
    }


    //=============================== 以下为adapter===================================================
    class ContactsAdapter extends ArrayAdapter<ECContacts> implements PinnedHeaderListView.PinnedHeaderAdapter, AbsListView.OnScrollListener {
        CustomSectionIndexer mIndexer;
        Context mContext;
        private int mLocationPosition = -1;

        public ContactsAdapter(Context context) {
            super(context, 0);
            mContext = context;
        }


        public void setData(List<ECContacts> data, CustomSectionIndexer indexer) {
            if (!data.isEmpty()) {
                mListView.setVisibility(View.VISIBLE);
                mLetterListView.setVisibility(View.VISIBLE);
                empty.setVisibility(View.GONE);
            }
            mIndexer = indexer;
            setNotifyOnChange(false);
            clear();
            setNotifyOnChange(true);
            if (data != null) {
                for (ECContacts appEntry : data) {
                    add(appEntry);
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view;
            ViewHolder mViewHolder;
            if (convertView == null || convertView.getTag() == null) {
                view = View.inflate(mContext, R.layout.mobile_contact_list_item, null);

                mViewHolder = new ViewHolder();
                mViewHolder.mAvatar = (ImageView) view.findViewById(R.id.avatar_iv);
                mViewHolder.name_tv = (EmojiconTextView) view.findViewById(R.id.name_tv);
                mViewHolder.account = (TextView) view.findViewById(R.id.account);
                mViewHolder.checkBox = (CheckBox) view.findViewById(R.id.contactitem_select_cb);
                mViewHolder.tvCatalog = (TextView) view.findViewById(R.id.contactitem_catalog);
                view.setTag(mViewHolder);
            } else {
                view = convertView;
                mViewHolder = (ViewHolder) view.getTag();
            }

            ECContacts contacts = getItem(position);
            if (contacts != null) {
                int section = mIndexer.getSectionForPosition(position);
                if (mIndexer.getPositionForSection(section) == position) {
                    mViewHolder.tvCatalog.setVisibility(View.VISIBLE);
                    mViewHolder.tvCatalog.setText(contacts.getSortKey());
                } else {
                    mViewHolder.tvCatalog.setVisibility(View.GONE);
                }


                if (mType != TYPE_NORMAL) {
                    mViewHolder.checkBox.setVisibility(View.VISIBLE);
                    if (mViewHolder.checkBox.isEnabled() && positions != null) {
                        mViewHolder.checkBox.setChecked(positions.contains(position));
                    } else {
                        mViewHolder.checkBox.setChecked(false);
                    }
                } else {
                    mViewHolder.checkBox.setVisibility(View.GONE);
                }

                mViewHolder.name_tv.setText(AvatorUtil.getInstance().getMarkName(contacts.getContactid()));
                mViewHolder.account.setText(contacts.getContactid());

                String photo = contacts.getPhotoUrl();
                if (!TextUtils.isEmpty(photo)) {
                    ImageLoader.getInstance().displayCricleImage(
                            mContext, photo
                            , mViewHolder.mAvatar);
                } else {
                    if (CCPAppManager.getUserId().equalsIgnoreCase(contacts.getContactid())) {
                        if (!TextUtils.isEmpty(ECApplication.photoUrl)) {
                            ImageLoader.getInstance().displayCricleImage(
                                    mContext, ECApplication.photoUrl
                                    , mViewHolder.mAvatar);
                        } else {
                            if (GetImageUtils.isFileExist()) {
                                ImageLoader.getInstance().displayCricleImage(getActivity(), GetImageUtils.getPicFile(), mViewHolder.mAvatar);
                            } else {
                                ImageLoader.getInstance().displayCricleImage(getActivity(), R.drawable.header_woman, mViewHolder.mAvatar);
                            }
                        }
                    }
                }


            }

            return view;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (view instanceof PinnedHeaderListView) {
                ((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
            }
        }


        @Override
        public int getPinnedHeaderState(int position) {
            int realPosition = position - 1;
            if (realPosition < 0
                    || (mLocationPosition != -1 && mLocationPosition == realPosition)) {
                return PINNED_HEADER_GONE;
            }
            mLocationPosition = -1;
            int section = mIndexer.getSectionForPosition(realPosition);
            int nextSectionPosition = mIndexer.getPositionForSection(section + 1);
            if (nextSectionPosition != -1
                    && realPosition == nextSectionPosition - 1) {
                return PINNED_HEADER_PUSHED_UP;
            }
            return PINNED_HEADER_VISIBLE;
        }

        @Override
        public void configurePinnedHeader(View header, int position, int alpha) {
            int realPosition = position;
            int _position = position - 1;
            if (_position < 0) {
                return;
            }
            TextView headView = ((TextView) header.findViewById(R.id.contactitem_catalog));
            if (_position == 0) {
                headView.setText(mSortKey);
                return;
            }
            ECContacts item = getItem(_position);
            if (item != null) {
                headView.setText(item.getSortKey());
            }
               /* int section = mIndexer.getSectionForPosition(realPosition);
                String title = (String) mIndexer.getSections()[section];*/
        }

        class ViewHolder {
            /**
             * 头像
             */
            ImageView mAvatar;
            /**
             * 名称
             */
            EmojiconTextView name_tv;
            /**
             * 账号
             */
            TextView account;
            /**
             * 选择状态
             */
            CheckBox checkBox;
            TextView tvCatalog;

        }
    }


}
