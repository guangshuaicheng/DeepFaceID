package face.yang.com.facerecognition.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class RectView extends View {
    private Canvas canvas;

    RectF rectF = new RectF();
    private RectF rect;
    private Paint paint;

    public RectView(Context context) {
        super(context);
    }

    public RectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas=canvas;
        if(rect!=null&&paint!=null){
            canvas.drawRect(rect,paint);
        }
    }

    public Canvas getCanvas(){
        return canvas;
    }

    public void drawRect(RectF rect, Paint paint){
        this.rect=rect;
        this.paint=paint;
        if(Looper.myLooper() != Looper.getMainLooper()){
            postInvalidate();
        }else {
            invalidate();
        }
    }
}
