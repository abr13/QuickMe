package com.abr.quickme.classes;

import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import br.com.instachat.emojilibrary.R;
import br.com.instachat.emojilibrary.controller.EmojiKeyboard;
import br.com.instachat.emojilibrary.model.layout.EmojiCompatActivity;
import br.com.instachat.emojilibrary.model.layout.EmojiEditText;
import br.com.instachat.emojilibrary.model.layout.TelegramPanelEventListener;

/**
 * Created by edgar on 18/02/2016.
 */
public class TelegramBottomPanel {

    private static final String TAG = "TelegramBottomPanel";
    public EmojiEditText mInput;
    private EmojiCompatActivity mActivity;
    private Toolbar mBottomPanel;
    private EmojiKeyboard mEmojiKeyboard;
    private TelegramPanelEventListener mListener;
    private LinearLayout mCurtain;
    private Boolean mToogleIcon = Boolean.TRUE;

    private Boolean isEmojiKeyboardVisible = Boolean.FALSE;

    // CONSTRUCTOR
    public TelegramBottomPanel(EmojiCompatActivity activity, TelegramPanelEventListener listener) {
        this.mActivity = activity;
        this.initBottomPanel();
        this.setInputConfig();
        this.setOnBackPressed();
        this.mEmojiKeyboard = new EmojiKeyboard(this.mActivity, this.mInput);
        this.mListener = listener;
    }

