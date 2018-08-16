package face.yang.com.facerecognition.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager;

import face.yang.com.facerecognition.R;

public class CommonDialog extends Dialog {
    private Context context;
    private int layoutId;

    public CommonDialog(@NonNull Context context, int layoutId) {
        super(context, R.style.CommonDialogStyle);
        this.layoutId = layoutId;
        this.context = context;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(this.layoutId);
        this.setCanceledOnTouchOutside(true);
        Window window = this.getWindow();
        this.getWindow().setFlags(2, 2);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.dimAmount = 0.7F;
        window.setAttributes(attributes);
    }
}
