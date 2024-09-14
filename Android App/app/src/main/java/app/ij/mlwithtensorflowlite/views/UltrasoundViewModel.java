package app.ij.mlwithtensorflowlite.views;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import app.ij.mlwithtensorflowlite.ml.Model;
import app.ij.mlwithtensorflowlite.ml.Masks;

public class UltrasoundViewModel extends ViewModel {

    private final MutableLiveData<Bitmap> imageLiveData ;
    private final MutableLiveData<String> resultLiveData;
    private static final String[] CLASSES = {"Benign", "Malignant", "Normal"};

    public UltrasoundViewModel() {
        resultLiveData = new MutableLiveData<>();
        imageLiveData = new MutableLiveData<>();
    }

    public LiveData<Bitmap> getImageLiveData() {
        return imageLiveData;
    }

    public LiveData<String> getResultLiveData() {
        return resultLiveData;
    }

    public void classifyImage(Bitmap image, Model model) {
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 400, 400, 3}, DataType.FLOAT32);
        int imageSize = 400;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[imageSize * imageSize];
        image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        int pixel = 0;
        for (int i = 0; i < imageSize; i++) {
            for (int j = 0; j < imageSize; j++) {
                int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255));
                byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255));
                byteBuffer.putFloat((val & 0xFF) * (1.f / 255));
            }
        }
        inputFeature0.loadBuffer(byteBuffer);
        Model.Outputs outputs = model.process(inputFeature0);
        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

        float[] confidences = outputFeature0.getFloatArray();
        int maxPos = 0;
        float maxConfidence = 0;
        for (int i = 0; i < confidences.length; i++) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i];
                maxPos = i;
            }
        }
        String classifiedAs = CLASSES[maxPos];
        resultLiveData.setValue(classifiedAs);
    }

    public Bitmap getMask(Bitmap image, Masks model) {
        Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 1}, DataType.FLOAT32);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 256 * 256);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[256 * 256];
        image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        int pixel = 0;
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                int val = intValues[pixel++];
                byteBuffer.putFloat((val >> 16) & 0xFF);
            }
        }

        inputFeature0.loadBuffer(byteBuffer);
        Masks.Outputs outputs = model.process(inputFeature0);
        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

        ByteBuffer buf = outputFeature0.getBuffer();
        buf.rewind();
        for (int i = 0; i < 256 * 256; i++) {
            float value = buf.getFloat();
            int color = value > 0.5 ? Color.WHITE : Color.BLACK;
            bitmap.setPixel(i % 256, i / 256, color);
        }
        return bitmap;
    }

    public Bitmap prepareMaskForClassification(Bitmap mask) {
        Bitmap rgbMask = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
        for (int x = 0; x < mask.getWidth(); x++) {
            for (int y = 0; y < mask.getHeight(); y++) {
                int pixel = mask.getPixel(x, y);
                int gray = (pixel & 0xff) > 128 ? 255 : 0;
                rgbMask.setPixel(x, y, Color.rgb(gray, gray, gray));
            }
        }
        return Bitmap.createScaledBitmap(rgbMask, 400, 400, false);
    }
}
