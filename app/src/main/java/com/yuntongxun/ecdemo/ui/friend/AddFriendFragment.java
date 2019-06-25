package com.yuntongxun.ecdemo.ui.friend;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.DateUtil;
import com.yuntongxun.ecdemo.common.utils.DemoUtils;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.PatchMgr;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.SearchEditText;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.core.ContactsCache;
import com.yuntongxun.ecdemo.storage.ContactSqlManager;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecdemo.ui.LazyFrament;
import com.yuntongxun.ecdemo.ui.RestServerDefines;
import com.yuntongxun.ecdemo.ui.chatting.base.EmojiconTextView;
import com.yuntongxun.ecdemo.ui.contact.BladeView;
import com.yuntongxun.ecdemo.ui.contact.ContactDetailActivity;
import com.yuntongxun.ecdemo.ui.contact.CustomSectionIndexer;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecdemo.ui.contact.MobileContactSelectActivity;
import com.yuntongxun.ecdemo.ui.contact.PinnedHeaderListView;
import com.yuntongxun.ecdemo.ui.personcenter.FriendInfoUI;
import com.yuntongxun.ecdemo.ui.phonemodel.HttpMethods;
import com.yuntongxun.ecdemo.ui.settings.EditConfigureActivity;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
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
 * 添加好友
 */
public class AddFriendFragment extends LazyFrament {
    @BindView(R.id.address_contactlist)
    PinnedHeaderListView mListView;
    @BindView(R.id.mLetterListView)
    BladeView mLetterListView;
    @BindView(R.id.loading_tips_area)
    LinearLayout loadingTipsArea;

    @BindView(R.id.search)
    SearchEditText searchView;


    /**
     * 当前联系人列表类型（查看、联系人选择）
     */
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_SELECT = 2;
    @BindView(R.id.title_bar)
    TitleBar mTitleBar;


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
    private OnContactClickListener mClickListener;
    /**
     * 每个姓氏第一次出现对应的position
     */
    private int[] counts;
    private String mSortKey = "#";
    private CustomSectionIndexer mCustomSectionIndexer;
    private ContactsAdapter mAdapter;





    public interface OnContactClickListener {
        void onContactClick(int count);

        void onSelectGroupClick();


    }


    /**
     * 选择联系人
     */
    private View mSelectCardItem;


