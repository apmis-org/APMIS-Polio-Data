package de.symeda.sormas.ui.report;

import com.vaadin.flow.component.UI;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.TextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.components.grid.HeaderCell;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.HeaderCell;
import com.vaadin.ui.components.grid.HeaderRow;

import java.net.URI;
import java.util.List;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.Descriptions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.InfrastructureType;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.criteria.BaseCriteria;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.SormasUI;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.ViewModelProviders;
import de.symeda.sormas.ui.campaign.components.CampaignFormPhaseSelector;
import de.symeda.sormas.ui.configuration.infrastructure.CommunitiesGrid;
import de.symeda.sormas.ui.configuration.infrastructure.CommunitiesView;
import de.symeda.sormas.ui.configuration.infrastructure.components.SearchField;
import de.symeda.sormas.ui.user.UserGrid;
import de.symeda.sormas.ui.utils.AbstractView;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.ComboBoxHelper;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.ExportEntityName;
import de.symeda.sormas.ui.utils.FieldHelper;
import de.symeda.sormas.ui.utils.GridExportStreamResource;
import de.symeda.sormas.ui.utils.MenuBarHelper;
import de.symeda.sormas.ui.utils.RowCount;
import de.symeda.sormas.ui.utils.V7GridExportStreamResource;

public class CompletionAnalysisTabSheets extends VerticalLayout implements View {

	private static final long serialVersionUID = -3533557348144005469L;

	public static final String ACTIVE_FILTER = I18nProperties.getString(Strings.active);
	public static final String INACTIVE_FILTER = I18nProperties.getString(Strings.inactive);

	private CompletionAnalysisGrid grid;
	private CampaignFormDataCriteria criteria;
	private Button syncButton;
	// check the diagram definition Selector
	// Filter
	private SearchField searchField;
	private ComboBox areaFilter;
	private ComboBox campaignFilter;
//		private ComboBox campaignPhaseFilter;
	private CampaignFormPhaseSelector campaignFormPhaseSelector;

	private ComboBox regionFilter;
	private ComboBox districtFilter;
	private ComboBox relevanceStatusFilter;
	private Button resetButton;
	protected boolean applyingCriteria;

	private HorizontalLayout filterLayout;
	// private VerticalLayout gridLayout;

	private RowCount rowsCount;

	

	public CompletionAnalysisTabSheets(FormAccess formAccess) {
		// System.out.println("Qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq
		// "+formAccess.toString());
	//	criteria = criteria;
		// gridLayout = new VerticalLayout();
		criteria = new CampaignFormDataCriteria();
		grid = new CompletionAnalysisGrid(criteria, formAccess);
		HeaderRow mainHeader = grid.getDefaultHeaderRow();
		
		HeaderCell regionNameHeader = mainHeader.getCell("area");
		regionNameHeader.setDescription("Region");
		HeaderCell provinceNameHeader = mainHeader.getCell("region");
		provinceNameHeader.setDescription("Province");
		HeaderCell districtNameHeader = mainHeader.getCell("district");
		districtNameHeader.setDescription("District");
		HeaderCell ccodeNameHeader = mainHeader.getCell("ccode");
		ccodeNameHeader.setDescription("CCode");
		HeaderCell clusterNumberHeader = mainHeader.getCell("clusternumber");
		clusterNumberHeader.setDescription("Cluster Number");
		HeaderCell houseHoldHeader = mainHeader.getCell("analysis_a");
		houseHoldHeader.setDescription("ICM Household Monitoring");
		HeaderCell icmRevisitsHeader = mainHeader.getCell("analysis_b");
		icmRevisitsHeader.setDescription("ICM Revisits");
		HeaderCell supervisorMonitpringHeader = mainHeader.getCell("analysis_c");
		supervisorMonitpringHeader.setDescription("ICM Supervisor Monitoring");
		HeaderCell teamMonitpringHeader = mainHeader.getCell("analysis_d");
		teamMonitpringHeader.setDescription("ICM Team Monitoring");
		
		this.addComponent(createFilterBar());
		extractUrl();
		this.addComponent(grid);
		this.setHeightFull();
		this.setMargin(false);
		this.setSpacing(false);
		this.setSizeFull();
		this.setExpandRatio(grid, 1);
		this.setStyleName("crud-main-layout");
		
		
		
		

		Button exportButton = ButtonHelper.createIconButton(Captions.export, VaadinIcons.TABLE, null,
				ValoTheme.BUTTON_PRIMARY);
		exportButton.setDescription(I18nProperties.getDescription(Descriptions.descExportButton));
		this.addComponent(exportButton);

		StreamResource streamResource = GridExportStreamResource.createStreamResource("", "", grid,
				ExportEntityName.USERS, CompletionAnalysisGrid.EDIT_BTN_ID);
		FileDownloader fileDownloaderx = new FileDownloader(streamResource);
		fileDownloaderx.extend(exportButton);

	}

