
package com.yuntongxun.ecdemo.ui;

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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yuntongxun.ecdemo.AvatorUtil;
import com.yuntongxun.ecdemo.ECApplication;
import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.ImageLoader;
import com.yuntongxun.ecdemo.common.utils.LogUtil;
import com.yuntongxun.ecdemo.common.utils.PatchMgr;
import com.yuntongxun.ecdemo.common.utils.ToastUtil;
import com.yuntongxun.ecdemo.common.view.SearchEditText;
import com.yuntongxun.ecdemo.common.view.TitleBar;
import com.yuntongxun.ecdemo.core.ContactsCache;
import com.yuntongxun.ecdemo.core.comparator.PyComparator;
import com.yuntongxun.ecdemo.storage.ContactSqlManager;
import com.yuntongxun.ecdemo.storage.FriendMessageSqlManager;
import com.yuntongxun.ecdemo.storage.GroupMemberSqlManager;
import com.yuntongxun.ecdemo.ui.chatting.base.EmojiconTextView;
import com.yuntongxun.ecdemo.ui.contact.BladeView;
import com.yuntongxun.ecdemo.ui.contact.ContactDetailActivity;
import com.yuntongxun.ecdemo.ui.contact.ContactLogic;
import com.yuntongxun.ecdemo.ui.contact.CustomSectionIndexer;
import com.yuntongxun.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecdemo.ui.contact.NewFrendAct;
import com.yuntongxun.ecdemo.ui.contact.PinnedHeaderListView;
import com.yuntongxun.ecdemo.ui.group.DiscussionAct;
import com.yuntongxun.ecdemo.ui.group.GroupAct;
import com.yuntongxun.ecdemo.ui.personcenter.FriendInfoUI;
import com.yuntongxun.ecdemo.ui.settings.EditConfigureActivity;
import com.yuntongxun.ecsdk.im.ECGroupMember;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;

import static android.app.Activity.RESULT_OK;
import static com.tencent.bugly.crashreport.inner.InnerAPI.context;
import static com.yuntongxun.ecdemo.R.id.avatar_iv;
import static com.yuntongxun.ecdemo.ui.group.GroupMemberControlAct.VALUE_TRANS_OWNER;

/**
 * Created by zlk on 2017/7/25.
 * 群组设置
 * 通过type区分
 */

public class GagMembesFragment extends LazyFrament {
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

    public static final int TYPE_SET_MANAGER = 5;//设置管理员和转让群主
    private ArrayList<ECContacts> managers;
    private HeadAdapter headAdapter;
    private boolean isLocalDiscussion;

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
    private List<ECContacts> contacts = new ArrayList<ECContacts>();
    private List<ECContacts> originalData = new ArrayList<ECContacts>();
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
            Intent intent = new Intent(GagMembesFragment.this.getActivity(), EditConfigureActivity.class);
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

            ECContacts contacts = mAdapter.getItem(_position);

            if (mAdapter == null || mAdapter.getItem(_position) == null) {
                return;
            }
            if (mType == TYPE_SELECT) {
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
            } else if (mType == TYPE_SET_MANAGER ||mType == VALUE_TRANS_OWNER ) {// 设置管理员,转让群主
                if (lisener != null) {
                    lisener.onSelectedEcontact(contacts);
                }
                return;
            }

