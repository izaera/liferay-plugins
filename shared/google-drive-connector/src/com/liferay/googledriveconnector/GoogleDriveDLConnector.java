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

import com.liferay.googledriveconnector.util.Constants;
import com.liferay.googledriveconnector.util.OSGiUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portlet.documentlibrary.connector.BaseDLConnector;
import com.liferay.portlet.documentlibrary.connector.DLConnector;
import com.liferay.portlet.documentlibrary.connector.capabilities.AddDocumentsCapability;
import com.liferay.portlet.documentlibrary.model.DLFileEntryType;
import com.liferay.portlet.documentlibrary.service.DLFileEntryTypeLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFileEntryTypeServiceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.osgi.service.component.annotations.Activate;
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

	@Activate
	public void activate() throws PortalException {
		List<Company> companies = CompanyLocalServiceUtil.getCompanies();

		for (Company company : companies) {
			addGoogleDocumentFileEntryType(company);
		}
	}

	@Override
	public String getLocalizedName(Locale locale) {
		return getName();
	}

	@Override
	public String getName() {
		return "Google Drive";
	}

	protected void addGoogleDocumentFileEntryType(Company company)
		throws PortalException,
			SystemException {

		long groupId = company.getGroupId();

		DLFileEntryType dlFileEntryType =
			_fetchGoogleDriveDocumentFileEntryType(groupId);

		if (dlFileEntryType == null) {
			dlFileEntryType = _createGoogleDriveDocumentFileEntryType(company);
		}
	}

	private DLFileEntryType _createGoogleDriveDocumentFileEntryType(
			Company company)
		throws PortalException {

		User defaultUser = company.getDefaultUser();

		Locale defaultLocale = LocaleUtil.getDefault();

		Map<Locale, String> nameMap = new HashMap<Locale, String>();

		nameMap.put(defaultLocale, Constants.GOOGLE_DOCUMENT_NAME);

		Map<Locale, String> descriptionMap = new HashMap<Locale, String>();

		descriptionMap.put(defaultLocale, Constants.GOOGLE_DOCUMENT_NAME);

		long[] ddmStructureIds = new long[] {};

		ServiceContext serviceContext = new ServiceContext();

		String xsd = OSGiUtil.get(
			getClass(),
			"com/liferay/googledriveconnector/GoogleDocument_DDM_XSD.xml");

		serviceContext.setAttribute("definition", xsd);

		return DLFileEntryTypeLocalServiceUtil.addFileEntryType(
			defaultUser.getUserId(), company.getGroupId(),
			Constants.GOOGLE_DOCUMENT_FILE_ENTRY_TYPE_KEY, nameMap,
			descriptionMap, ddmStructureIds, serviceContext);
	}

	private DLFileEntryType _fetchGoogleDriveDocumentFileEntryType(
		long groupId) {

		long[] groupIds = new long[] { groupId };

		List<DLFileEntryType> fileEntryTypes =
			DLFileEntryTypeServiceUtil.getFileEntryTypes(groupIds);

		for (DLFileEntryType fileEntryType : fileEntryTypes) {
			if (fileEntryType.getFileEntryTypeKey().equals(
					Constants.GOOGLE_DOCUMENT_FILE_ENTRY_TYPE_KEY)) {

				return fileEntryType;
			}
		}

		return null;
	}

}