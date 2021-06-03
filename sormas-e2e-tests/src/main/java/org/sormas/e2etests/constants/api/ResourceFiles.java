/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.sormas.e2etests.constants.api;

public interface ResourceFiles {
  String POST_CASES_BASIC_JSON_BODY = "src/test/resources/JsonFiles/PostCase-Basic.json";
  String POST_IN_COUNTRY_NO_HOSPITALIZATION_CASES_JSON_BODY =
      "src/test/resources/JsonFiles/InCountry-NoHospitalization-PostCase.json";
  String POST_PERSON_JSON_BODY = "src/test/resources/JsonFiles/Person-Post.json";
}