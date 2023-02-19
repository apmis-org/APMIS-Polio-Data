/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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
 *******************************************************************************/
package de.symeda.sormas.ui.dashboard.components;

import static de.symeda.sormas.ui.utils.AbstractFilterForm.FILTER_ITEM_STYLE;
import static de.symeda.sormas.ui.utils.LayoutUtil.filterLocs;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.ComboBox;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.dashboard.AbstractDashboardView;
import de.symeda.sormas.ui.dashboard.DashboardDataProvider;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.ComboBoxHelper;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.DateFormatHelper;

@SuppressWarnings("serial")
public abstract class DashboardFilterLayout extends HorizontalLayout {

	public static final String DATE_FILTER = "dateFilter";
	public static final String REGION_FILTER = "regionFilter";
	public static final String DISTRICT_FILTER = "districtFilter";
	private static final String RESET_AND_APPLY_BUTTONS = "resetAndApplyButtons";

	protected AbstractDashboardView dashboardView;
	protected DashboardDataProvider dashboardDataProvider;
	private CustomLayout customLayout;

	// Filters
	private ComboBox regionFilter;
	private ComboBox districtFilter;
	private PopupButton btnCurrentPeriod;
	private PopupButton btnComparisonPeriod;
	private Set<Button> dateFilterButtons;
	private Set<Button> dateComparisonButtons;

	// Buttons
	private Button btnShowCustomPeriod;
	private Button btnToday;
	private Button btnYesterday;
	private Button btnThisWeek;
	private Button btnLastWeek;
	private Button btnThisYear;
	private Button btnPeriodBefore;
	private Button btnPeriodLastYear;
	private Button activeComparisonButton;
	private Button resetButton;
	private Button applyButton;

	private DateFilterType currentDateFilterType;

	private HorizontalLayout customDateFilterLayout;

	private Runnable dateFilterChangeCallback;

	public DashboardFilterLayout(AbstractDashboardView dashboardView, DashboardDataProvider dashboardDataProvider, String[] templateContent) {
		this.dashboardView = dashboardView;
		this.dashboardDataProvider = dashboardDataProvider;
		this.regionFilter = ComboBoxHelper.createComboBoxV7();
		this.districtFilter = ComboBoxHelper.createComboBoxV7();
		dateFilterButtons = new HashSet<>();
		dateComparisonButtons = new HashSet<>();

		setSpacing(true);
		setSizeUndefined();
		setMargin(new MarginInfo(true, true, false, true));

		String[] templateLocs = new String[] {
			DATE_FILTER,
			RESET_AND_APPLY_BUTTONS };
		templateLocs = ArrayUtils.insert(1, templateLocs, templateContent);

		customLayout = new CustomLayout();
		customLayout.setTemplateContents(filterLocs(templateLocs));

		addComponent(customLayout);
		populateLayout();
	}

	public void populateLayout() {
		createResetAndApplyButtons();
	};

	public void createRegionAndDistrictFilter() {
		// Region filter
		if (UserProvider.getCurrent().getUser().getRegion() == null) {
			regionFilter.setWidth(200, Unit.PIXELS);
			regionFilter.setInputPrompt(I18nProperties.getString(Strings.promptRegion));
			regionFilter.addItems(FacadeProvider.getRegionFacade().getAllActiveByServerCountry());
			regionFilter.addValueChangeListener(e -> {
				dashboardDataProvider.setRegion((RegionReferenceDto) regionFilter.getValue());
			});
			// save height
			// regionFilter.setCaption(I18nProperties.getString(Strings.entityRegion));
			addCustomComponent(regionFilter, REGION_FILTER);
			dashboardDataProvider.setRegion((RegionReferenceDto) regionFilter.getValue());
		}

		// District filter
		if (UserProvider.getCurrent().getUser().getRegion() != null && UserProvider.getCurrent().getUser().getDistrict() == null) {
			districtFilter.setWidth(200, Unit.PIXELS);
			districtFilter.setInputPrompt(I18nProperties.getString(Strings.promptDistrict));
			districtFilter
				.addItems(FacadeProvider.getDistrictFacade().getAllActiveByRegion(UserProvider.getCurrent().getUser().getRegion().getUuid()));
			districtFilter.addValueChangeListener(e -> {
				dashboardDataProvider.setDistrict((DistrictReferenceDto) districtFilter.getValue());
			});
			// save height
			//districtFilter.setCaption(I18nProperties.getString(Strings.entityDistrict));
			addCustomComponent(districtFilter, DISTRICT_FILTER);
			dashboardDataProvider.setDistrict((DistrictReferenceDto) districtFilter.getValue());
		}
	}

