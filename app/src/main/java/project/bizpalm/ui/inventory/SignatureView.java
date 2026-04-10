package project.bizpalm.ui.inventory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;

import java.io.ByteArrayOutputStream;

public class SignatureView extends View {

    private Paint paint = new Paint();
    private Path path = new Path();
    private boolean isEmpty = true;

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Disable parent scrolling when the user starts drawing
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                path.moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                isEmpty = false;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Re-enable parent scrolling when the user lifts their finger
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void clear() {
        path.reset();
        isEmpty = true;
        invalidate();
    }

    public boolean isSignatureEmpty() {
        return isEmpty;
    }

    public String getSignatureBase64() {
        if (isEmpty) return null;
        
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
