/*
 * WiFi Analyzer
 * Copyright (C) 2016  VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.vrem.wifianalyzer.navigation;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.vrem.wifianalyzer.MainActivity;
import com.vrem.wifianalyzer.MainContextHelper;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.band.WiFiWidth;
import com.vrem.wifianalyzer.wifi.model.WiFiConnection;
import com.vrem.wifianalyzer.wifi.model.WiFiData;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.model.WiFiSignal;
import com.vrem.wifianalyzer.wifi.scanner.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExportItemTest {
    @Mock
    Intent sendIntent;
    @Mock
    Intent chooserIntent;
    @Mock
    private MainActivity mainActivity;
    @Mock
    private Resources resources;
    @Mock
    private MenuItem menuItem;
    @Mock
    private PackageManager packageManager;
    @Mock
    private ComponentName componentName;

    private ExportItem fixture;
    private String sendTitle;
    private Scanner scanner;
    private WiFiDetail wiFiDetail;

    @Before
    public void setUp() throws Exception {
        scanner = MainContextHelper.INSTANCE.getScanner();

        sendTitle = "title";
        wiFiDetail = new WiFiDetail("SSID", "BSSID", "capabilities", new WiFiSignal(2412, 2422, WiFiWidth.MHZ_40, -40));

        fixture = new ExportItem() {
            @Override
            Intent createSendIntent() {
                return sendIntent;
            }

            @Override
            Intent createChooserIntent(@NonNull Intent intent, @NonNull String title) {
                assertEquals(sendIntent, intent);
                assertEquals(sendTitle, title);
                return chooserIntent;
            }
        };
    }

    @After
    public void tearDown() {
        MainContextHelper.INSTANCE.restore();
    }


    @Test
    public void testActivate() throws Exception {
        // setup
        WiFiData wiFiData = withWiFiData();
        String sendData = fixture.getData(wiFiData.getWiFiDetails());
        when(scanner.getWiFiData()).thenReturn(wiFiData);
        withResources();
        withResolveActivity();
        // execute
        fixture.activate(mainActivity, menuItem, NavigationMenu.EXPORT);
        // validate
        verify(scanner).getWiFiData();
        verifyResources();
        verifyResolveActivity();
        verifySendIntentInformation(sendData);
        verify(mainActivity).startActivity(chooserIntent);
    }

    @Test
    public void testGetData() throws Exception {
        // setup
        WiFiData wiFiData = withWiFiData();
        String expected =
            "SSID|BSSID|Strength|Primary Channel|Primary Frequency|Center Channel|Center Frequency|Width (Range)|Distance|Security"
                + System.lineSeparator()
                + "SSID|BSSID|-40dBm|1|2412MHz|3|2422MHz|40MHz (2402 - 2442)|1.0m|capabilities"
                + System.lineSeparator();
        // execute
        String actual = fixture.getData(wiFiData.getWiFiDetails());
        // validate
        assertEquals(expected, actual);
    }

    @NonNull
    private WiFiData withWiFiData() {
        return new WiFiData(Arrays.asList(wiFiDetail), WiFiConnection.EMPTY, new ArrayList<String>());
    }

    private void verifySendIntentInformation(String sendData) {
        verify(sendIntent).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        verify(sendIntent).setType("text/plain");
        verify(sendIntent).putExtra(Intent.EXTRA_TITLE, sendTitle);
        verify(sendIntent).putExtra(Intent.EXTRA_SUBJECT, sendTitle);
        verify(sendIntent).putExtra(Intent.EXTRA_TEXT, sendData);
    }

    private void withResolveActivity() {
        when(mainActivity.getPackageManager()).thenReturn(packageManager);
        when(chooserIntent.resolveActivity(packageManager)).thenReturn(componentName);
    }

    private void verifyResolveActivity() {
        verify(mainActivity).getPackageManager();
        verify(chooserIntent).resolveActivity(packageManager);
    }

    private void withResources() {
        when(mainActivity.getResources()).thenReturn(resources);
        when(resources.getString(R.string.action_access_points)).thenReturn(sendTitle);
    }

    private void verifyResources() {
        verify(mainActivity).getResources();
        verify(resources).getString(R.string.action_access_points);
    }

}