    // INITIALIZATION
    private void initBottomPanel() {
        this.mBottomPanel = this.mActivity.findViewById(R.id.panel);
        this.mBottomPanel.setNavigationIcon(R.drawable.input_emoji);
        this.mBottomPanel.setTitleTextColor(0xFFFFFFFF);
        this.mBottomPanel.inflateMenu(R.menu.telegram_menu);

        this.mBottomPanel.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TelegramBottomPanel.this.isEmojiKeyboardVisible) {
                    TelegramBottomPanel.this.closeCurtain();
                    if (TelegramBottomPanel.this.mInput.isSoftKeyboardVisible()) {
                        TelegramBottomPanel.this.mBottomPanel.setNavigationIcon(R.drawable.ic_keyboard_grey600_24dp);
                        TelegramBottomPanel.this.mInput.hideSoftKeyboard();
                    } else {
                        TelegramBottomPanel.this.mBottomPanel.setNavigationIcon(R.drawable.input_emoji);
                        TelegramBottomPanel.this.mInput.showSoftKeyboard();
                    }
                } else {
                    TelegramBottomPanel.this.mBottomPanel.setNavigationIcon(R.drawable.ic_keyboard_grey600_24dp);
                    TelegramBottomPanel.this.closeCurtain();
                    TelegramBottomPanel.this.showEmojiKeyboard(0);
                }
            }
        });

        this.mBottomPanel.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (TelegramBottomPanel.this.mListener != null) {
                    if (item.getItemId() == R.id.action_attach) {
                        TelegramBottomPanel.this.mListener.onAttachClicked();
                    } else if (item.getItemId() == R.id.action_mic) {
                        if (TelegramBottomPanel.this.mInput.getText().toString().equals("")) {
                            TelegramBottomPanel.this.mListener.onMicClicked();
                        } else {
                            TelegramBottomPanel.this.mListener.onSendClicked();
                        }
                    }
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });

        this.mCurtain = this.mActivity.findViewById(R.id.curtain);
    }

    private void setInputConfig() {
        this.mInput = this.mBottomPanel.findViewById(R.id.input);
        this.mInput.addOnSoftKeyboardListener(new EmojiEditText.OnSoftKeyboardListener() {
            @Override
            public void onSoftKeyboardDisplay() {
                if (!TelegramBottomPanel.this.isEmojiKeyboardVisible) {
                    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                    scheduler.schedule(new Runnable() {
                        @Override
                        public void run() {
                            Handler mainHandler = new Handler(TelegramBottomPanel.this.mActivity.getMainLooper());
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    TelegramBottomPanel.this.openCurtain();
                                    TelegramBottomPanel.this.showEmojiKeyboard(0);
                                }
                            };
                            mainHandler.post(myRunnable);
                        }
                    }, 150, TimeUnit.MILLISECONDS);
                }
            }

            @Override
            public void onSoftKeyboardHidden() {
                if (TelegramBottomPanel.this.isEmojiKeyboardVisible) {
                    TelegramBottomPanel.this.closeCurtain();
                    TelegramBottomPanel.this.hideEmojiKeyboard(200);
                }
            }
        });

        this.mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final MenuItem micButton = TelegramBottomPanel.this.mBottomPanel.getMenu().findItem(R.id.action_mic);
                if (!TelegramBottomPanel.this.mInput.getText().toString().equals("") && TelegramBottomPanel.this.mToogleIcon) {
                    TelegramBottomPanel.this.mToogleIcon = Boolean.FALSE;
                    TelegramBottomPanel.this.mBottomPanel.findViewById(R.id.action_attach).animate().scaleX(0).scaleY(0).setDuration(150).start();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        TelegramBottomPanel.this.mBottomPanel.findViewById(R.id.action_mic).animate().scaleX(0).scaleY(0).setDuration(75).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                micButton.setIcon(R.drawable.ic_send_telegram);
                                TelegramBottomPanel.this.mBottomPanel.findViewById(R.id.action_mic).animate().scaleX(1).scaleY(1).setDuration(75).start();
                            }
                        }).start();
                    }
                } else if (TelegramBottomPanel.this.mInput.getText().toString().equals("")) {
                    TelegramBottomPanel.this.mToogleIcon = Boolean.TRUE;
                    TelegramBottomPanel.this.mBottomPanel.findViewById(R.id.action_attach).animate().scaleX(1).scaleY(1).setDuration(150).start();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        TelegramBottomPanel.this.mBottomPanel.findViewById(R.id.action_mic).animate().scaleX(0).scaleY(0).setDuration(75).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                micButton.setIcon(R.drawable.ic_microphone_grey600_24dp);
                                TelegramBottomPanel.this.mBottomPanel.findViewById(R.id.action_mic).animate().scaleX(1).scaleY(1).setDuration(75).start();
                            }
                        }).start();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setOnBackPressed() {
        this.mActivity.setOnBackPressed(new EmojiCompatActivity.OnBackPressed() {
            @Override
            public Boolean onBackPressed() {
                if (TelegramBottomPanel.this.isEmojiKeyboardVisible) {
                    TelegramBottomPanel.this.isEmojiKeyboardVisible = Boolean.FALSE;
                    TelegramBottomPanel.this.hideEmojiKeyboard(0);
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
    }

    private void showEmojiKeyboard(int delay) {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TelegramBottomPanel.this.isEmojiKeyboardVisible = Boolean.TRUE;
        TelegramBottomPanel.this.mEmojiKeyboard.getEmojiKeyboardLayout().setVisibility(LinearLayout.VISIBLE);
    }

    private void hideEmojiKeyboard(int delay) {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TelegramBottomPanel.this.mBottomPanel.setNavigationIcon(R.drawable.input_emoji);
        TelegramBottomPanel.this.isEmojiKeyboardVisible = Boolean.FALSE;
        TelegramBottomPanel.this.mEmojiKeyboard.getEmojiKeyboardLayout().setVisibility(LinearLayout.GONE);
    }

    private void openCurtain() {
        this.mCurtain.setVisibility(LinearLayout.VISIBLE);
    }

    private void closeCurtain() {
        this.mCurtain.setVisibility(LinearLayout.INVISIBLE);
    }

    //GETTER AND SETTERS
    public void setListener(TelegramPanelEventListener mListener) {
        this.mListener = mListener;
    }

    public String getText() {
        return this.mInput.getText().toString();
    }

    public void setText(String text) {
        this.mInput.setText(text);
    }
}
