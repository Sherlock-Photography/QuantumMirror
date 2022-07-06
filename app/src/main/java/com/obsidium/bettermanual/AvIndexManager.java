package com.obsidium.bettermanual;

import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.github.ma1co.openmemories.framework.DateTime;
import com.github.ma1co.openmemories.framework.ImageInfo;
import com.sony.scalar.media.AvindexContentInfo;
import com.sony.scalar.provider.AvindexStore;

import java.io.*;
import java.util.Locale;

/**
 * Created by KillerInk on 29.09.2017.
 */

 /*
        FOLDER
        _id 0 _data null dcf_folder_number 100 dcf_file_number null content_type null exist_jpeg null exist_mpo null exist_raw null content_created_local_date_time null content_created_local_date null content_created_local_time null content_created_utc_date_time null content_created_utc_date null content_created_utc_time null has_gps_info null time_zone null latitude null longitude null rec_order null
        LAST_CONTENT
        Nothing to log
        MEDIA
        _id 1 _data avindex://1000/00000001-default/00000001-00000925 dcf_folder_number 100 dcf_file_number 1663 content_type 1 exist_jpeg 1 exist_mpo 0 exist_raw 1 content_created_local_date_time 1507114550000 content_created_local_date 20171004 content_created_local_time 105550 content_created_utc_date_time 1507110950000 content_created_utc_date 20171004 content_created_utc_time 095550 has_gps_info 0 time_zone 60 latitude 0 longitude 0 rec_order 1
        INFO
        avi_version 45cd5a8d03e44b57b963dc9e781b722e
        THUMB
        Nothing to log
     */

public class AvIndexManager extends BroadcastReceiver
{
    private static final String TAG = AvIndexManager.class.getSimpleName();

    private ContentResolver contentResolver;
    private Cursor cursor;
    private final Uri mediaStorageUri;
    private Context context;
    private long _id;

    private final String idFieldName, sortFieldName;

    public IntentFilter MEDIA_INTENTS = new IntentFilter();
    public IntentFilter AVAILABLE_SIZE_INTENTS = new IntentFilter("com.sony.scalar.providers.avindex.action.AVINDEX_MEDIA_AVAILABLE_SIZE_CHANGED");

    public AvIndexManager(ContentResolver contentResolver, Context context)
    {
        this.context = context;
        this.contentResolver = contentResolver;
        this.mediaStorageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        this.idFieldName = MediaStore.Images.Media._ID;
        this.sortFieldName = MediaStore.Images.Media.DATE_TAKEN;
    }

    public ContentResolver getContentResolver() {
        return contentResolver;
    }

    public void onResume(Context context)
    {
        update();
    }

    private Cursor getCursorFromUri(Uri uri)
    {
        return contentResolver.query(uri, new String[]{idFieldName, MediaStore.Images.Media.DATA}, null, null, sortFieldName +" DESC");
    }


    public void onPause(Context context)
    {
        if (cursor != null && cursor.isClosed())
            cursor.close();
    }

    public String getData()
    {
        String filename = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));

        String storagePrefix = Environment.getExternalStorageDirectory().toString();

        // Inside the SD card mount we're only allowed to access DOS-style 8.3 paths where all letters are capital!
        filename = storagePrefix + filename.replace(storagePrefix, "").toUpperCase();

        return filename;
    }

    public Uri getUri() {
        return ContentUris.withAppendedId(mediaStorageUri, Long.parseLong(getId()));
    }

    /**
     * Creates a new {@link ImageInfo} instance for the given image id
     */
    public ImageInfo getImageInfo() {
        return ImageInfo.create(context, mediaStorageUri, _id);
    }

    public String getId()
    {
        return cursor.getString(cursor.getColumnIndexOrThrow(idFieldName));
    }

    public void update()
    {
        cursor = getCursorFromUri(mediaStorageUri);
        if (cursor.getCount() == 0)
            return;
        cursor.moveToFirst();
        _id = Long.parseLong(getId());
    }

    public void moveToNext()
    {
        if (cursor.getCount() == 0)
            return;
        cursor.moveToNext();
        if(cursor.isAfterLast())
            cursor.moveToFirst();
        _id = Long.parseLong(getId());
    }

    public void moveToPrevious()
    {
        if (cursor.getCount() == 0)
            return;
        cursor.moveToPrevious();
        if (cursor.isBeforeFirst())
            cursor.moveToLast();
        _id = Long.parseLong(getId());
    }

    public int getPosition()
    {
        return  cursor.getPosition();
    }

    public int getCount()
    {
        return cursor.getCount();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,intent.getAction());
    }


    private static void copyStream(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = source.read(buf)) > 0) {
            target.write(buf, 0, length);
        }
    }

    public String importImage(InputStream in) {
        Log.d(TAG, "Importing image...");

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.TITLE, "");
        values.put(MediaStore.Images.Media.DESCRIPTION, "");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        long epochTime = DateTime.getInstance().getCurrentTime().getTimeInMillis() / 1000;
        values.put(MediaStore.Images.Media.DATE_ADDED, epochTime);
        values.put(MediaStore.Images.Media.DATE_TAKEN, epochTime);
        values.put(MediaStore.Images.Media.DATE_MODIFIED, epochTime);

        Uri url = null;
        String stringUrl = null;

        try {
            url = contentResolver.insert(mediaStorageUri, values);

            OutputStream out = contentResolver.openOutputStream(url);

            try {
                copyStream(in, out);
            } finally {
                out.close();
            }

            /*
             * We can't create thumbnails because Android will try to insert them into a .thumbnails directory,
             * and the SDcard provider only allows 8.3 DOS-style filenames.
             */
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert image", e);
            if (url != null) {
                contentResolver.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }

    public void delete() {
        contentResolver.delete(getUri(), null, null);
    }
}
