
package com.philleeran.flicktoucher.view.select.file;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.philleeran.flicktoucher.IPhilPad;
import com.philleeran.flicktoucher.PadUtils;
import com.philleeran.flicktoucher.R;
import com.philleeran.flicktoucher.R.drawable;
import com.philleeran.flicktoucher.db.PhilPad;
import com.philleeran.flicktoucher.utils.D;
import com.philleeran.flicktoucher.utils.L;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class AddFileOpenActivity extends Activity implements  ServiceConnection{

    IPhilPad mPadBind;

    int index = 0;

    private Context mContext = null;

    final int GALLERY_INTENT_CALLED = 1;
    final int GALLERY_KITKAT_INTENT_CALLED = 2;

    private int mGroupId;

    private int mPositionId;

    private float DPSCALE;

    private String mMimeType;
    private int mPadSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mPadSize = Integer.parseInt(PhilPad.Settings.getString(mContext.getContentResolver(), PhilPad.Settings.SETTINGS_KEY_PAD_SIZE, "4"));
        DPSCALE = getResources().getDisplayMetrics().density;

        Intent intent = getIntent();

        mGroupId = intent.getIntExtra(PadUtils.INTENT_DATA_GROUPID, 0);
        mPositionId = intent.getIntExtra(PadUtils.INTENT_DATA_LISTID, 0);
        mMimeType = intent.getStringExtra(PadUtils.INTENT_DATA_MIMETYPE);
        setContentView(R.layout.app_list_activity);


        if (Build.VERSION.SDK_INT <Build.VERSION_CODES.KITKAT){
            Intent chooseFile;
            chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType(mMimeType);
            startActivityForResult(Intent.createChooser(chooseFile, "Select Application"), GALLERY_INTENT_CALLED);

        } else {
            Intent chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFile.setType(mMimeType);
            startActivityForResult(chooseFile, GALLERY_KITKAT_INTENT_CALLED);
        }

        Intent serviceIntent = new Intent(PadUtils.ACTION_PHILPAD_SERVICE);
        L.d("startPhilPadService");
        serviceIntent.setPackage(PadUtils.ACTION_PHILPAD_PACKAGE);
        serviceIntent.setAction(PadUtils.ACTION_PHILPAD_SERVICE);
        bindService(serviceIntent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GALLERY_INTENT_CALLED: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String uriString = uri.toString();
                    L.d("uriString : " + uriString);
                    String path = null;
                    if (uriString.startsWith("content:")) {
                        path = getPath(mContext, uri);
                    } else if (uriString.startsWith("file:")) {
                        path = uri.toString().substring("file:/".length());
                    }
                    final int THUMBSIZE = 94;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;

                    Bitmap curThumb = null;
                    if (mMimeType.startsWith("image")) {
                        L.d("path : " + path);
                        if (checkValidFileType(path, "image")) {
                            curThumb = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), THUMBSIZE * (int) DPSCALE, THUMBSIZE * (int) DPSCALE);
                        } else {
                            PadUtils.Toast(mContext, R.string.toast_invalid_file);
                        }

                    } else if (mMimeType.startsWith("audio")) {
                        if (checkValidFileType(path, "audio")) {
                            Cursor cursor = mContext.getContentResolver().query(uri, new String[]{
                                    MediaStore.Audio.AlbumColumns.ALBUM_ID
                            }, null, null, null);

                            if (cursor.moveToFirst()) {
                                int albumId = cursor.getInt(0);
                                curThumb = getAlbumArt(albumId);
                                if (curThumb == null) {
                                    curThumb = PadUtils.getBitmapFromDrawable(mContext, mContext.getResources().getDrawable(drawable.ic_music_video_black_24px), 96 * (int) DPSCALE, 96 * (int) DPSCALE);
                                }
                            }
                            cursor.close();
                        } else {
                            PadUtils.Toast(mContext, R.string.toast_invalid_file);
                        }

                    } else if (mMimeType.startsWith("video")) {
                        if (checkValidFileType(path, "video")) {
                            curThumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
                        } else {
                            PadUtils.Toast(mContext, R.string.toast_invalid_file);
                        }
                    }
                    if (curThumb == null) {
                        finish();
                        return;
                    }
                    ContextWrapper cw = new ContextWrapper(mContext);
                    File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                    File mypath = new File(directory, "group_" + mGroupId + "_" + mPositionId + "_" + System.currentTimeMillis() + ".png");
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(mypath);
                        curThumb.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    PhilPad.Pads.setPadItem(mContext, mGroupId, mPositionId, PhilPad.Pads.PAD_TYPE_FILEOPEN, null, mMimeType, "file://" + mypath.getPath(), 0, path);
                    PhilPad.Pads.setGroupIcon(mContext, mGroupId, mPadSize);
                    try {
                        mPadBind.notifyReDrawGridView();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
            case GALLERY_KITKAT_INTENT_CALLED: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String uriString = uri.toString();
                    L.d("uriString : " + uriString);
                    String path = null;
                    if (uriString.startsWith("content:")) {
                        final int takeFlags = data.getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        // Check for the freshest data.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }

                        path = getPath(mContext, uri);
                    } else if (uriString.startsWith("file:")) {
                        path = uri.toString().substring("file:/".length());
                    }
                    final int THUMBSIZE = 94;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;

                    Bitmap curThumb = null;
                    if (mMimeType.startsWith("image")) {
                        if (!TextUtils.isEmpty(path) && checkValidFileType(path, "image")) {
                            curThumb = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), THUMBSIZE * (int) DPSCALE, THUMBSIZE * (int) DPSCALE);
                        } else {
                            PadUtils.Toast(mContext, R.string.toast_invalid_file);
                        }

                    } else if (mMimeType.startsWith("audio")) {
                        if (checkValidFileType(path, "audio")) {
                            Cursor cursor = mContext.getContentResolver().query(uri, new String[]{
                                    MediaStore.Audio.AlbumColumns.ALBUM_ID
                            }, null, null, null);

                            if (cursor.moveToFirst()) {
                                int albumId = cursor.getInt(0);
                                curThumb = getAlbumArt(albumId);

                                //TODO curThumb == null at M
                                if (curThumb == null) {
                                    curThumb = PadUtils.getBitmapFromDrawable(mContext, mContext.getResources().getDrawable(drawable.ic_music_video_black_24px), 96 * (int) DPSCALE, 96 * (int) DPSCALE);
                                }
                            }
                            cursor.close();
                        } else {
                            PadUtils.Toast(mContext, R.string.toast_invalid_file);
                        }

                    } else if (mMimeType.startsWith("video")) {
                        if (checkValidFileType(path, "video")) {
                            curThumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
                        } else {
                            PadUtils.Toast(mContext, R.string.toast_invalid_file);
                        }
                    }
                    if (curThumb == null) {
                        finish();
                        return;
                    }
                    ContextWrapper cw = new ContextWrapper(mContext);
                    File directory = cw.getDir(D.DIR_PATH_DEFAULT, Context.MODE_PRIVATE);
                    File mypath = new File(directory, "group_" + mGroupId + "_" + mPositionId + "_" + System.currentTimeMillis() + ".png");
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(mypath);
                        curThumb.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    PhilPad.Pads.setPadItem(mContext, mGroupId, mPositionId, PhilPad.Pads.PAD_TYPE_FILEOPEN, null, mMimeType, "file://" + mypath.getPath(), 0, path);
                    PhilPad.Pads.setGroupIcon(mContext, mGroupId, mPadSize);
                    try {
                        mPadBind.notifyReDrawGridView();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }


                break;
            }
        }
        finish();
    }

    private boolean checkValidFileType(String path, String fileType) {
        String filePath = path.toLowerCase();
        if (fileType.equals("image")) {
            if (filePath.endsWith(".png")) {
                return true;
            } else if (filePath.endsWith(".gif")) {
                return true;
            } else if (filePath.endsWith(".jpg")) {
                return true;
            } else if (filePath.endsWith(".jpeg")) {
                return true;
            } else if (filePath.endsWith(".bmp")) {
                return true;
            } else {
                return false;
            }
        } else if (fileType.equals("audio")) {
            if (filePath.endsWith(".mp3")) {
                return true;
            } else if (filePath.endsWith(".wav")) {
                return true;
            } else if (filePath.endsWith(".ogg")) {
                return true;
            } else if (filePath.endsWith(".mid")) {
                return true;
            } else if (filePath.endsWith(".midi")) {
                return true;
            } else {
                return false;
            }
        } else if (fileType.equals("video")) {
            if (filePath.endsWith(".avi")) {
                return true;
            } else if (filePath.endsWith(".3gp")) {
                return true;
            } else if (filePath.endsWith(".mp4")) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }
    public Bitmap getAlbumArt(int albumId) {
        Bitmap bitmap = null;
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
        try {
            ParcelFileDescriptor fd = mContext.getContentResolver().openFileDescriptor(sAlbumArtUri, "r");
            bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private int getAlbumId(Uri uri) {
        String[] projection = {
            MediaStore.Images.Media._ID
        };
        Cursor cursor = null;
        try {
            cursor = managedQuery(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int ret = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                return ret;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    private String getPath(Uri uri) {
        String[] projection = {
            MediaStore.Images.Media.DATA
        };
        Cursor cursor = null;
        try {
            cursor = managedQuery(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String ret = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                return ret;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
    private String getUriId(Uri uri) {
        String[] projection = {
            MediaStore.Images.ImageColumns._ID
        };
        Cursor cursor = null;
        try {
            cursor = managedQuery(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String ret = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
                return ret;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPadBind = IPhilPad.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPadBind = null;
        }

}
