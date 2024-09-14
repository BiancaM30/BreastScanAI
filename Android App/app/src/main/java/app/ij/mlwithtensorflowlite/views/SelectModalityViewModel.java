package app.ij.mlwithtensorflowlite.views;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SelectModalityViewModel extends ViewModel {

    private MutableLiveData<Boolean> navigateToUltrasound = new MutableLiveData<>();
    private MutableLiveData<Boolean> navigateToMammography = new MutableLiveData<>();

    public LiveData<Boolean> getNavigateToUltrasound() {
        return navigateToUltrasound;
    }

    public LiveData<Boolean> getNavigateToMammography() {
        return navigateToMammography;
    }

    public void onUltrasoundButtonClicked() {
        navigateToUltrasound.setValue(true);
    }

    public void onMammographyButtonClicked() {
        navigateToMammography.setValue(true);
    }
}
