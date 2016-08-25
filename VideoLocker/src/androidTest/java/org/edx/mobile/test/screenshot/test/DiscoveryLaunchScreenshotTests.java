package org.edx.mobile.test.screenshot.test;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.facebook.testing.screenshot.Screenshot;

import org.edx.mobile.R;
import org.edx.mobile.test.EdxInstrumentationTestApplication;
import org.edx.mobile.view.DiscoveryLaunchActivity;
import org.edx.mobile.view.DiscoveryLaunchPresenter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DiscoveryLaunchScreenshotTests {

    @Rule
    public ActivityTestRule<DiscoveryLaunchActivity> mActivityRule =
            new ActivityTestRule<>(DiscoveryLaunchActivity.class, false, false);

    @Test
    public void testScreenshot_recordLaunchActivity() throws Throwable {
        ((EdxInstrumentationTestApplication)InstrumentationRegistry.getTargetContext().getApplicationContext()).setNextPresenter(
                new DiscoveryLaunchPresenter()
        );
        mActivityRule.launchActivity(null);
        View view = mActivityRule.getActivity().findViewById(android.R.id.content);
        Screenshot.snap(view).record();
    }
}
