package org.monroe.team.smooker.promo.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.monroe.team.android.box.BitmapUtils;
import org.monroe.team.android.box.utils.DisplayUtils;

public class BlurImageView extends ImageView{

    private Bitmap finalImage;
    public BlurImageView(Context context) {
        super(context);
    }

    public BlurImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BlurImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BlurImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private Point lastUsedSize;

    @Override
    public void setImageBitmap(Bitmap bm) {
        lastUsedSize = null;
        super.setImageBitmap(bm);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Point point = new Point(getWidth(),getHeight());
        int bottomPixels = (int) DisplayUtils.dpToPx(80,getResources());

        if (getWidth() == 0 || getHeight()==0){
            super.onDraw(canvas);
            return;
        }
        if (lastUsedSize == null || point.x != lastUsedSize.x || point.y != lastUsedSize.y ){
            lastUsedSize = point;
            Bitmap originalBitmap = Bitmap.createBitmap(lastUsedSize.x, bottomPixels, Bitmap.Config.ARGB_8888);
            Canvas originalBitmapCanvas = new Canvas(originalBitmap);
            originalBitmapCanvas.translate(0,-(getHeight() - bottomPixels));
            super.onDraw(originalBitmapCanvas);
            finalImage = BitmapUtils.fastblur(originalBitmap,20);
        }

        super.onDraw(canvas);
        if (finalImage != null){
            canvas.drawBitmap(finalImage,0,getHeight() - bottomPixels,null);
        }
    }
}