	public void createResetAndApplyButtons() {
		HorizontalLayout buttonLayout = new HorizontalLayout();
		Button.ClickListener resetListener = e -> dashboardView.navigateTo(null);
		resetButton = ButtonHelper.createButton(Captions.actionResetFilters, resetListener, CssStyles.BUTTON_FILTER_LIGHT);
		buttonLayout.addComponent(resetButton);
		Button.ClickListener applyListener = e -> dashboardView.refreshDashboard();
		applyButton = ButtonHelper.createButton(Captions.actionApplyFilters, applyListener, CssStyles.BUTTON_FILTER_LIGHT);
		applyButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		applyButton.addClickListener(e -> {
			if (getDateFilterChangeCallback() != null) {
				getDateFilterChangeCallback().run();
			}
		});
		buttonLayout.addComponent(applyButton);
		addCustomComponent(buttonLayout, RESET_AND_APPLY_BUTTONS);
	}

	

	private HorizontalLayout createDateFilterButtonsLayout() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		// Date filters
		btnShowCustomPeriod =
			createAndAddDateFilterButton(Captions.dashboardCustom, I18nProperties.getCaption(Captions.dashboardCustom), dateFilterButtons);

		btnToday = createAndAddDateFilterButton(
			Captions.dashboardToday,
			String.format(I18nProperties.getCaption(Captions.dashboardToday), DateFormatHelper.formatDate(new Date())),
			dateFilterButtons);
		btnToday.addClickListener(e -> {
			currentDateFilterType = DateFilterType.TODAY;
			Date now = new Date();
			Date from = DateHelper.getStartOfDay(now);
			Date to = now;
			setDateFilter(from, to);
			btnCurrentPeriod.setCaption(btnToday.getCaption());
			updateComparisonButtons(DateFilterType.TODAY, from, to, false);
		});

		btnYesterday = createAndAddDateFilterButton(
			Captions.dashboardYesterday,
			String
				.format(I18nProperties.getCaption(Captions.dashboardYesterday), DateFormatHelper.formatDate(DateHelper.subtractDays(new Date(), 1))),
			dateFilterButtons);
		btnYesterday.addClickListener(e -> {
			currentDateFilterType = DateFilterType.YESTERDAY;
			Date now = new Date();
			Date from = DateHelper.getStartOfDay(DateHelper.subtractDays(now, 1));
			Date to = DateHelper.getEndOfDay(DateHelper.subtractDays(now, 1));
			setDateFilter(from, to);
			btnCurrentPeriod.setCaption(btnYesterday.getCaption());
			updateComparisonButtons(DateFilterType.YESTERDAY, from, to, false);
		});

		btnThisWeek = createAndAddDateFilterButton(
			Captions.dashboardThisWeek,
			String.format(
				I18nProperties.getCaption(Captions.dashboardThisWeek),
				DateHelper.getEpiWeek(new Date()).toString(new Date(), I18nProperties.getUserLanguage())),
			dateFilterButtons);
		btnThisWeek.addClickListener(e -> {
			currentDateFilterType = DateFilterType.THIS_WEEK;
			Date now = new Date();
			Date from = DateHelper.getStartOfWeek(now);
			Date to = now;
			setDateFilter(from, to);
			btnCurrentPeriod.setCaption(btnThisWeek.getCaption());
			updateComparisonButtons(DateFilterType.THIS_WEEK, from, to, false);
		});

		btnLastWeek = createAndAddDateFilterButton(
			Captions.dashboardLastWeek,
			String.format(
				I18nProperties.getCaption(Captions.dashboardLastWeek),
				DateHelper.getPreviousEpiWeek(new Date()).toString(I18nProperties.getUserLanguage())),
			dateFilterButtons);
		btnLastWeek.addClickListener(e -> {
			currentDateFilterType = DateFilterType.LAST_WEEK;
			Date now = new Date();
			Date from = DateHelper.getStartOfWeek(DateHelper.subtractWeeks(now, 1));
			Date to = DateHelper.getEndOfWeek(DateHelper.subtractWeeks(now, 1));
			setDateFilter(from, to);
			btnCurrentPeriod.setCaption(btnLastWeek.getCaption());
			updateComparisonButtons(DateFilterType.LAST_WEEK, from, to, false);
		});

