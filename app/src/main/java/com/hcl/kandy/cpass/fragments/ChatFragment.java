package com.hcl.kandy.cpass.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.hcl.kandy.cpass.App;
import com.hcl.kandy.cpass.R;
import com.hcl.kandy.cpass.adapters.ChatAdapter;
import com.hcl.kandy.cpass.models.ChatModel;
import com.rbbn.cpaas.mobile.CPaaS;
import com.rbbn.cpaas.mobile.messaging.api.InboundMessage;
import com.rbbn.cpaas.mobile.messaging.api.MessagingCallback;
import com.rbbn.cpaas.mobile.messaging.api.OutboundMessage;
import com.rbbn.cpaas.mobile.messaging.chat.api.ChatConversation;
import com.rbbn.cpaas.mobile.messaging.chat.api.ChatGroupParticipant;
import com.rbbn.cpaas.mobile.messaging.chat.api.ChatListener;
import com.rbbn.cpaas.mobile.messaging.chat.api.ChatService;
import com.rbbn.cpaas.mobile.utilities.exception.MobileError;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ashish Goel on 2/1/2019.
 */
public class ChatFragment extends BaseFragment implements View.OnClickListener {

    private ChatService chatService;
    private EditText mEtDestination;
    private EditText mEtMessage;
    private RecyclerView mRecyclerView;
    private ChatAdapter mChatAdapter;
    private ArrayList<ChatModel> mChatModels;

    public ChatFragment() {

    }

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        if (context != null)
            initChatService(context);
        mChatModels = new ArrayList<>();
        mChatAdapter = new ChatAdapter(mChatModels);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_chat, container, false);
        View mBtnSendMessage = inflate.findViewById(R.id.btnStartChat);
        mEtDestination = inflate.findViewById(R.id.etDestainationAddress);
        mEtMessage = inflate.findViewById(R.id.etMessage);
        mRecyclerView = inflate.findViewById(R.id.recycleView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mChatAdapter);
        mBtnSendMessage.setOnClickListener(this);

        return inflate;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    private void initChatService(@NonNull Context context) {
        App applicationContext = (App) context.getApplicationContext();
        CPaaS cpass = applicationContext.getCpass();
        chatService = cpass.getChatService();
        chatService.setChatListener(new ChatListener() {
            @Override
            public void inboundChatMessageReceived(InboundMessage inboundMessage) {
                Log.d("CPaaS.ChatService", "New message from " + inboundMessage.getSenderAddress() + inboundMessage.getMessage());
                ChatModel chatModel = new ChatModel(
                        inboundMessage.getMessage(),
                        inboundMessage.getSenderAddress(),
                        true,
                        inboundMessage.getMessageId()
                );
                notifyList(chatModel);
            }

            @Override
            public void chatDeliveryStatusChanged(String s, String s1, String s2) {
                Log.d("CPaaS.ChatService", "Message delivery status changed to " + s1);
            }

            @Override
            public void chatParticipantStatusChanged(ChatGroupParticipant chatGroupParticipant, String s) {

            }

            @Override
            public void outboundChatMessageSent(OutboundMessage outboundMessage) {
                Log.d("CPaaS.ChatService", "Message is sent to " + outboundMessage.getSenderAddress());

                ChatModel chatModel = new ChatModel(
                        outboundMessage.getMessage(),
                        outboundMessage.getSenderAddress(),
                        false,
                        outboundMessage.getMessageId()
                );
                notifyList(chatModel);
                mEtMessage.setText("");
            }

            @Override
            public void isComposingReceived(String s, String s1, long l) {

            }

            @Override
            public void groupChatSessionInvitation(List<ChatGroupParticipant> list, String s, String s1) {

            }

            @Override
            public void groupChatEventNotification(String s, String s1, String s2) {

            }
        });

    }

    private void sendMessage(String participant, String txt) {
        ChatConversation chatConversation = (ChatConversation) chatService.createConversation(participant);

        OutboundMessage message = chatService.createMessage(txt);

        chatConversation.send(message, new MessagingCallback() {
            @Override
            public void onSuccess() {
                Log.d("CPaaS.ChatService", "Message is sent");
            }

            @Override
            public void onFail(MobileError error) {
                Log.d("CPaaS.ChatService", "Message is failed");
                showMessage("Try again later");
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartChat:
                sendMessage(mEtDestination.getText().toString(), mEtMessage.getText().toString());
                break;
        }
    }

    private void notifyList(ChatModel chatModel) {
        mChatModels.add(chatModel);
        Context context = getContext();
        if (context != null) {
            Handler mainHandler = new Handler(context.getMainLooper());

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d("cpass", "notify list");
                    mChatAdapter.notifyDataSetChanged();
                    mRecyclerView.scrollToPosition(mChatModels.size() - 1);
                } // This is your code
            };
            mainHandler.post(myRunnable);
        }

    }

    private void showMessage(String message) {
        Snackbar.make(mEtMessage, message, Snackbar.LENGTH_SHORT).show();
    }
}
