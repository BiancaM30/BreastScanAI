package app.ij.mlwithtensorflowlite;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.net.Uri;
import android.app.Instrumentation;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;


import app.ij.mlwithtensorflowlite.activities.UltrasoundActivity;

@RunWith(AndroidJUnit4.class)
public class UltrasoundActivityInstrumentationTest {

    @Rule
    public ActivityScenarioRule<UltrasoundActivity> scenarioRule
            = new ActivityScenarioRule<>(UltrasoundActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_EXTERNAL_STORAGE);

    public static Matcher<View> hasDrawable() {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("has drawable");
            }
            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof ImageView)) {
                    return false;
                }
                return ((ImageView) view).getDrawable() != null;
            }
        };
    }

    @Test
    public void testFileSelectionAndPredictionBenign() {
        Uri uriToTestFile = Uri.parse("file:///sdcard/DCIM/Ultra_benign/IMG-20221217-WA0018(1).jpg");

        Intent resultData = new Intent();
        resultData.setData(uriToTestFile);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        intending(not(isInternal())).respondWith(result);

        onView(withId(R.id.button_upload_ultrasound)).perform(click());
        onView(withId(R.id.imageView_ultrasound)).check(matches(hasDrawable()));
        onView(withId(R.id.button_make_prediction_ultrasound)).perform(click());
        onView(withId(R.id.result_ultrasound)).check(matches(withText("Classified as: Benign")));

        onView(withId(R.id.button_get_mask_ultrasound)).perform(click());
        onView(withId(R.id.imageView_ultrasound)).check(matches(hasDrawable()));
    }

    @Test
    public void testFileSelectionAndPredictionMalignant() {
        Uri uriToTestFile = Uri.parse("file:///sdcard/DCIM/Ultra_malignant/malignant (208).png");

        Intent resultData = new Intent();
        resultData.setData(uriToTestFile);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        intending(not(isInternal())).respondWith(result);

        onView(withId(R.id.button_upload_ultrasound)).perform(click());
        onView(withId(R.id.imageView_ultrasound)).check(matches(hasDrawable()));
        onView(withId(R.id.button_make_prediction_ultrasound)).perform(click());
        onView(withId(R.id.result_ultrasound)).check(matches(withText("Classified as: Malignant")));

        onView(withId(R.id.button_get_mask_ultrasound)).perform(click());
        onView(withId(R.id.imageView_ultrasound)).check(matches(hasDrawable()));
    }

    @Test
    public void testFileSelectionAndPredictionNormal() {
        Uri uriToTestFile = Uri.parse("file:///sdcard/DCIM/Ultra_normal/IMG-20221217-WA0007(1).jpg");

        Intent resultData = new Intent();
        resultData.setData(uriToTestFile);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        intending(not(isInternal())).respondWith(result);

        onView(withId(R.id.button_upload_ultrasound)).perform(click());
        onView(withId(R.id.imageView_ultrasound)).check(matches(hasDrawable()));
        onView(withId(R.id.button_make_prediction_ultrasound)).perform(click());
        onView(withId(R.id.result_ultrasound)).check(matches(withText("Classified as: Normal")));

        onView(withId(R.id.button_get_mask_ultrasound)).perform(click());
        onView(withId(R.id.imageView_ultrasound)).check(matches(hasDrawable()));
    }
}
