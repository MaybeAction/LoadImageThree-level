package com.example.maybe.loadimagethreelevel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * 用来图片的网络下载，以及图片的缓存处理
 * 图片的三级缓存
 */

public class LoadImage {
    //声明一个Context对讲，从外部获得context对象，并实例化它
    private static Context contextThis;

    /**
     * 用LruCache缓存
     */
    private static LruCache<String,Bitmap> lruCache=new LruCache<String,Bitmap>(1024*1024*5){
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };


    /**
     *
     * @param url  图片地址
     * @param imageView  要显示的控件
     */
    public static void getLoadImage(Context context, String url, ImageView imageView,int with,int height){
        contextThis=context;
        //设置tag标记
        imageView.setTag(url);
        //声明一个Bitmap 对象
        Bitmap bitmap=null;
        //首先查看内存缓存中是否有这个图片
        bitmap=lruCache.get(url);
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap);

            return;//直接结束方法
        }

        //其次是从SDK中获取图片
        File cacheDir=contextThis.getCacheDir();
        if(cacheDir.exists()){
            File[] files = cacheDir.listFiles();
            for (File file:files) {
                int a=url.lastIndexOf("/");
                String fileName=url.substring(a+1);
                if(fileName.equals(file.getName())){
                    bitmap=BitmapFactory.decodeFile(file.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                    return;
                }
            }
        }

        //启动异步任务获取图片
        getBitmapByAsyncTask(url,imageView,with,height);
    }

    /**
     *
     * @param str    图片的地址
     * @param imageView 将ImageView通过内部类的构造方法传入，并设置
     *                  实例化AsyncTask子类对象，并调用execute()方法，传入URL
     */
    private static void getBitmapByAsyncTask(String str,ImageView imageView,int with,int height) {
        LoadImageAsyncTask loadImageAsyncTask=new LoadImageAsyncTask(imageView,with,height);
        loadImageAsyncTask.execute(str);

    }

    /**
     * 异步下载图片内部类
     */
    public static class LoadImageAsyncTask extends AsyncTask<String,Void,Bitmap>{
        private String str=null;
        private ImageView imageView;
        private int with,height;

        public LoadImageAsyncTask(ImageView imageView,int with,int height) {
            this.imageView = imageView;
            this.with=with;
            this.height=height;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap=null;
            str=params[0];
            try {
                URL url=new URL(str);
                HttpURLConnection connection= (HttpURLConnection) url.openConnection();
                connection.connect();
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
                    InputStream is=connection.getInputStream();
                    bitmap= BitmapFactory.decodeStream(is);
                    //压缩图片
                    bitmap=Bitmap.createScaledBitmap(bitmap,with,height,false);

                    //保存到内存缓存中
                    lruCache.put(str,bitmap);
                    //保存到SD卡中
                    saveToSDK(str,bitmap);


                    //关闭流
                    is.close();
                }
                connection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        /**
         * 将图片保存到SD卡中
         * @param url   图片的地址
         * @param bitmap    异步任务下载的Bitmap对象
         */
        private void saveToSDK(String url,Bitmap bitmap) {
            //获得缓存路径
            File cacheDir=contextThis.getCacheDir();
            if(!cacheDir.exists()){//如果不存在就创建出来
                cacheDir.mkdirs();
            }
            //截取字符串中子字符串
            int a=url.lastIndexOf("/");
            String fileName=url.substring(a+1);

            try {
                //  I/O流写入图片
                FileOutputStream outputStream=new FileOutputStream(new File(cacheDir,fileName));
                //第二个参数代表，压缩率
                bitmap.compress(Bitmap.CompressFormat.JPEG,100, outputStream);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap!=null){
                if(imageView!=null&&imageView.getTag().equals(str)){
                    //如果imageView不为空，且标记的位置和对应的图片地址一致，就设置图片
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}