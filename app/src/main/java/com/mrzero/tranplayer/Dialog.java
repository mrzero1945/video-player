package com.mrzero.tranplayer;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.MainThread;

/* loaded from: classes3.dex */
public abstract class Dialog {
    public static final int TYPE_ERROR = 0;
    public static final int TYPE_LOGIN = 1;
    public static final int TYPE_PROGRESS = 3;
    public static final int TYPE_QUESTION = 2;
    private static Callbacks sCallbacks;
    private static Handler sHandler;
    private Object mContext;
    public String mText;
    private final String mTitle;
    public final int mType;

    public interface Callbacks {
        @MainThread
        void onCanceled(Dialog dialog);

        @MainThread
        void onDisplay(ErrorMessage errorMessage);

        @MainThread
        void onDisplay(LoginDialog loginDialog);

        @MainThread
        void onDisplay(ProgressDialog progressDialog);

        @MainThread
        void onDisplay(QuestionDialog questionDialog);

        @MainThread
        void onProgressUpdate(ProgressDialog progressDialog);
    }

    public static class ErrorMessage extends Dialog {
        private ErrorMessage(String str, String str2) {
            super(0, str, str2);
        }
    }

    public static abstract class IdDialog extends Dialog {
        public long mId;

        public IdDialog(long j10, int i10, String str, String str2) {
            super(i10, str, str2);
            this.mId = j10;
        }

        private native void nativeDismiss(long j10);

        @Override // com.mrzero.tranplayer.Dialog
        @MainThread
        public void dismiss() {
            long j10 = this.mId;
            if (j10 != 0) {
                nativeDismiss(j10);
                this.mId = 0L;
            }
        }
    }

    public static class LoginDialog extends IdDialog {
        private final boolean mAskStore;
        private final String mDefaultUsername;

        private native void nativePostLogin(long j10, String str, String str2, boolean z10);

        @MainThread
        public boolean asksStore() {
            return this.mAskStore;
        }

        @Override // com.mrzero.tranplayer.Dialog.IdDialog, com.mrzero.tranplayer.Dialog
        @MainThread
        public /* bridge */ /* synthetic */ void dismiss() {
            super.dismiss();
        }

        @MainThread
        public String getDefaultUsername() {
            return this.mDefaultUsername;
        }

        @MainThread
        public void postLogin(String str, String str2, boolean z10) {
            long j10 = this.mId;
            if (j10 != 0) {
                nativePostLogin(j10, str, str2, z10);
                this.mId = 0L;
            }
        }

        private LoginDialog(long j10, String str, String str2, String str3, boolean z10) {
            super(j10, 1, str, str2);
            this.mDefaultUsername = str3;
            this.mAskStore = z10;
        }
    }

    public static class ProgressDialog extends IdDialog {
        private final String mCancelText;
        private final boolean mIndeterminate;
        private float mPosition;

        /* JADX INFO: Access modifiers changed from: private */
        public void update(float f10, String str) {
            this.mPosition = f10;
            this.mText = str;
        }

        @Override // com.mrzero.tranplayer.Dialog.IdDialog, com.mrzero.tranplayer.Dialog
        @MainThread
        public /* bridge */ /* synthetic */ void dismiss() {
            super.dismiss();
        }

        @MainThread
        public String getCancelText() {
            return this.mCancelText;
        }

        @MainThread
        public float getPosition() {
            return this.mPosition;
        }

        @MainThread
        public boolean isCancelable() {
            return this.mCancelText != null;
        }

        @MainThread
        public boolean isIndeterminate() {
            return this.mIndeterminate;
        }

        private ProgressDialog(long j10, String str, String str2, boolean z10, float f10, String str3) {
            super(j10, 3, str, str2);
            this.mIndeterminate = z10;
            this.mPosition = f10;
            this.mCancelText = str3;
        }
    }

    public static class QuestionDialog extends IdDialog {
        public static final int TYPE_ERROR = 2;
        public static final int TYPE_NORMAL = 0;
        public static final int TYPE_WARNING = 1;
        private final String mAction1Text;
        private final String mAction2Text;
        private final String mCancelText;
        private final int mQuestionType;

        private native void nativePostAction(long j10, int i10);

        @Override // com.mrzero.tranplayer.Dialog.IdDialog, com.mrzero.tranplayer.Dialog
        @MainThread
        public /* bridge */ /* synthetic */ void dismiss() {
            super.dismiss();
        }

        @MainThread
        public String getAction1Text() {
            return this.mAction1Text;
        }

