/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.settings;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Matchers;

import org.powermock.api.mockito.PowerMockito;

/**
 * @author Iván Zaera
 */
public class ConfigurationSettingsTest extends PowerMockito {

	public ConfigurationSettingsTest() {
		_settings = mock(Settings.class);

		_configurationSettings = new ConfigurationSettings(_map, _settings);
	}

	@Test
	public void testGetValuesWhenFoundInRequest() {
		String[] values = {"request value 1", "request value 2"};

		_map.put("preferences--key--", values);

		Assert.assertArrayEquals(
			values,
			_configurationSettings.getValues(
				"key", new String[] {"default value"}));
	}

	@Test
	public void testGetValuesWhenFoundInSettings() {
		String[] values = {"settings value 1", "settings value 2"};

		mockSettingsGetValues("key", values);

		Assert.assertArrayEquals(
			values,
			_configurationSettings.getValues(
				"key", new String[] {"default value"}));
	}

	@Test
	public void testGetValueWhenFoundInRequest() {
		_map.put("preferences--key--", new String[]{"request value"});

		mockSettingsGetValue("key", "settings value");

		Assert.assertEquals(
			"request value",
			_configurationSettings.getValue("key", "default value"));
	}

	@Test
	public void testGetValueWhenFoundInSettings() {
		mockSettingsGetValue("key", "settings value");

		Assert.assertEquals(
			"settings value",
			_configurationSettings.getValue("key", "default value"));
	}

	protected void mockSettingsGetValue(String key, String value) {
		when(
			_settings.getValue(Matchers.eq(key), Matchers.anyString())
		).thenReturn(
			value
		);
	}

	protected void mockSettingsGetValues(String key, String... values) {
		when(
			_settings.getValues(Matchers.eq(key), Matchers.any(String[].class))
		).thenReturn(
			values
		);
	}

	private ConfigurationSettings _configurationSettings;
	private Map<String, String[]> _map = new HashMap<String, String[]>();
	private Settings _settings;

}