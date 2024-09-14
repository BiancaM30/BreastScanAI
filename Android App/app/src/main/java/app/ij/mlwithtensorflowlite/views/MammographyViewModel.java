package app.ij.mlwithtensorflowlite.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MammographyViewModel extends ViewModel {

    private final OkHttpClient client;
    private final MutableLiveData<String> resultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> imageLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> progressLiveData = new MutableLiveData<>();
    private MutableLiveData<JSONObject> predictionLiveData = new MutableLiveData<>();

    public MammographyViewModel() {
        this.client = new OkHttpClient();
    }

    public MutableLiveData<String> getResultLiveData() {
        return resultLiveData;
    }

    public MutableLiveData<Bitmap> getImageLiveData() {
        return imageLiveData;
    }

    public MutableLiveData<Boolean> getProgressLiveData() {
        return progressLiveData;
    }

    public MutableLiveData<JSONObject> getPredictionLiveData() {
        return predictionLiveData;
    }

    public void processImage(Bitmap image, String url) {
        progressLiveData.postValue(true);
        resultLiveData.postValue("");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        encoded = encoded.replaceAll("\\n", "");
        RequestBody body = RequestBody.create("{\"img\":\"" + encoded + "\"}", JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                progressLiveData.postValue(false);
                resultLiveData.postValue("Network Error");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                String res = Objects.requireNonNull(response.body()).string();
                try {
                    JSONArray jsonArray = new JSONArray(res);
                    JSONObject predictionResult = jsonArray.getJSONObject(0);  // get the first (and only) object
                    String label = predictionResult.getString("label");
                    resultLiveData.postValue("Classified as: " + label);
                    predictionLiveData.postValue(predictionResult);
                    progressLiveData.postValue(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void displayMask(JSONObject prediction) {
        try {
            String imageBase64 = prediction.getString("mask");

            // Decode the base64 string to Bitmap
            byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            imageLiveData.postValue(bitmap);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void displayBbox(JSONObject prediction, Bitmap originalImage) {
        try {
            JSONArray bounding_boxes = prediction.getJSONArray("bounding_boxes");

            Bitmap mutableBitmap = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);

            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);

            // Draw the bounding boxes
            for (int i = 0; i < bounding_boxes.length(); i++) {
                JSONObject box = bounding_boxes.getJSONObject(i);
                int x1 = box.getInt("x1");
                int y1 = box.getInt("y1");
                int x2 = box.getInt("x2");
                int y2 = box.getInt("y2");

                canvas.drawRect(x1, y1, x2, y2, paint);
            }

            // Update the imageLiveData with the modified bitmap on the main thread
            imageLiveData.postValue(mutableBitmap);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void reset() {
        resultLiveData.postValue("");
        imageLiveData.postValue(null);
        predictionLiveData.postValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        // Cancel all pending requests
        if (client != null) {
            client.dispatcher().cancelAll();
        }
    }
}
