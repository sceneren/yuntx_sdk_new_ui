package com.yuntongxun.ecdemo.common.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.yuntongxun.ecdemo.R;
import com.yuntongxun.ecdemo.common.CCPAppManager;
import com.yuntongxun.ecdemo.common.base.CommonPoPWindow;
import com.yuntongxun.ecdemo.ui.personcenter.PersonInfoUI;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * onActivityResult（）中
 * <p>
 * switch (requestCode) {
 * case GetImageUtils.REQUEST_CODE_FROM_CAMERA:
 * if(resultCode == Activity.RESULT_CANCELED) {
 * GetImageUtils.deleteImageUri(getActivity(), GetImageUtils.imageUriFromCamera);
 * } else {
 * GetImageUtils.startPhotoZoom(getActivity(), GetImageUtils.imageUriFromCamera);
 * }
 * break;
 * case GetImageUtils.REQUEST_CODE_FROM_ALBUM:
 * if(resultCode == Activity.RESULT_CANCELED) {
 * return;
 * }
 * GetImageUtils.startPhotoZoom(getActivity(), data.getData());
 * break;
 * <p>
 * case GetImageUtils.REQUEST_CODE_FROM_CUTTING:
 * Bundle extras = data.getExtras();
 * if (extras != null) {
 * Bitmap photo = extras.getParcelable("data");
 * imageView.setImageBitmap(photo); //把图片显示在ImageView控件上
 * }
 * break;
 */
public class GetImageUtils {

    public static final int REQUEST_CODE_FROM_CAMERA = 5001;
    public static final int REQUEST_CODE_FROM_ALBUM = 5002;
    public static final int REQUEST_CODE_FROM_CUTTING = 5003;
    public static String mFileName;
    /**
     * 存放拍照图片的uri地址
     */
    public static Uri imageUriFromCamera;
    private static CommonPoPWindow commonPoPWindow;

    /**
     * 显示获取照片不同方式对话框
     */


    public static void showImagePickDialog(final Activity activity, final PersonInfoUI personCenterFragment, View view) {
        commonPoPWindow = new CommonPoPWindow(activity, new CommonPoPWindow.PopCallback() {
            @Override
            public View getPopWindowChildView(View mMenuView) {


                mMenuView.findViewById(R.id.btn_take_photo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickImageFromCamera(activity, personCenterFragment);
                        if (commonPoPWindow != null && commonPoPWindow.isShowing()) {
                            commonPoPWindow.dismiss();
                        }
                    }
                });
                mMenuView.findViewById(R.id.btn_pick_photo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickImageFromAlbum(activity, personCenterFragment);
                        if (commonPoPWindow != null && commonPoPWindow.isShowing()) {
                            commonPoPWindow.dismiss();
                        }
                    }
                });
                mMenuView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (commonPoPWindow != null && commonPoPWindow.isShowing()) {
                            commonPoPWindow.dismiss();
                        }
                    }
                });
                return null;
            }
        }, R.layout.pick_pic);

        commonPoPWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        commonPoPWindow.setAnimationStyle(android.R.style.Animation_InputMethod);

        commonPoPWindow.showAtLocation(
                view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }


    /**
     * 打开相机拍照获取图片
     */
    public static void pickImageFromCamera(final Activity activity, PersonInfoUI fragment) {
        imageUriFromCamera = createImageUri(activity);

        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriFromCamera);
        fragment.startActivityForResult(intent, REQUEST_CODE_FROM_CAMERA);
    }

    /**
     * 打开本地相册选取图片
     */
    public static void pickImageFromAlbum(final Activity activity, PersonInfoUI personCenterFragment) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        personCenterFragment.startActivityForResult(intent, REQUEST_CODE_FROM_ALBUM);
    }


    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public static void startPhotoZoom(Activity activity, Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", true);
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, REQUEST_CODE_FROM_CUTTING);
    }

    /**
     * 将图片保存到SD中
     */
    public static String saveFile(Context context, Bitmap bm, String fileName) throws IOException {

        File path = new File(FileAccessor.IMESSAGE_IMAGE);
        // 图片路径不存在创建之
        if (!path.exists()) {
            path.mkdirs();
        }


        // 图片文件如果不存在创建之
        File file = new File(path, fileName);

        if (!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
            file.createNewFile();
        }

        // 将图片压缩至文件对应的流里,即保存图片至该文件中
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
        return file.getPath();
    }


    public static void deleteAllFiles(File root) {
        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }

    public static boolean isFileExist() {
        File file = new File(FileAccessor.IMESSAGE_IMAGE, CCPAppManager.getUserId() + "touxiang.jpg");
        return file.exists();


    }

    public static File getPicFile() {
        File file = new File(FileAccessor.IMESSAGE_IMAGE, CCPAppManager.getUserId() + "touxiang.jpg");
        return file;
    }


    /**
     * 根据路径获取图片并压缩, 返回bitmap用于显示 保证图片在100K一下
     *
     * @param filePath 图片所在的路径
     * @param width    图片要显示的宽度
     * @param height   图片要显示的宽度
     * @return 图片Bitmap对象
     */
    public static Bitmap getSmallBitmap(String filePath, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // 计算压缩比例
        options.inSampleSize = calculateInSampleSize(options, width, height);
        // 根据压缩比例,压缩图片文件
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        System.out.println("gesmallBitmap");
        // int degree = readPictureDegree(filePath);
        // Bitmap bm = rotateBitmap(bitmap,degree) ;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        // 保证图片在500K一下
        while (baos.toByteArray().length > 1024 * 500) {
            baos.reset();
            quality -= 1;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(bais);

        return bitmap;
    }


    /**
     * 计算图片的压缩比例
     *
     * @param options   图片的设置
     * @param reqWidth  需要的宽度
     * @param reqHeight 需要的高度
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;// 不压缩
        if (height > reqHeight || width > reqWidth) {// 是否需要压缩
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            return inSampleSize;
        } else {
            return inSampleSize;
        }
    }


    /**
     * 创建一条图片uri,用于保存拍照后的照片
     */
    private static Uri createImageUri(Context context) {
        String name = "boreWbImg" + System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, name);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpeg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return uri;
    }

    /**
     * 删除一条图片
     */
    public static void deleteImageUri(Context context, Uri uri) {
        context.getContentResolver().delete(imageUriFromCamera, null, null);
    }

    /**
     * 获取图片文件路径
     */
    public static String getImageAbsolutePath(Context context, Uri uri) {
        Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(), uri,
                new String[]{MediaStore.Images.Media.DATA});
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return null;
    }

    /////////////////////Android4.4以上版本特殊处理如下//////////////////////////////////////

    /**
     * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
     *
     * @param context
     * @param imageUri
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getImageAbsolutePath19(Activity context, Uri imageUri) {
        if (context == null || imageUri == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }

        // MediaStore (and general)
        if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri)) {
                return imageUri.getLastPathSegment();
            }
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
