package com.circlex.litehttp.callback;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.Response;

public abstract class FileCallback extends BaseCallback<File>{

    private String fileDir;
    private String fileName;

    public FileCallback(String fileDir, String fileName)
    {
        this.fileDir = fileDir;
        this.fileName = fileName;
    }
    @Override
    public File parseNetworkResponse(Response response) throws Exception {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        long downloadedLength = 0;
        try {
            long contentLength = response.body().contentLength();
            file = new File(fileDir + fileName);
            if (file.exists()) {
                downloadedLength = file.length();
            }
            if (contentLength == 0){
                return null;
            }else if (contentLength == downloadedLength){
                return file;
            }
            is = response.body().byteStream();
            savedFile = new RandomAccessFile(file, "rw");
            savedFile.seek(downloadedLength);
            byte[] b = new byte[2048];
            float sum = 0;
            int len;
            while ((len = is.read(b)) != -1){
                sum += len;
                savedFile.write(b, 0, len);
                Handler handler = new Handler(Looper.getMainLooper());
                float finalSum = sum;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onProgress(finalSum, contentLength);
                    }
                });
            }
            response.body().close();
            return file;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
