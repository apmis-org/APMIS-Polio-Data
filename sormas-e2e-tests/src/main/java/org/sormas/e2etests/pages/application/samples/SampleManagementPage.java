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

package org.sormas.e2etests.pages.application.samples;

import org.openqa.selenium.By;

public class SampleManagementPage {
  public static final By SAMPLE_SEARCH_INPUT = By.cssSelector("[id='caseCodeIdLike']");
  public static final By SAMPLE_EDIT_PURPOSE_OPTIONS =
      By.cssSelector("#samplePurpose .v-select-option");
  public static final By SAMPLE_EDIT_PURPOSE_SELECTED_OPTION =
      By.xpath("//input[@checked]/following-sibling::label");
  public static final By POPUP_CONTENT = By.cssSelector("[role='dialog'] .popupContent");
  public static final By TEST_RESULTS_SEARCH_COMBOBOX =
      By.cssSelector("[id='pathogenTestResult'] [class='v-filterselect-button']");
  public static final By TEST_RESULTS_SEARCH_INPUT =
      By.cssSelector("[id='pathogenTestResult'] input");
  public static final By SPECIMEN_CONDITION_SEARCH_COMBOBOX =
      By.cssSelector("[id='specimenCondition'][class='v-filterselect-button']");
  public static final By SPECIMEN_CONDITION_SEARCH_INPUT =
      By.cssSelector("[id='specimenCondition'] input");
  public static final By CASE_CLASSIFICATION_SEARCH_COMBOBOX =
      By.cssSelector("[id='caseClassification'] [class='v-filterselect-button']");
  public static final By CASE_CLASSIFICATION_SEARCH_INPUT =
      By.cssSelector("[id='caseClassification'] input");
  public static final By DISEASE_SEARCH_COMBOBOX =
      By.cssSelector("[id='disease'] [class='v-filterselect-button']");
  public static final By DISEASE_SEARCH_INPUT = By.cssSelector("[id='disease'] input");
  public static final By REGION_SEARCH_COMBOBOX =
      By.cssSelector("[id='region'] [class='v-filterselect-button']");
  public static final By REGION_SEARCH_INPUT = By.cssSelector("[id='region'] input");
  public static final By DISTRICT_SEARCH_COMBOBOX =
      By.cssSelector("[id='district'] [class='v-filterselect-button']");
  public static final By DISTRICT_SEARCH_INPUT = By.cssSelector("[id='district'] input");
  public static final By LABORATORY_SEARCH_COMBOBOX =
      By.cssSelector("[id='laboratory'] [class='v-filterselect-button']");
  public static final By LABORATORY__SEARCH_INPUT = By.cssSelector("[id='laboratory'] input");
  public static final By RESET_FILTER_BUTTON = By.cssSelector("[id='actionResetFilters']");
  public static final By APPLY_FILTER_BUTTON = By.cssSelector("[id='actionApplyFilters']");
  public static final By RESULT_VERIFIED_BY_LAB_SUPERVISOR_EDIT_OPTIONS =
      By.cssSelector(".popupContent #testResultVerified .v-select-option");
  public static final By RESULT_VERIFIED_BY_LAB_SUPERVISOR_OPTIONS_SELECTED_OPTION =
      By.xpath("//div[@class='popupContent'] //input[@checked]/following-sibling::label");
  public static final By SEARCH_RESULT_SAMPLE = By.cssSelector("[role='gridcell'] a");
  public static final By EDIT_TEST_RESULTS_BUTTON =
      By.cssSelector("[location='pathogenTests'] [class='v-slot v-slot-s-list'] [role='button']");
  public static final By SAVE_EDIT_SAMPLE_BUTTON = By.cssSelector("[id='commit']");
}