    final private View.OnClickListener mSelectClickListener
            = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AddFriendFragment.this.getActivity(), EditConfigureActivity.class);
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


    public static AddFriendFragment newInstance() {
        AddFriendFragment fragment = new AddFriendFragment();
        return fragment;
    }

    public static AddFriendFragment newInstance(int type) {
        AddFriendFragment f = new AddFriendFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        f.setArguments(args);
        return f;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mobile_contacts_activity;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {

        mType = getArguments() != null ? getArguments().getInt("type") : TYPE_NORMAL;
        if (positions == null) {
            positions = new ArrayList<Integer>();
        }

        mTitleBar.setVisibility(View.GONE);

    }

    @Override
    protected void initWidgetActions() {
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

        // TODO: 2017/11/7 改成sql查询
        searchView.setOnSearchClickListener(new SearchEditText.OnSearchClickListener() {
            @Override
            public void onSearchClick(String s) {

                if (!TextUtils.isEmpty(s)) {
                    doFilter(s);
                } else {
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
            mClickListener = (OnContactClickListener) activity;
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

    public String getChatuserName() {
        StringBuilder selectUser = new StringBuilder();
        for (Integer position : positions) {
            ECContacts item = mAdapter.getItem(position);
            ContactSqlManager.insertContact(item);
            if (item != null) {
                selectUser.append(item.getNickname()).append(",");
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
        registerReceiver(new String[]{ContactsCache.ACTION_ACCOUT_INIT_CONTACTS});
        if (mListView != null) {
            mListView.setAdapter(null);
        }
        mListView.setOnItemClickListener(onItemClickListener);
        initContactListView();
    }

    /**
     * 初始化联系人列表
     */
    private void initContactListView() {
        if (mListView != null && mSelectCardItem != null) {
            mListView.removeHeaderView(mSelectCardItem);
            mListView.setAdapter(null);
        }
        contacts = ContactsCache.getInstance().getContacts();
        if (contacts == null) {
            return;
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
        if (mType == TYPE_NORMAL) {//添加头布局
//            addHeadView();
        }
        mAdapter = new ContactsAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mAdapter.setData(contacts, mCustomSectionIndexer);
        mListView.setOnScrollListener(mAdapter);
        //設置頂部固定頭部
        mListView.setPinnedHeaderView(LayoutInflater.from(getActivity()).inflate(
                R.layout.header_item_cator, mListView, false));
        loadingTipsArea.setVisibility(View.GONE);
    }


    protected class HeaderItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_head_newfriend:
                    break;
                case R.id.tv_head_group:
                    break;
                case R.id.tv_head_discussion:

                    break;
                default:
                    break;
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        showLetter(mLetterListView);
    }

    @Override
    public void fetchData() {
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
            CCPAppManager.startChattingAction(AddFriendFragment.this.getActivity(), result_data, result_data, true);
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
            initContactListView();
        }
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
            setNotifyOnChange(true);
            mIndexer = indexer;
            clear();
            if (data != null) {
                addAll(data);
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder mViewHolder;

            if (position <= contacts.size()) {
                final ECContacts item = getItem(position);

                String contactId = item.getContactid();
                if (convertView == null || convertView.getTag() == null) {
                    view = View.inflate(mContext, R.layout.mobile_contact_list_item_addfriend, null);

                    mViewHolder = new ViewHolder();
                    mViewHolder.mAvatar = (ImageView) view.findViewById(R.id.avatar_iv);
                    mViewHolder.name_tv = (EmojiconTextView) view.findViewById(R.id.name_tv);
                    mViewHolder.account = (TextView) view.findViewById(R.id.account);
                    mViewHolder.checkBox = (CheckBox) view.findViewById(R.id.contactitem_select_cb);
                    mViewHolder.tvCatalog = (TextView) view.findViewById(R.id.contactitem_catalog);

                    mViewHolder.bu = (Button) view.findViewById(R.id.bu_addfriend);

                    view.setTag(mViewHolder);
                } else {
                    view = convertView;
                    mViewHolder = (ViewHolder) view.getTag();
                }

                boolean isFriend = FriendMessageSqlManager.isFriend(contactId);

                mViewHolder.bu.setText(isFriend ? "已添加" : "添加");
                if(isFriend){
                    mViewHolder.bu.setTextColor(getActivity().getResources().getColor(R.color.hint_text_color_dark_bg));
                    mViewHolder.bu.setBackground(null);
                    mViewHolder.bu.setOnClickListener(null);
                }else {
                    mViewHolder.bu.setTextColor(getActivity().getResources().getColor(R.color.white));
                    mViewHolder.bu.setBackground(getActivity().getResources().getDrawable(R.drawable.blue_rect_bg));
                    mViewHolder.bu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestAddFriend(item);
                        }
                    });

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
                    mViewHolder.name_tv.setText(contacts.getNickname());
                    mViewHolder.account.setText(contacts.getContactid());

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
                }
                return view;
            }
            return null;
        }

        private void requestAddFriend(ECContacts item) {
            if (item == null) {
                return;
            }
            ((AddressBookListAct) getActivity()).showCommonProcessDialog();
            Observer<Object> subscriber = new Observer<Object>() {
                @Override
                public void onComplete() {
                    LogUtil.e("onCompleted");
                    ((AddressBookListAct) getActivity()).dismissCommonPostingDialog();
                }

                @Override
                public void onError(Throwable e) {
                    ToastUtil.showMessage("添加失败");
                    LogUtil.e(e.toString());
                    ((AddressBookListAct) getActivity()).dismissCommonPostingDialog();
                }

                @Override
                public void onSubscribe(Disposable d) {
                }

                @Override
                public void onNext(Object movieEntity) {
                    ((AddressBookListAct) getActivity()).dismissCommonPostingDialog();
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
                                ToastUtil.showMessage("添加成功");
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
            JSONObject map = HttpMethods.buildAddFriend(CCPAppManager.getUserId(), item.getContactid());
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), map.toString());
            HttpMethods.getInstance(time).addFriend(subscriber, RestServerDefines.APPKER, url, body);
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

            Button bu;

        }
    }


}