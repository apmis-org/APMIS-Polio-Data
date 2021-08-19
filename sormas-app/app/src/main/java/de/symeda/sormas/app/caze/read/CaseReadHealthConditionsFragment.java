/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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

package de.symeda.sormas.app.caze.read;

import android.os.Bundle;

import de.symeda.sormas.api.utils.fieldvisibility.FieldVisibilityCheckers;
import de.symeda.sormas.api.utils.fieldvisibility.checkers.CountryFieldVisibilityChecker;
import de.symeda.sormas.app.BaseReadHealthConditionsFragment;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.config.ConfigProvider;

public class CaseReadHealthConditionsFragment extends BaseReadHealthConditionsFragment<Case> {

	public static final String TAG = CaseReadHealthConditionsFragment.class.getSimpleName();

	private Case caze;

	public static CaseReadHealthConditionsFragment newInstance(Case activityRootData) {
		return newInstanceWithFieldCheckers(
			CaseReadHealthConditionsFragment.class,
			null,
			activityRootData,
			FieldVisibilityCheckers.withDisease(activityRootData.getDisease())
				.add(new CountryFieldVisibilityChecker(ConfigProvider.getServerLocale())),
			null);
	}

	@Override
	protected void prepareFragmentData(Bundle savedInstanceState) {
		caze = getActivityRootData();
		record = caze.getClinicalCourse().getHealthConditions();
	}
}