            toNextFriendInfoAct(contacts);

        }
    };


    public static GagMembesFragment newInstance() {
        GagMembesFragment fragment = new GagMembesFragment();
        return fragment;
    }

    public static GagMembesFragment newInstance(int type) {
        GagMembesFragment f = new GagMembesFragment();
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
        titleBar.setMyCenterTitle("通讯录");
        mType = getArguments() != null ? getArguments().getInt("type") : TYPE_NORMAL;
        if (positions == null) {
            positions = new ArrayList<Integer>();
        }
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

        searchView.setOnSearchClickListener(new SearchEditText.OnSearchClickListener() {
            @Override
            public void onSearchClick(String  s) {

                if (!TextUtils.isEmpty(s)){
                    doFilter(s);
                }else{
                    mAdapter.setData(contacts, mCustomSectionIndexer);

                    //管理员
                    if(mType == TYPE_GROUP_MEMBERS&&managers!=null){
                        headAdapter.setData(managers);
                    }

                }
            }
        });
    }

    // 双向过滤
    public void doFilter(CharSequence s) {
        final String keyword = s.toString().toLowerCase();
        //成员
        final List<ECContacts> matchContactses = PatchMgr.search(keyword, contacts);
        ECHandlerHelper.postRunnOnUI(new Runnable() {
            @Override
            public void run() {
                if (!matchContactses.isEmpty()) {
                    mAdapter.setData(matchContactses, mCustomSectionIndexer);
                }else {
                    mAdapter.clear();
                }
            }
        });

        //管理员
        if(mType == TYPE_GROUP_MEMBERS){
            final List<ECContacts> matchManagers = PatchMgr.search(keyword, managers);
            ECHandlerHelper.postRunnOnUI(new Runnable() {
                @Override
                public void run() {
                    if (!matchManagers.isEmpty()) {
                        headAdapter.setData(matchManagers);
                    }else {
                        headAdapter.clear();
                    }
                }
            });
            if (matchContactses.isEmpty()&&matchManagers.isEmpty()){
                mListView.setVisibility(View.GONE);
                mLetterListView.setVisibility(View.GONE);
                empty.setVisibility(View.VISIBLE);
            }else{
                mListView.setVisibility(View.VISIBLE);
                mLetterListView.setVisibility(View.VISIBLE);
                empty.setVisibility(View.GONE);
            }
        }else{
            if (matchContactses.isEmpty()){
                mListView.setVisibility(View.GONE);
                mLetterListView.setVisibility(View.GONE);
                empty.setVisibility(View.VISIBLE);
            }else{
                mListView.setVisibility(View.VISIBLE);
                mLetterListView.setVisibility(View.VISIBLE);
                empty.setVisibility(View.GONE);
            }
        }




    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
        if (positions.size() == 0) {
            return "";
        }
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
        if (positions.size() == 0) {
            return "";
        }
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
        if (mType == TYPE_GROUP_MEMBERS) {
            managers = getmManagers();
            contacts.removeAll(managers);
        }

        if (mListView != null && mSelectCardItem != null) {
            mListView.removeHeaderView(mSelectCardItem);
            mListView.setAdapter(null);
        }
        if (contacts == null) {
            return;
        }
        counts = new int[sections.length];
        for (ECContacts c : contacts) {

            String firstCharacter = c.getSortKey();
            int index = ALL_CHARACTER.indexOf(firstCharacter);
            if (index == -1) {
                continue;
            }
            counts[index]++;
        }
        if (contacts != null && !contacts.isEmpty()) {
            mSortKey = contacts.get(0).getSortKey();
        }
        mCustomSectionIndexer = new CustomSectionIndexer(sections, counts);
        if (mType == TYPE_NORMAL) {//添加头布局
            addHeadView();
        } else if (mType == TYPE_GROUP_MEMBERS) {//群成员
            addHeadViewMemvers();
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

    //群组成员头布局
    private void addHeadViewMemvers() {
        View headView = View.inflate(getActivity(), R.layout.members_head, null);
        if(isLocalDiscussion){
            ((TextView) headView.findViewById(R.id.tv_manager_count)).setText("讨论组创建者（" + managers.size() + "人）");
        }else{
            ((TextView) headView.findViewById(R.id.tv_manager_count)).setText("群主、管理员（" + managers.size() + "人）");
        }

        ListView lvMerbers = (ListView) headView.findViewById(R.id.lv_manager);

        headAdapter = new HeadAdapter(getActivity());
        lvMerbers.setAdapter(headAdapter);
        headAdapter.setData(managers);

        HeaderItemClickListener clickListener = new HeaderItemClickListener();
        lvMerbers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ECContacts contacts = headAdapter.getItem(position);
                toNextFriendInfoAct(contacts);
            }
        });

        mListView.addHeaderView(headView);

    }

    private void toNextFriendInfoAct(ECContacts contacts) {

        if (contacts == null || contacts.getId() == -1) {
            ToastUtil.showMessage(R.string.contact_none);
            return;
        }
        Intent intent = new Intent(getActivity(), FriendInfoUI.class);
        intent.putExtra(ContactDetailActivity.MOBILE, contacts.getContactid());
        intent.putExtra(ContactDetailActivity.DISPLAY_NAME, contacts.getNickname());
        startActivity(intent);
    }

    private ArrayList<ECContacts> getmManagers() {
        ArrayList<ECContacts> managers = new ArrayList<>();

        for (ECContacts contact : contacts) {
            if (contact.getRole() == ECGroupMember.Role.OWNER) {
                managers.add(0, contact);
            } else if (contact.getRole() == ECGroupMember.Role.MANAGER) {
                managers.add(contact);
            }
        }
        return managers;
    }

    //联系人头布局
    private void addHeadView() {
        View headView = View.inflate(getActivity(), R.layout.contact_head, null);
        HeaderItemClickListener clickListener = new HeaderItemClickListener();
        headView.findViewById(R.id.tv_head_newfriend).setOnClickListener(clickListener);
        headView.findViewById(R.id.tv_head_group).setOnClickListener(clickListener);
        headView.findViewById(R.id.tv_head_discussion).setOnClickListener(clickListener);
        mListView.addHeaderView(headView);
    }

    public void setData(ArrayList<ECGroupMember> members) {


        for (int i = 0; i < members.size(); i++) {
            ECGroupMember ecGroupMember = members.get(i);
            ECContacts contact = new ECContacts();
            contact.setContactid(ecGroupMember.getVoipAccount());
            contact.setNickname(ecGroupMember.getDisplayName());
            contact.setRole(ecGroupMember.getMemberRole());

            ContactLogic.pyFormat(contact);
            contacts.add(contact);
        }
        originalData = contacts;

        Collections.sort((List) contacts, new PyComparator());
    }

    public void setData(ArrayList<ECGroupMember> members,boolean isLocalDiscussion) {
        this.isLocalDiscussion = isLocalDiscussion;

        for (int i = 0; i < members.size(); i++) {
            ECGroupMember ecGroupMember = members.get(i);
            ECContacts contact = new ECContacts();
            contact.setContactid(ecGroupMember.getVoipAccount());
            contact.setNickname(ecGroupMember.getDisplayName());
            contact.setRole(ecGroupMember.getMemberRole());

            ContactLogic.pyFormat(contact);
            contacts.add(contact);
        }
        originalData = contacts;

        Collections.sort((List) contacts, new PyComparator());
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
                    startActivity(GroupAct.class);
                    break;
                case R.id.tv_head_discussion:

                    startActivity(DiscussionAct.class);

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
            CCPAppManager.startChattingAction(GagMembesFragment.this.getActivity(), result_data, result_data, true);
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


    private OnSelectedEcontactLisener lisener;

    public void setLisener(OnSelectedEcontactLisener lisener) {
        this.lisener = lisener;
    }

    public interface OnSelectedEcontactLisener {
        void onSelectedEcontact(ECContacts ecContact);
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
            mIndexer = indexer;
            setNotifyOnChange(false);
            clear();
            setNotifyOnChange(true);
            if (data != null) {
                for (ECContacts appEntry : data) {
                    add(appEntry);
                }
            }

            mListView.setVisibility(View.VISIBLE);
            mLetterListView.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view;
            ViewHolder mViewHolder;
            if (convertView == null || convertView.getTag() == null) {
                view = View.inflate(mContext, R.layout.mobile_contact_list_item, null);

                mViewHolder = new ViewHolder();
                mViewHolder.mAvatar = (ImageView) view.findViewById(avatar_iv);
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
                    String sesitionStr = contacts.getSortKey();
                    if (TextUtils.equals("&", contacts.getSortKey())) {
                        sesitionStr = "群主、管理员";
                    }
                    mViewHolder.tvCatalog.setText(sesitionStr);
                } else {
                    mViewHolder.tvCatalog.setVisibility(View.GONE);
                }
                String remark = contacts.getRemark();
                //String remark = ContactLogic.CONVER_PHONTO[ECSDKUtils.getIntRandom(4, 0)];
//                mViewHolder.mAvatar.setImageBitmap(ContactLogic.getPhoto(/*contacts.getRemark()*/remark));
                mViewHolder.name_tv.setText(contacts.getNickname());
                mViewHolder.account.setText(contacts.getContactid());


                String headUrl = FriendMessageSqlManager.queryURLByID(contacts.getContactid());
                if (!TextUtils.isEmpty(headUrl)) {
                    ImageLoader.getInstance().displayCricleImage(context,headUrl, mViewHolder.mAvatar);
                } else {
                    mViewHolder.mAvatar.setImageResource(R.drawable.addressbook_header);
                }

                if (mType != TYPE_NORMAL
                        && mType != TYPE_SET_MANAGER
                        && mType != TYPE_GROUP_MEMBERS) {
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
                String sesitionStr = item.getSortKey();
                if (TextUtils.equals("&", item.getSortKey())) {
                    sesitionStr = "群主、管理员";
                }

                headView.setText(sesitionStr);
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


    class HeadAdapter extends ArrayAdapter<ECContacts> {
        CustomSectionIndexer mIndexer;
        Context mContext;
        private int mLocationPosition = -1;

        public HeadAdapter(Context context) {
            super(context, 0);
            mContext = context;
        }


        public void setData(List<ECContacts> data) {
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
                view = View.inflate(mContext, R.layout.members_head_list_item, null);

                mViewHolder = new ViewHolder();
                mViewHolder.mAvatar = (ImageView) view.findViewById(avatar_iv);
                mViewHolder.name_tv = (EmojiconTextView) view.findViewById(R.id.name_tv);
                mViewHolder.account = (TextView) view.findViewById(R.id.account);
                mViewHolder.tvRoal = (TextView) view.findViewById(R.id.tv_roal);

                view.setTag(mViewHolder);
            } else {
                view = convertView;
                mViewHolder = (ViewHolder) view.getTag();
            }

            ECContacts contacts = getItem(position);
            if (contacts != null) {

                mViewHolder.name_tv.setText(AvatorUtil.getInstance().getMarkNameByGroup(GroupMemberSqlManager.getGroupIdByVoipAcount(contacts.getContactid()),contacts.getContactid()));
                mViewHolder.account.setText(contacts.getContactid());
                if (contacts.getRole().equals(ECGroupMember.Role.OWNER)) {
                    if(isLocalDiscussion){
                        mViewHolder.tvRoal.setText("创建者");
                    }else{
                        mViewHolder.tvRoal.setText("群主");
                    }

                    mViewHolder.tvRoal.setBackgroundResource(R.drawable.yellow_rect_bg);
                } else if (contacts.getRole().equals(ECGroupMember.Role.MANAGER)) {
                    mViewHolder.tvRoal.setText("管理员");
                    mViewHolder.tvRoal.setBackgroundResource(R.drawable.green_rect_bg);
                }

                String headUrl = FriendMessageSqlManager.queryURLByID(contacts.getContactid());
                if (!TextUtils.isEmpty(headUrl)) {
                    ImageLoader.getInstance().displayCricleImage(context,headUrl, mViewHolder.mAvatar);
                } else {
                    if(CCPAppManager.getUserId().equalsIgnoreCase(contacts.getContactid())
                    &&!TextUtils.isEmpty(ECApplication.photoUrl)){
                        ImageLoader.getInstance().displayCricleImage(getActivity(),ECApplication.photoUrl, mViewHolder.mAvatar);
                    }else {
                        ImageLoader.getInstance().displayCricleImage(getActivity(),R.drawable.addressbook_header,mViewHolder.mAvatar);
                    }

                }
            }

            return view;
        }
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
         * 账号
         */
        TextView tvRoal;


    }

}
