/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.app.immunization.vaccination;

import android.content.Context;

import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.app.BaseReadActivity;
import de.symeda.sormas.app.BaseReadFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.vaccination.VaccinationEntity;
import de.symeda.sormas.app.component.menu.PageMenuItem;

public class VaccinationReadActivity extends BaseReadActivity<VaccinationEntity> {

	public static void startActivity(Context context, String rootUuid) {
		BaseReadActivity.startActivity(context, VaccinationReadActivity.class, buildBundle(rootUuid));
	}

	public static void startActivity(Context context, String rootUuid, boolean finishInsteadOfUpNav) {
		BaseReadActivity.startActivity(context, VaccinationReadActivity.class, buildBundle(rootUuid, finishInsteadOfUpNav));
	}

	@Override
	protected VaccinationEntity queryRootEntity(String recordUuid) {
		return DatabaseHelper.getVaccinationDao().queryUuid(recordUuid);
	}

	@Override
	protected BaseReadFragment buildReadFragment(PageMenuItem menuItem, VaccinationEntity activityRootData) {
		final VaccinationSection section = VaccinationSection.fromOrdinal(menuItem.getPosition());
		BaseReadFragment fragment;
		switch (section) {
		case VACCINATION_INFO:
			fragment = VaccinationReadFragment.newInstance(activityRootData);
			break;
		case HEALTH_CONDITIONS:
			fragment = VaccinationReadHealthConditionsFragment.newInstance(activityRootData);
			break;
		default:
			throw new IndexOutOfBoundsException(DataHelper.toStringNullable(section));
		}

		return fragment;
	}

	@Override
	public void goToEditView() {
		VaccinationEditActivity.startActivity(this, getRootUuid());
	}

	@Override
	public Enum getPageStatus() {
		return null;
	}

	@Override
	protected int getActivityTitle() {
		return R.string.heading_vaccination;
	}
}