		btnThisYear = createAndAddDateFilterButton(
			Captions.dashboardThisYear,
			String.format(
				I18nProperties.getCaption(Captions.dashboardThisYear),
				DateFormatHelper.buildPeriodString(DateHelper.getStartOfYear(new Date()), new Date())),
			dateFilterButtons);
		btnThisYear.addClickListener(e -> {
			currentDateFilterType = DateFilterType.THIS_YEAR;
			Date now = new Date();
			Date from = DateHelper.getStartOfYear(now);
			Date to = now;
			setDateFilter(from, to);
			btnCurrentPeriod.setCaption(btnThisYear.getCaption());
			updateComparisonButtons(DateFilterType.THIS_YEAR, from, to, false);
		});

		layout.addComponents(btnShowCustomPeriod, btnToday, btnYesterday, btnThisWeek, btnLastWeek, btnThisYear);

		return layout;
	}


	private Component createDateComparisonButtonsLayout() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		btnPeriodBefore = createAndAddDateFilterButton(Captions.dashboardDayBefore, null, dateComparisonButtons);
		btnPeriodBefore.addClickListener(e -> {
			activeComparisonButton = btnPeriodBefore;
			updateComparisonDates();
			btnComparisonPeriod.setCaption(btnPeriodBefore.getCaption());
		});

		btnPeriodLastYear = createAndAddDateFilterButton(Captions.dashboardSameDayLastYear, null, dateComparisonButtons);
		btnPeriodLastYear.addClickListener(e -> {
			activeComparisonButton = btnPeriodLastYear;
			updateComparisonDates();
			btnComparisonPeriod.setCaption(btnPeriodLastYear.getCaption());
		});

		layout.addComponents(btnPeriodBefore, btnPeriodLastYear);

		return layout;
	}

	private Button createAndAddDateFilterButton(String id, String caption, Set<Button> buttonSet) {
		Button button = ButtonHelper.createButton(id, caption, e -> {
			changeCustomDateFilterPanelStyle(e.getButton(), buttonSet);
		}, ValoTheme.BUTTON_BORDERLESS, CssStyles.BUTTON_FILTER, CssStyles.BUTTON_FILTER_LIGHT);

		buttonSet.add(button);

		return button;
	}

	private void changeCustomDateFilterPanelStyle(Button activeFilterButton, Set<Button> buttonSet) {
		if (activeFilterButton != null) {
			CssStyles.style(activeFilterButton, CssStyles.BUTTON_FILTER_DARK);
			CssStyles.removeStyles(activeFilterButton, CssStyles.BUTTON_FILTER_LIGHT);
		}

		buttonSet.forEach(b -> {
			if (activeFilterButton == null || b != activeFilterButton) {
				CssStyles.style(b, CssStyles.BUTTON_FILTER_LIGHT);
				CssStyles.removeStyles(b, CssStyles.BUTTON_FILTER_DARK);
			}
		});

		if (activeFilterButton == btnShowCustomPeriod)
			customDateFilterLayout.setVisible(true);
		else
			customDateFilterLayout.setVisible(false);
	}

	private void updateComparisonButtons(DateFilterType dateFilterType, Date from, Date to, boolean skipChangeButtonCaptions) {
		Language userLanguage = I18nProperties.getUserLanguage();

		if (!skipChangeButtonCaptions) {
			switch (dateFilterType) {
			case TODAY:
			case YESTERDAY:
				btnPeriodBefore.setCaption(
					String.format(
						I18nProperties.getCaption(Captions.dashboardDayBefore),
						DateFormatHelper.formatDate(DateHelper.subtractDays(from, 1))));
				btnPeriodLastYear.setCaption(
					String.format(
						I18nProperties.getCaption(Captions.dashboardSameDayLastYear),
						DateFormatHelper.formatDate(DateHelper.subtractYears(from, 1))));
				break;
			case THIS_WEEK:
				btnPeriodBefore.setCaption(
					String.format(
						I18nProperties.getCaption(Captions.dashboardWeekBefore),
						DateHelper.getPreviousEpiWeek(from).toString(DateHelper.subtractWeeks(to, 1), userLanguage)));
				int daysBetweenEpiWeekStartAndNow =
					DateHelper.getFullDaysBetween(DateHelper.getEpiWeekStart(DateHelper.getEpiWeek(from)), new Date());
				btnPeriodLastYear.setCaption(
					String.format(
						I18nProperties.getCaption(Captions.dashboardSameWeekLastYear),
						DateHelper.getEpiWeekYearBefore(DateHelper.getEpiWeek(from)).toString(daysBetweenEpiWeekStartAndNow, userLanguage)));
				break;
			case LAST_WEEK:
				btnPeriodBefore.setCaption(
					String
						.format(I18nProperties.getCaption(Captions.dashboardWeekBefore), DateHelper.getPreviousEpiWeek(from).toString(userLanguage)));
				btnPeriodLastYear.setCaption(
					String.format(
						I18nProperties.getCaption(Captions.dashboardSameWeekLastYear),
						DateHelper.getEpiWeekYearBefore(DateHelper.getEpiWeek(from)).toString(userLanguage)));
				break;
			case THIS_YEAR:
				btnPeriodLastYear.setCaption(
					String.format(
						I18nProperties.getCaption(Captions.dashboardSamePeriodLastYear),
						DateFormatHelper.buildPeriodString(DateHelper.subtractYears(from, 1), DateHelper.subtractYears(to, 1))));
				break;
			case CUSTOM:
				throw new UnsupportedOperationException(
					"Captions for custom filter should be changed elsewhere to account for the differences between epi weeks and dates.");
			}
		}

		if (dateFilterType == DateFilterType.THIS_YEAR) {
			btnPeriodBefore.setVisible(false);
			activeComparisonButton = btnPeriodLastYear;
			changeCustomDateFilterPanelStyle(btnPeriodLastYear, dateComparisonButtons);
		} else {
			btnPeriodBefore.setVisible(true);
		}

		btnComparisonPeriod.setCaption(activeComparisonButton.getCaption());
	}

	private void setDateFilter(Date from, Date to) {
		dashboardDataProvider.setFromDate(DateHelper.getStartOfDay(from));
		dashboardDataProvider.setToDate(DateHelper.getEndOfDay(to));
		updateComparisonDates();
		if (dateFilterChangeCallback != null) {
			dateFilterChangeCallback.run();
		}
	}

	private void updateComparisonDates() {
		if (currentDateFilterType != DateFilterType.THIS_YEAR && activeComparisonButton == btnPeriodBefore) {
			int activePeriodLength = currentDateFilterType == DateFilterType.THIS_WEEK
				? 7
				: DateHelper.getDaysBetween(dashboardDataProvider.getFromDate(), dashboardDataProvider.getToDate());
			dashboardDataProvider.setPreviousFromDate(DateHelper.subtractDays(dashboardDataProvider.getFromDate(), activePeriodLength));
			dashboardDataProvider.setPreviousToDate(DateHelper.subtractDays(dashboardDataProvider.getToDate(), activePeriodLength));
		} else if (currentDateFilterType == DateFilterType.THIS_WEEK || currentDateFilterType == DateFilterType.LAST_WEEK) {
			Date previousEpiWeekStart =
				DateHelper.getEpiWeekStart(DateHelper.getEpiWeekYearBefore(DateHelper.getEpiWeek(dashboardDataProvider.getFromDate())));
			dashboardDataProvider.setPreviousFromDate(previousEpiWeekStart);
			int activePeriodLength = DateHelper.getDaysBetween(dashboardDataProvider.getFromDate(), dashboardDataProvider.getToDate());
			dashboardDataProvider.setPreviousToDate(DateHelper.addDays(previousEpiWeekStart, activePeriodLength - 1));
		} else {
			dashboardDataProvider.setPreviousFromDate(DateHelper.subtractYears(dashboardDataProvider.getFromDate(), 1));
			dashboardDataProvider.setPreviousToDate(DateHelper.subtractYears(dashboardDataProvider.getToDate(), 1));
		}
	}

	private enum DateFilterType {
		TODAY,
		YESTERDAY,
		THIS_WEEK,
		LAST_WEEK,
		THIS_YEAR,
		CUSTOM;
	}

	public Runnable getDateFilterChangeCallback() {
		return dateFilterChangeCallback;
	}

	public void setDateFilterChangeCallback(Runnable dateFilterChangeCallback) {
		this.dateFilterChangeCallback = dateFilterChangeCallback;
	}

	protected void addCustomComponent(Component component, String locator) {
		customLayout.addComponent(component, locator);
		component.addStyleName(FILTER_ITEM_STYLE);
	}
}