	private HorizontalLayout createFilterBar() {

		final UserDto user = UserProvider.getCurrent().getUser();

		criteria.area(user.getArea());// .setArea(user.getArea());
		criteria.region(user.getRegion());// .setRegion(user.getRegion());
		criteria.district(user.getDistrict()); // .setDistrict(user.getDistrict());
		
		
		filterLayout = new HorizontalLayout();
		filterLayout.setMargin(false);
		filterLayout.setSpacing(true);
		
		campaignFilter = ComboBoxHelper.createComboBoxV7();
		campaignFilter.setId(CampaignDto.NAME);
		campaignFilter.setRequired(true);
		campaignFilter.setNullSelectionAllowed(false);
		campaignFilter.setCaption(I18nProperties.getCaption(Captions.Campaign));
		campaignFilter.setWidth(200, Unit.PIXELS);
		campaignFilter.setInputPrompt(I18nProperties.getString(Strings.promptCampaign));
		campaignFilter.addItems(FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference());
//		campaignFilter.addValueChangeListener(e -> {
//		});
		 campaignFilter.addValueChangeListener(e -> {
			CampaignReferenceDto campaign = (CampaignReferenceDto) e.getProperty().getValue();
			criteria.campaign(campaign);
			grid.reload();
		});

		filterLayout.addComponent(campaignFilter);

		final CampaignReferenceDto lastStartedCampaign = getLastStartedCampaign();
		if (lastStartedCampaign != null) {
			campaignFilter.setValue(lastStartedCampaign);

		}

		areaFilter = ComboBoxHelper.createComboBoxV7();
		areaFilter.setId(RegionDto.AREA);
		areaFilter.setWidth(140, Unit.PIXELS);
		if (user.getArea() == null) {
		
		areaFilter.setCaption(I18nProperties.getPrefixCaption(RegionDto.I18N_PREFIX, RegionDto.AREA));
		areaFilter.addItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());

		System.out.println(" +++++++ = " + criteria.getArea() == null);
		if (criteria.getArea() == null) {
			criteria.fromUrlParams("area=W5R34K-APYPCA-4GZXDO-IVJWKGIM");
		}
		areaFilter.addValueChangeListener(e -> {
			System.out.println(e.getProperty().getValue() + "khgfksuiihyikgivivciouvsiuvivkihvi");
			AreaReferenceDto area = (AreaReferenceDto) e.getProperty().getValue();
			criteria.area(area);
//			navigateTo(criteria);
			FieldHelper.updateItems(regionFilter,
					area != null ? FacadeProvider.getRegionFacade().getAllActiveByArea(area.getUuid()) : null);

			grid.reload();
		});
		
		filterLayout.addComponent(areaFilter);
		}
		
		
		if (user.getRegion() == null) {
		regionFilter = ComboBoxHelper.createComboBoxV7();
		regionFilter.setId(DistrictDto.REGION);
		regionFilter.setWidth(140, Unit.PIXELS);
		regionFilter.setCaption(I18nProperties.getPrefixCaption(DistrictDto.I18N_PREFIX, DistrictDto.REGION));
		
		if(user.getArea() != null) {
			regionFilter.addItems(FacadeProvider.getRegionFacade().getAllActiveByArea(user.getArea().getUuid()));
		}else {
			regionFilter.addItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
		}
		
		regionFilter.addValueChangeListener(e -> {
			RegionReferenceDto region = (RegionReferenceDto) e.getProperty().getValue();
			criteria.region(region);
//			navigateTo(criteria);
			FieldHelper.updateItems(districtFilter,
					region != null ? FacadeProvider.getDistrictFacade().getAllActiveByRegion(region.getUuid()) : null);
			grid.reload();
		});
		filterLayout.addComponent(regionFilter);
		}
		
		
		if(user.getDistrict() == null) {
		districtFilter = ComboBoxHelper.createComboBoxV7();
		districtFilter.setId(CommunityDto.DISTRICT);
		districtFilter.setWidth(140, Unit.PIXELS);
		districtFilter.setCaption(I18nProperties.getPrefixCaption(CommunityDto.I18N_PREFIX, CommunityDto.DISTRICT));
		
		if(user.getRegion() != null) {
			districtFilter.addItems(FacadeProvider.getDistrictFacade().getAllActiveByRegion(user.getRegion().getUuid()));
		}
		
		
		districtFilter.addValueChangeListener(e -> {
			criteria.district((DistrictReferenceDto) e.getProperty().getValue());
			grid.reload();
		});
		filterLayout.addComponent(districtFilter);

		resetButton = ButtonHelper.createButton(Captions.actionResetFilters, event -> {
			ViewModelProviders.of(CommunitiesView.class).remove(CommunityCriteriaNew.class);
			navigateTo(null);
			// grid.reload();
		}, CssStyles.FORCE_CAPTION);
		resetButton.setVisible(true);

		filterLayout.addComponent(resetButton);
		}
		
