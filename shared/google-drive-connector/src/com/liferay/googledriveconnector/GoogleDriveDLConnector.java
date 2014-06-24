/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.googledriveconnector;

import com.liferay.portlet.documentlibrary.connector.BaseDLConnector;
import com.liferay.portlet.documentlibrary.connector.DLConnector;
import com.liferay.portlet.documentlibrary.connector.capabilities.AddDocumentsCapability;

import java.util.Locale;
import java.util.UUID;

import org.osgi.service.component.annotations.Component;

/**
 * @author Iván Zaera
 */
@Component(service = DLConnector.class)
public class GoogleDriveDLConnector extends BaseDLConnector {

	public static final UUID ID = UUID.fromString(
		"56244EF0-EFC6-11E3-AC10-0800200C9A66");

	public GoogleDriveDLConnector() {
		super(ID);

		addCapability(
			AddDocumentsCapability.class,
			new GoogleDriveAddDocumentsCapability());
	}

	@Override
	public String getLocalizedName(Locale locale) {
		return getName();
	}

	@Override
	public String getName() {
		return "Google Drive";
	}

}