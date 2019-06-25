/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.cloopen.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.yuntongxun.ecdemo.ui.chatting.model;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.utils.DensityUtil;
import com.yuntongxun.ecdemo.ui.chatting.ChattingActivity;
import com.yuntongxun.ecdemo.ui.chatting.holder.BaseHolder;
import com.yuntongxun.ecdemo.ui.chatting.holder.DescriptionViewHolder;
import com.yuntongxun.ecdemo.ui.chatting.view.CCPChattingFooter2;
import com.yuntongxun.ecdemo.ui.chatting.view.ChattingItemContainer;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.im.ECCallMessageBody;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * <p>收到文字row</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: Beijing Speedtong Information Technology Co.,Ltd</p>
 * @author Jorstin Chan
 * @date 2014-4-17
 * @version 1.0
 *
 *
 */
public class DescriptionRxRow extends BaseChattingRow {

	
	public DescriptionRxRow(int type){
		super(type);
	}
	
	@Override
	public View buildChatView(LayoutInflater inflater, View convertView) {
        //we have a don't have a converView so we'll have to create a new one
        if (convertView == null ) {
            convertView = new ChattingItemContainer(inflater, R.layout.chatting_item_from);

            
            //use the view holder pattern to save of already looked up subviews
            DescriptionViewHolder holder = new DescriptionViewHolder(mRowType);
            convertView.setTag(holder.initBaseHolder(convertView, true));
        } 
		return convertView;
	}

	@Override
	public void buildChattingData(final Context context, BaseHolder baseHolder,
			ECMessage detail, int position) {

		DescriptionViewHolder holder = (DescriptionViewHolder) baseHolder;
		ECMessage message = detail;
		if(message != null) {
			if (message.getType() == ECMessage.Type.TXT) {
				String msgType="";
				JSONArray jsonArray=null;
				if (!TextUtils.isEmpty(message.getUserData())) {
                    try {
                        JSONObject jsonObject = new JSONObject(message.getUserData());
                        msgType = jsonObject.getString(CCPChattingFooter2.TXT_MSGTYPE);
                        jsonArray = jsonObject.getJSONArray(CCPChattingFooter2.MSG_DATA);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
				if (TextUtils.equals(msgType, CCPChattingFooter2.FACETYPE)) {
					holder.getDescTextView().setBackgroundResource(0);
				} else {
//					holder.getDescTextView().setBackgroundResource(R.drawable.chat_qipao_white);
					holder.getDescTextView().setPadding(DensityUtil.dip2px(10),DensityUtil.dip2px(10)
							,DensityUtil.dip2px(10),DensityUtil.dip2px(10));
				}
				ECTextMessageBody textBody = (ECTextMessageBody) message.getBody();
				String msgTextString =textBody.getMessage();
				holder.getDescTextView().setMovementMethod(LinkMovementMethod.getInstance());


				if(message.getForm().startsWith("~ytxro")){
					String[]arr = msgTextString.split("\\,");
					final String[] arr2 = new String[arr.length-1];
					msgTextString = arr[0];
					for(int i=0;i<arr.length-1;i++){
							arr2[i] = arr[i+1];
					}
					UserAdapter adapter = new UserAdapter(CCPAppManager.getContext(),R.id.tv,arr2);
					holder.listView.setVisibility(View.VISIBLE);
					holder.listView.setAdapter(adapter);

					holder.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
							((ChattingActivity) context).mChattingFragment.handleSendTextMessage(arr2[i]);

						}
					});

					adapter.notifyDataSetChanged();
				}
				holder.getDescTextView().showMessage(message.getId() + "", msgTextString, msgType, jsonArray);
				View.OnClickListener onClickListener = ((ChattingActivity) context).mChattingFragment.getChattingAdapter().getOnClickListener();
				ViewHolderTag holderTag = ViewHolderTag.createTag(message,
						ViewHolderTag.TagType.TAG_IM_TEXT, position);
				holder.getDescTextView().setTag(holderTag);
				holder.getDescTextView().setOnClickListener(onClickListener);
			} else if (message.getType() == ECMessage.Type.CALL) {
				ECCallMessageBody textBody = (ECCallMessageBody) message.getBody();
				holder.getDescTextView().setMovementMethod(LinkMovementMethod.getInstance());

				holder.getDescTextView().setText(textBody.getCallText());
			}
		}
	}


	class UserAdapter extends ArrayAdapter<String> {
		private int mResourceId;

		public String[] a ;

		public UserAdapter(Context context, int textViewResourceId,
						   String[] users) {
			super(context, textViewResourceId, users);
			this.a = users;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {


			View view = View.inflate(CCPAppManager.getContext(),R.layout.ac, null);

			TextView text = (TextView) view.findViewById(R.id.tv);
			text.setText(a[position]);
			text.setTextColor(getContext().getResources().getColor(R.color.blue_titlebar_color));
			return view;
		}
	}


	@Override
	public int getChatViewType() {

		return ChattingRowType.DESCRIPTION_ROW_RECEIVED.ordinal();
	}

	@Override
	public boolean onCreateRowContextMenu(ContextMenu contextMenu,
			View targetView, ECMessage detail) {

		return false;
	}
}