		return filterLayout;
	}

	public static StreamResource createGridExportStreamResourcsse(List<String> lst, String fln) {

		return new V7GridExportStreamResource(lst, fln);
	}

	// @Override
	public void extractUrl() {
		URI location = Page.getCurrent().getLocation();
		String uri = location.toString();

		String params = uri.trim();
		if (params.startsWith("?")) {
			params = params.substring(1);
			criteria.fromUrlParams(params);
		}
		updateFilterComponents();
		grid.reload();
	}

	public void updateFilterComponents() {

		// TODO replace with Vaadin 8 databinding
		applyingCriteria = true;

		resetButton.setVisible(criteria.hasAnyFilterActive());

//		if (relevanceStatusFilter != null) {
//			relevanceStatusFilter.setValue(criteria.getRelevanceStatus());
//		}
//		searchField.setValue(criteria.getNameLike());
		areaFilter.setValue(criteria.getArea());
		regionFilter.setValue(criteria.getRegion());
		districtFilter.setValue(criteria.getDistrict());
		campaignFilter.setValue(criteria.getCampaign());

		applyingCriteria = false;
	}

	public boolean navigateTo(BaseCriteria criteria) {
		return navigateTo(criteria, true);
	}

	public boolean navigateTo(BaseCriteria criteria, boolean force) {
		if (applyingCriteria) {
			return false;
		}
		applyingCriteria = true;

		Navigator navigator = SormasUI.get().getNavigator();

		String state = navigator.getState();
		String newState = buildNavigationState(state, criteria);

		boolean didNavigate = false;
		if (!newState.equals(state) || force) {
			navigator.navigateTo(newState);

			didNavigate = true;
		}

		applyingCriteria = false;

		return didNavigate;
	}

	public static String buildNavigationState(String currentState, BaseCriteria criteria) {

		String newState = currentState;
		int paramsIndex = newState.lastIndexOf('?');
		if (paramsIndex >= 0) {
			newState = newState.substring(0, paramsIndex);
		}

		if (criteria != null) {
			String params = criteria.toUrlParams();
			if (!DataHelper.isNullOrEmpty(params)) {
				if (newState.charAt(newState.length() - 1) != '/') {
					newState += "/";
				}

				newState += "?" + params;
			}
		}

		return newState;
	}

	public CampaignReferenceDto getLastStartedCampaign() {
		return FacadeProvider.getCampaignFacade().getLastStartedCampaign();
	}

//	private void createCampaignPhaseFilter() {
//		campaignFormPhaseSelector = new CampaignFormPhaseSelector(null);
//		campaignFormPhaseSelector.addValueChangeListener(e -> {
//			campaignFormPhaseSelector.getValue().toLowerCase();
//			//dashboardView.refreshDashboard();
//		});
//		addComponent(campaignFormPhaseSelector);
//		//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.  "+campaignFormPhaseSelector.getValue().toLowerCase());
//		campaignFormPhaseSelector.getValue().toLowerCase();
//	}

}
