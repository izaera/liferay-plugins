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

package com.liferay.portal.settings.impl.archive;

import java.io.IOException;

import com.liferay.portal.settings.archive.ArchivedSettings;
import com.liferay.portal.settings.PortletPreferencesSettings;
import com.liferay.portal.settings.Settings;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.service.PortletPreferencesServiceUtil;
import javax.portlet.PortletPreferences;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.PortletConstants;
import com.liferay.portal.model.PortletItem;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.util.PortletKeys;
import java.util.Date;

/**
 * @author Iván Zaera
 */
public class ArchivedSettingsImpl implements ArchivedSettings {
	
	private Settings _settings;
	private PortletItem _portletItem;

	public ArchivedSettingsImpl(PortletItem portletItem) {
		_portletItem = portletItem;
	}

	public String getName() {
		return _portletItem.getName();
	}
	
	public String getUserName() {
		return _portletItem.getUserName();
	}
	
	public Date getModifiedDate() {
		return _portletItem.getModifiedDate();
	}

	public Settings getSettings() throws IOException {
		if (_settings == null) {
			long ownerId = _portletItem.getPortletItemId();
			int ownerType = PortletKeys.PREFS_OWNER_TYPE_ARCHIVED;
			long plid = 0;
			String portletId = _portletItem.getPortletId();
	
			PortletPreferences archivedPreferences = null;
			
			try {
				archivedPreferences = 
					PortletPreferencesLocalServiceUtil.getPreferences(
						_portletItem.getCompanyId(), ownerId, ownerType, plid,
						PortletConstants.getRootPortletId(portletId));
			} 
			catch (SystemException se) {
				throw new IOException("Unable to retrieve named settings", se);

			}
			
			_settings = new PortletPreferencesSettings(archivedPreferences);

		}
		
		return _settings; 
	}

	public void delete() throws IOException {
		try {
			PortletPreferencesServiceUtil.deleteArchivedPreferences(
				_portletItem.getPortletItemId());
		} 
		catch (PortalException pe) {
			throw new IOException("Unable to delete archived settings", pe);
		} 
		catch (SystemException se) {
			throw new IOException("Unable to delete archived settings", se);
		}
	}
	
}
