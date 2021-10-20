package com.comp6442.route42.data;

import static org.mockito.Mockito.mock;

import android.location.Location;
import android.os.Parcel;

import com.comp6442.route42.ui.viewmodel.ActiveMapViewModel;
import com.comp6442.route42.data.model.Activity;
import com.comp6442.route42.data.model.BaseActivity;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ActiveMapViewModelTest {

    ActiveMapViewModel activeMapViewModel = new ActiveMapViewModel();
    long elapsedTime = 0;
    Date date1 = new Date(2021, 10, 19);
    Date date2 = new Date(2021, 10, 20);
    Date lastUpdateTime = new Date(2021, 10, 18);
    final List<LatLng> pastLocation = Arrays.asList(//past location check
            new LatLng(-35.25932077515105, 149.11459641897002),
            new LatLng(-35.25918060533768, 149.11596970986986));
    final List<LatLng> newLocation = Arrays.asList(//past location check
            new LatLng(-35.25918060553768, 149.11596970996986));
    Activity activity;
    Location location;
    @Before
    public void setUp() {
        activeMapViewModel.setPastLocations(pastLocation);
        activeMapViewModel.setSnapshotFileName("TestLocation");//filename check
        activeMapViewModel.setLastUpdateTime(date1);//updatedate check
        activity =  mock(Activity.class);
        location = mock(Location.class);
    }
    @Test
    public void typeTest() {
        activeMapViewModel.setActivityType(Activity.Activity_Type.RUNNING);//type check
        Assert.assertTrue(activeMapViewModel.getActivityType().equals(Activity.Activity_Type.RUNNING));
        activeMapViewModel.setActivityType(Activity.Activity_Type.CYCLING);//type check
        Assert.assertTrue(activeMapViewModel.getActivityType().equals(Activity.Activity_Type.CYCLING));
        activeMapViewModel.setActivityType(Activity.Activity_Type.WALKING);//type check
        Assert.assertTrue(activeMapViewModel.getActivityType().equals(Activity.Activity_Type.WALKING));
    }
    @Test
    public void elapsedTimeTest() {
        Assert.assertTrue(activeMapViewModel.getElapsedTime()==0);
        elapsedTime = elapsedTime + BaseActivity.getElapsedTimeSeconds(date2, lastUpdateTime);
        Assert.assertEquals(elapsedTime,172800);
    }

    @Test
    public void lastUpdateTimeTest() {
        Assert.assertTrue(activeMapViewModel.getLastUpdateTime()==date1);
    }

    @Test
    public void resetTest() {
        activeMapViewModel.reset();//reset check
        Assert.assertEquals(activeMapViewModel.getElapsedTime(),0);
        Assert.assertEquals(activeMapViewModel.getLastUpdateTime(),null);
        Assert.assertEquals(activeMapViewModel.getPastLocations(),new ArrayList<>());
        Assert.assertEquals(activeMapViewModel.getSnapshotFileName(),null);
        Assert.assertEquals(activeMapViewModel.getActivityData(),null);
        Assert.assertEquals(activeMapViewModel.getActivityType(),null);
        Assert.assertEquals(activeMapViewModel.hasPastLocations(),false);
    }

    @Test
    public void pastLocationTest() {
        Assert.assertEquals(activeMapViewModel.hasPastLocations(),true);
        Assert.assertEquals(activeMapViewModel.getPastLocations(),pastLocation);
    }

    @Test
    public void snapShotFileNameTest() {
        Assert.assertEquals(activeMapViewModel.getSnapshotFileName(),"TestLocation");
    }

    @Test
    public void activityTest() {
        activeMapViewModel.setActivityData(activity);
        Assert.assertEquals(activeMapViewModel.getActivityData(),activity);
    }
}
