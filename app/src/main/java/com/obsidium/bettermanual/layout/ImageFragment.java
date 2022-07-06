package com.obsidium.bettermanual.layout;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.ma1co.openmemories.framework.ImageInfo;
import com.github.ma1co.pmcademo.app.ScalingBitmapView;
import com.obsidium.bettermanual.ActivityInterface;
import com.obsidium.bettermanual.AvIndexManager;
import com.obsidium.bettermanual.MainActivity;
import com.obsidium.bettermanual.R;
import com.sony.scalar.hardware.avio.DisplayManager;
import com.sony.scalar.media.AvindexContentInfo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageFragment extends BaseLayout {

    private final String TAG = ImageFragment.class.getSimpleName();
    private ScalingBitmapView imageView;
    private FrameLayout surfaceViewParent;
    Bitmap image;
    AvindexContentInfo info;
    private float scaleFactor = 1;
    private final float scaleStep = 0.2f;
    private final float maxScaleFactor = 8;
    private TextView zoomImageView;

    private TextView photoNumView;
    private boolean zoomEnabled = false;

    public ImageFragment(Context context,ActivityInterface activityInterface) {
        super(context,activityInterface);
        inflateLayout(R.layout.image_fragment);
        surfaceViewParent = (FrameLayout) findViewById(R.id.surfaceParentView);
        zoomImageView = (TextView)findViewById(R.id.iv_zoom);
        zoomImageView.setVisibility(GONE);
        imageView = (ScalingBitmapView) findViewById(R.id.imageView);
        photoNumView = (TextView)findViewById(R.id.iv_photonum);

        Log.d(TAG, "ImageCount:" + activityInterface.getAvIndexManager().getCount());
        activityInterface.getAvIndexManager().update();
        loadImage();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void Destroy() {
        closeImage();
    }

    private void loadImage()
    {
        AvIndexManager avIndexManager = activityInterface.getAvIndexManager();

        int position = avIndexManager.getPosition();
        int count = avIndexManager.getCount();

        if (position > -1 && count > 0)
        {
            String imageFilename = avIndexManager.getData();
            Log.d(TAG,"Img path:" + imageFilename);

            try {
                photoNumView.setText((position + 1) + "/" + count);

                BitmapFactory.Options options = new BitmapFactory.Options();

                // First get image size
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imageFilename, options);

                Log.d(TAG, "Image size " + options.outWidth + ", " + options.outHeight);

                if (options.outWidth == 0 && options.outHeight == 0) {
                    throw new FileNotFoundException();
                }

                DisplayManager.VideoRect videoRect = activityInterface.getDisplayManager().getDisplayedVideoRect();

                int scale = Math.max(
                    Math.max(options.outWidth / (videoRect.pxRight - videoRect.pxLeft), 1),
                    Math.max(options.outHeight / (videoRect.pxBottom - videoRect.pxTop), 1)
                );

                options.inJustDecodeBounds = false;
                options.inSampleSize = scale;

                if (image != null) {
                    image.recycle();
                }

                image = BitmapFactory.decodeFile(imageFilename, options);
                imageView.setImageBitmap(image);
            } catch (FileNotFoundException e) {
                Log.e(TAG, Log.getStackTraceString(e));

                // Trim out obsolete thumbnails
                avIndexManager.delete();
            } catch (Throwable e) {
                Log.e(TAG, Log.getStackTraceString(e));
            } finally {
                if (image != null && image.isRecycled()) {
                    // View is really unhappy if it decides to redraw and its bitmap is gone, so:
                    Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                    image = Bitmap.createBitmap(16, 16, conf);
                }
            }
        }
    }

    private void closeImage() {
        if (image != null) {
            image.recycle();
            image = null;
        }
    }

    private void logCursor(Cursor cursor)
    {
        String name;
        String value;
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.getPosition() == -1)
                cursor.moveToFirst();
            int columnCount = cursor.getColumnCount();
            String out = "";
            for (int i = 0; i < columnCount; i++) {
                name = cursor.getColumnName(i);
                value = cursor.getString(cursor.getColumnIndexOrThrow(cursor.getColumnName(i)));

                out += " " +name + " " +value;

            }
            Log.d(TAG, out);
        }
        else Log.d(TAG, "Nothing to log");
    }


    public void moveX(boolean z) {

    }

    public void moveY(boolean z) {

    }


    @Override
    public boolean onUpperDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onLowerDialChanged(int value) {

        if (value < 0)
        {
            activityInterface.getAvIndexManager().moveToPrevious();
        }
        else
            activityInterface.getAvIndexManager().moveToNext();
        loadImage();

        return false;
    }

    private void toggleZoom()
    {
        if (zoomEnabled)
        {
            zoomEnabled = false;
            zoomImageView.setVisibility(GONE);
        }
        else
        {
            zoomEnabled = true;
            zoomImageView.setVisibility(VISIBLE);
        }
    }

    @Override
    public boolean onUpKeyDown() {
        return false;
    }

    @Override
    public boolean onUpKeyUp()
    {
        if (zoomEnabled)
        {
            moveY(false);
        }
        return false;
    }

    @Override
    public boolean onDownKeyDown() {
        return false;
    }

    @Override
    public boolean onDownKeyUp() {
        if (zoomEnabled)
        {
            moveY(true);
        }
        return false;
    }

    @Override
    public boolean onLeftKeyDown() {
        return false;
    }

    @Override
    public boolean onLeftKeyUp() {
        if (zoomEnabled)
        {
            moveX(true);
        }
        else {
            activityInterface.getAvIndexManager().moveToPrevious();
            loadImage();
        }
        return false;
    }

    @Override
    public boolean onRightKeyDown() {
        return false;
    }

    @Override
    public boolean onRightKeyUp() {
        if (zoomEnabled)
        {
            moveX(false);
        }
        else {
            activityInterface.getAvIndexManager().moveToNext();
            loadImage();
        }
        return false;
    }

    @Override
    public boolean onEnterKeyDown() {
        return false;
    }

    @Override
    public boolean onEnterKeyUp() {
        toggleZoom();
        return false;
    }

    @Override
    public boolean onFnKeyDown() {
        return false;
    }

    @Override
    public boolean onFnKeyUp() {
        return false;
    }

    @Override
    public boolean onAelKeyDown() {
        return false;
    }

    @Override
    public boolean onAelKeyUp() {
        return false;
    }

    @Override
    public boolean onMenuKeyDown() {
        return false;
    }

    @Override
    public boolean onMenuKeyUp() {
        return false;
    }

    @Override
    public boolean onFocusKeyDown() {
        activityInterface.loadFragment(MainActivity.FRAGMENT_WAITFORCAMERARDY);
        return false;
    }

    @Override
    public boolean onFocusKeyUp() {
        return false;
    }

    @Override
    public boolean onShutterKeyDown() {
        return false;
    }

    @Override
    public boolean onShutterKeyUp() {
        return false;
    }

    @Override
    public boolean onPlayKeyDown() {
        return false;
    }

    @Override
    public boolean onPlayKeyUp() {
        activityInterface.loadFragment(MainActivity.FRAGMENT_WAITFORCAMERARDY);
        return false;
    }

    @Override
    public boolean onMovieKeyDown() {
        return false;
    }

    @Override
    public boolean onMovieKeyUp() {
        return false;
    }

    @Override
    public boolean onC1KeyDown() {
        return false;
    }

    @Override
    public boolean onC1KeyUp() {
        return false;
    }

    @Override
    public boolean onLensAttached() {
        return false;
    }

    @Override
    public boolean onLensDetached() {
        return false;
    }

    @Override
    public boolean onModeDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onZoomTeleKey() {
        return false;
    }

    @Override
    public boolean onZoomWideKey() {
        return false;
    }

    @Override
    public boolean onZoomOffKey() {
        return false;
    }

    @Override
    public boolean onDeleteKeyDown() {
        return false;
    }

    @Override
    public boolean onDeleteKeyUp() {
        return false;
    }
}
