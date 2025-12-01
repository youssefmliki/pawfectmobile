package com.example.pawfect.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Helper class for uploading images to ImageKit using REST API
 * 
 * Uses ImageKit's REST API directly via OkHttp for reliable uploads
 */
public class ImageKitHelper {
    
    // ImageKit credentials - configured for plmaosmzg
    private static final String IMAGEKIT_URL_ENDPOINT = "https://ik.imagekit.io/plmaosmzg";
    private static final String IMAGEKIT_PUBLIC_KEY = "public_fxmN+1igSpuGMlcxIo39fjfaAfQ=";
    private static final String IMAGEKIT_PRIVATE_KEY = "private_X7JIyypl9pbLuNshXDjPR/cjL3o=";
    private static final String IMAGEKIT_UPLOAD_URL = "https://upload.imagekit.io/api/v1/files/upload";
    
    private static final String TAG = "ImageKitHelper";
    private static final OkHttpClient client = new OkHttpClient();
    
    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }
    
    /**
     * Upload an image to ImageKit using REST API
     * @param context Android context
     * @param imageUri URI of the image to upload
     * @param callback Callback for upload result
     */
    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        try {
            // Convert URI to File
            File imageFile = uriToFile(context, imageUri);
            if (imageFile == null) {
                callback.onError("Failed to read image file");
                return;
            }
            
            // Generate unique file name
            String fileName = "pet_" + System.currentTimeMillis() + ".jpg";
            String folderPath = "/pawfect_match/pets/";
            
            // Create multipart request body
            RequestBody fileBody = RequestBody.create(
                MediaType.parse("image/jpeg"),
                imageFile
            );
            
            // Create multipart request body
            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .addFormDataPart("fileName", fileName)
                .addFormDataPart("folder", folderPath)
                .addFormDataPart("useUniqueFileName", "true");
            
            RequestBody requestBody = multipartBuilder.build();
            
            // ImageKit server-side upload uses Basic Auth: username = private key, password = empty
            String credentials = IMAGEKIT_PRIVATE_KEY + ":";
            String authHeader = "Basic " + android.util.Base64.encodeToString(
                credentials.getBytes("UTF-8"),
                android.util.Base64.NO_WRAP
            );
            
            // Build request with Basic Authentication
            Request request = new Request.Builder()
                .url(IMAGEKIT_UPLOAD_URL)
                .addHeader("Authorization", authHeader)
                .post(requestBody)
                .build();
            
            // Execute request
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    Log.e(TAG, "Upload failed", e);
                    callback.onError("Upload failed: " + e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws java.io.IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Upload response: " + responseBody);
                        
                        try {
                            // Parse JSON response
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String imageUrl = jsonResponse.optString("url");
                            
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Log.d(TAG, "Upload successful: " + imageUrl);
                                callback.onSuccess(imageUrl);
                            } else {
                                callback.onError("Failed to get image URL from response");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response", e);
                            callback.onError("Error parsing upload response: " + e.getMessage());
                        }
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        Log.e(TAG, "Upload failed: " + response.code() + " - " + errorBody);
                        callback.onError("Upload failed: " + response.code() + " - " + errorBody);
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error uploading image", e);
            callback.onError("Error: " + e.getMessage());
        }
    }
    
    /**
     * Convert URI to File
     */
    private static File uriToFile(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            File tempFile = File.createTempFile("upload_", ".jpg", context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.close();
            inputStream.close();
            
            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "Error converting URI to file", e);
            return null;
        }
    }
}