        @MainThread
        public String getAction2Text() {
            return this.mAction2Text;
        }

        @MainThread
        public String getCancelText() {
            return this.mCancelText;
        }

        @MainThread
        public int getQuestionType() {
            return this.mQuestionType;
        }

        @MainThread
        public void postAction(int i10) {
            long j10 = this.mId;
            if (j10 != 0) {
                nativePostAction(j10, i10);
                this.mId = 0L;
            }
        }

        private QuestionDialog(long j10, String str, String str2, int i10, String str3, String str4, String str5) {
            super(j10, 2, str, str2);
            this.mQuestionType = i10;
            this.mCancelText = str3;
            this.mAction1Text = str4;
            this.mAction2Text = str5;
        }
    }

    public Dialog(int i10, String str, String str2) {
        this.mType = i10;
        this.mTitle = str;
        this.mText = str2;
    }

    private static void cancelFromNative(final Dialog dialog) {
        sHandler.post(new Runnable() {
            @Override // java.lang.Runnable
            public void run() {
                if (dialog instanceof IdDialog) {
                    ((IdDialog) dialog).dismiss();
                }
                Callbacks callbacks = sCallbacks;
                if (callbacks == null || dialog == null) {
                    return;
                }
                callbacks.onCanceled(dialog);
            }
        });
    }

    private static void displayErrorFromNative(String str, String str2) {
        final ErrorMessage errorMessage = new ErrorMessage(str, str2);
        sHandler.post(new Runnable() {
            @Override // java.lang.Runnable
            public void run() {
                Callbacks callbacks = sCallbacks;
                if (callbacks != null) {
                    callbacks.onDisplay(errorMessage);
                }
            }
        });
    }

    private static Dialog displayLoginFromNative(long j10, String str, String str2, String str3, boolean z10) {
        final LoginDialog loginDialog = new LoginDialog(j10, str, str2, str3, z10);
        sHandler.post(new Runnable() {
            @Override // java.lang.Runnable
            public void run() {
                Callbacks callbacks = sCallbacks;
                if (callbacks != null) {
                    callbacks.onDisplay(loginDialog);
                }
            }
        });
        return loginDialog;
    }

    private static Dialog displayProgressFromNative(long j10, String str, String str2, boolean z10, float f10, String str3) {
        final ProgressDialog progressDialog = new ProgressDialog(j10, str, str2, z10, f10, str3);
        sHandler.post(new Runnable() {
            @Override // java.lang.Runnable
            public void run() {
                Callbacks callbacks = sCallbacks;
                if (callbacks != null) {
                    callbacks.onDisplay(progressDialog);
                }
            }
        });
        return progressDialog;
    }

    private static Dialog displayQuestionFromNative(long j10, String str, String str2, int i10, String str3, String str4, String str5) {
        final QuestionDialog questionDialog = new QuestionDialog(j10, str, str2, i10, str3, str4, str5);
        sHandler.post(new Runnable() {
            @Override // java.lang.Runnable
            public void run() {
                Callbacks callbacks = sCallbacks;
                if (callbacks != null) {
                    callbacks.onDisplay(questionDialog);
                }
            }
        });
        return questionDialog;
    }

    private static native void nativeSetCallbacks(LibVLC libVLC, boolean z10);

    @MainThread
    public static void setCallbacks(LibVLC libVLC, Callbacks callbacks) {
        if (callbacks != null && sHandler == null) {
            sHandler = new Handler(Looper.getMainLooper());
        }
        sCallbacks = callbacks;
        nativeSetCallbacks(libVLC, callbacks != null);
    }

    private static void updateProgressFromNative(final Dialog dialog, final float f10, final String str) {
        sHandler.post(new Runnable() {
            @Override // java.lang.Runnable
            public void run() {
                if (dialog.getType() != 3) {
                    throw new IllegalArgumentException("dialog is not a progress dialog");
                }
                ProgressDialog progressDialog = (ProgressDialog) dialog;
                progressDialog.update(f10, str);
                Callbacks callbacks = sCallbacks;
                if (callbacks != null) {
                    callbacks.onProgressUpdate(progressDialog);
                }
            }
        });
    }

    @MainThread
    public void dismiss() {
    }

    @MainThread
    public Object getContext() {
        return this.mContext;
    }

    @MainThread
    public String getText() {
        return this.mText;
    }

    @MainThread
    public String getTitle() {
        return this.mTitle;
    }

    @MainThread
    public int getType() {
        return this.mType;
    }

    @MainThread
    public void setContext(Object obj) {
        this.mContext = obj;
    }
}
