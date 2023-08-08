package com.cinoteck.application.views.campaigndata;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.campaign.CampaignDataImportDialog;
import com.cinoteck.application.views.campaign.ImportPopulationDataDialog;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.server.Page;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;

import de.symeda.sormas.api.ErrorStatusEnum;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.Descriptions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.SortProperty;

@PageTitle("APMIS-Campaign Data")
@Route(value = "campaigndata", layout = MainLayout.class)
public class CampaignDataView extends VerticalLayout {

	/**
	 * 
	 */

	private static final long serialVersionUID = 7588118851062483372L;

	private Grid<CampaignFormDataIndexDto> grid = new Grid<>(CampaignFormDataIndexDto.class, false);
//	private GridListDataView<CampaignFormDataIndexDto> dataView;
	private CampaignFormDataCriteria criteria;
	private CampaignFormMetaDto formMetaReference;
	private CampaignFormDataDto campaignFormDatadto;

	ComboBox<String> campaignYear = new ComboBox<>();
	ComboBox<CampaignReferenceDto> campaignz = new ComboBox<>();
	ComboBox<CampaignPhase> campaignPhase = new ComboBox<>();
	ComboBox<CampaignFormMetaReferenceDto> campaignFormCombo = new ComboBox<>();
	ComboBox<AreaReferenceDto> regionCombo = new ComboBox<>();
	ComboBox<RegionReferenceDto> provinceCombo = new ComboBox<>();
	ComboBox<DistrictReferenceDto> districtCombo = new ComboBox<>();
	ComboBox<CommunityReferenceDto> clusterCombo = new ComboBox<>();
	ComboBox<CampaignFormElementImportance> importanceSwitcher = new ComboBox<>();
	Button resetHandler = new Button();
//	Button applyHandler = new Button();
	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;
	List<CampaignFormMetaReferenceDto> campaignForms;
	Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
	Icon icon = VaadinIcon.UPLOAD_ALT.create();
	Paragraph countRowItems;
	UserProvider userProvider = new UserProvider();

	CampaignFormDataEditForm campaignFormDataEditForm;

	Button enterBulkEdit = new Button("Enter Bulk Edit Mode");
	Button leaveBulkEdit = new Button("Leave Bulk Edit");
	MenuBar dropdownBulkOperations = new MenuBar();
	ConfirmDialog confirmationDialog;

	private DataProvider<CampaignFormDataIndexDto, CampaignFormDataCriteria> dataProvider;

	public CampaignDataView() {

		if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
		setSizeFull();
		setSpacing(false);
		criteria = new CampaignFormDataCriteria();
		createCampaignDataFilter();

		configureGrid(criteria);
	}

	private String getLabelForEnum(CampaignPhase campaignPhase) {
		switch (campaignPhase) {
		case PRE:
			return "Pre-Campaign";

		case POST:
			return "Post-Campaign";

		case INTRA:
			return "Intra-Campaign";

		default:
			return campaignPhase.toString();
		}
	}

	private void createCampaignDataFilter() {
		setMargin(true);

		

		ComboBox<CampaignFormMetaReferenceDto> newForm = new ComboBox<>();
		newForm.setLabel(I18nProperties.getCaption(Captions.actionNewForm));
		newForm.setPlaceholder(I18nProperties.getCaption(Captions.dataEntry));
		newForm.setTooltipText(I18nProperties.getDescription(Descriptions.campaign_dataEntry));
		newForm.setClearButtonVisible(true);
		newForm.getStyle().set("padding-top", "0px !important");

		ComboBox<CampaignFormMetaReferenceDto> importFormData = new ComboBox<>();
		importFormData.setLabel(I18nProperties.getCaption(Captions.actionImport));
		importFormData.setPlaceholder(I18nProperties.getCaption(Captions.dataImport));
		importFormData.setTooltipText(I18nProperties.getDescription(Descriptions.campaign_dataImport));
		importFormData.setClearButtonVisible(true);
		importFormData.getStyle().set("padding-top", "0px !important");
		// ((HasPrefixAndSuffix)
		// newForm).setPrefixComponent(VaadinIcon.SEARCH.create());

//		Button importData = new Button("IMPORT", new Icon(VaadinIcon.PLUS_CIRCLE));

		VerticalLayout filterBlock = new VerticalLayout();
		filterBlock.setSpacing(true);
		filterBlock.setMargin(true);
		filterBlock.setClassName("campaignDataFilterParent");

		HorizontalLayout layout = new HorizontalLayout();
		layout.setAlignItems(Alignment.END);

		Button displayFilters = new Button(I18nProperties.getCaption(Captions.showFilters),
				new Icon(VaadinIcon.SLIDERS));

		HorizontalLayout actionButtonlayout = new HorizontalLayout();
		actionButtonlayout.setClassName("row pl-3");
		actionButtonlayout.setVisible(false);
		actionButtonlayout.setAlignItems(Alignment.END);
		actionButtonlayout.add(campaignYear, campaignz, campaignPhase, newForm, importFormData, anchor);

		HorizontalLayout level1Filters = new HorizontalLayout();
		level1Filters.setPadding(false);
		level1Filters.setVisible(false);
		level1Filters.setWidth("98%");
		level1Filters.setAlignItems(Alignment.END);
		level1Filters.setClassName("row pl-3");

		HorizontalLayout rightFloat = new HorizontalLayout();
		rightFloat.setWidth("100%");
		rightFloat.setJustifyContentMode(JustifyContentMode.END);
		level1Filters.add(campaignFormCombo, regionCombo, provinceCombo, districtCombo, clusterCombo,
				importanceSwitcher, resetHandler, rightFloat);

		displayFilters.addClickListener(e -> {
			if (!level1Filters.isVisible()) {
				actionButtonlayout.setVisible(true);
				level1Filters.setVisible(true);
				displayFilters.setText(I18nProperties.getCaption(Captions.hideFilters));
			} else {
				actionButtonlayout.setVisible(false);
				level1Filters.setVisible(false);
				displayFilters.setText(I18nProperties.getCaption(Captions.showFilters));
			}
		});

		TextArea resultField = new TextArea();
		resultField.setWidth("100%");

		campaignYear.setLabel(I18nProperties.getCaption(Captions.campaignYear));
		campaignYear.getStyle().set("padding-top", "0px !important");
		campaignYear.setClassName("col-sm-6, col-xs-6");

		campaignz.setLabel(I18nProperties.getCaption(Captions.Campaigns));
		campaignz.getStyle().set("padding-top", "0px !important");
		campaignz.setClassName("col-sm-6, col-xs-6");

		campaignPhase.setLabel(I18nProperties.getCaption(Captions.Campaign_phase));
		campaignPhase.getStyle().set("padding-top", "0px !important");
		campaignz.setClassName("col-sm-6, col-xs-6");

		campaignFormCombo.setLabel(I18nProperties.getCaption(Captions.campaignCampaignForm));
		campaignFormCombo.getStyle().set("padding-top", "0px !important");
		campaignFormCombo.getStyle().set("--vaadin-combo-box-overlay-width", "350px");
		campaignFormCombo.setClassName("col-sm-6, col-xs-6");

		regionCombo.setLabel(I18nProperties.getCaption(Captions.area));
		regionCombo.getStyle().set("padding-top", "0px !important");
		regionCombo.setClassName("col-sm-6, col-xs-6");
		regionCombo.setPlaceholder(I18nProperties.getCaption(Captions.area));

		regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
		regionCombo.setItems(regions);
		if (userProvider.getUser().getArea() != null) {
			regionCombo.setValue(userProvider.getUser().getArea());
			regionCombo.setEnabled(false);
		}

		provinceCombo.setLabel(I18nProperties.getCaption(Captions.region));
		provinceCombo.getStyle().set("padding-top", "0px !important");
		provinceCombo.setClassName("col-sm-6, col-xs-6");

		provinceCombo.setPlaceholder(I18nProperties.getCaption(Captions.region));
		provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
		provinceCombo.setItems(provinces);
		if (userProvider.getUser().getRegion() != null) {
			provinceCombo.setValue(userProvider.getUser().getRegion());
		}
		provinceCombo.setEnabled(false);

		provinceCombo.getStyle().set("padding-top", "0px");
		provinceCombo.setClassName("col-sm-6, col-xs-6");

		districtCombo.setLabel(I18nProperties.getCaption(Captions.district));
		districtCombo.getStyle().set("padding-top", "0px !important");
		districtCombo.setClassName("col-sm-6, col-xs-6");

		districtCombo.setPlaceholder(I18nProperties.getCaption(Captions.district));
		districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();
		districtCombo.setItems(districts);
		if (userProvider.getUser().getDistrict() != null) {
			districtCombo.setValue(userProvider.getUser().getDistrict());
		}
		districtCombo.setEnabled(false);
		districtCombo.getStyle().set("padding-top", "0px");
		districtCombo.setClassName("col-sm-6, col-xs-6");

		clusterCombo.setLabel(I18nProperties.getCaption(Captions.community));
		clusterCombo.getStyle().set("padding-top", "0px !important");

//		if(userProvider.getUser().getCommunity() != null) {
//			clusterCombo.setValue(userProvider.getUser().getCommunity());
//		}
		clusterCombo.setPlaceholder(I18nProperties.getCaption(Captions.community));
		clusterCombo.setClassName("col-sm-6, col-xs-6");

		clusterCombo.setEnabled(false);

		

		// Initialize Item lists
		List<CampaignReferenceDto> campaigns = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference();
		CampaignReferenceDto lastStarted = FacadeProvider.getCampaignFacade().getLastStartedCampaign();
		List<String> camYearList = campaigns.stream().map(CampaignReferenceDto::getCampaignYear).distinct()
				.collect(Collectors.toList());

		campaignYear.setItems(camYearList);
		campaignYear.setValue(lastStarted.getCampaignYear());

		List<CampaignReferenceDto> allCampaigns = campaigns.stream()
				.filter(c -> c.getCampaignYear().equals(campaignYear.getValue())).collect(Collectors.toList());

		campaignz.setItems(allCampaigns);
		campaignz.setValue(lastStarted);

		campaignPhase.setItemLabelGenerator(this::getLabelForEnum);
		if (userProvider.getUser().getUsertype() == UserType.EOC_USER) {
			campaignPhase.setItems(CampaignPhase.INTRA, CampaignPhase.POST);
			campaignPhase.setValue(CampaignPhase.INTRA);
		} else {
			campaignPhase.setItems(CampaignPhase.values());
			campaignPhase.setValue(CampaignPhase.PRE);
		}

		campaignForms = FacadeProvider.getCampaignFormMetaFacade()
				.getAllCampaignFormMetasAsReferencesByRoundandCampaign(
						campaignPhase.getValue().toString().toLowerCase(), campaignz.getValue().getUuid());

		campaignFormCombo.setItems(campaignForms);
		campaignFormCombo.getStyle().set("--vaadin-combo-box-overlay-width", "350px");
		campaignFormCombo.setValue(campaignForms.get(0));

		newForm.setItems(campaignForms);

		importFormData.setItems(campaignForms);

		criteria.campaign(lastStarted);
		criteria.setFormType(campaignPhase.getValue().toString());
		criteria.setCampaignFormMeta(campaignFormCombo.getValue());
		
		int numberOfRows = (int) FacadeProvider.getCampaignFormDataFacade().count(criteria);
		countRowItems = new Paragraph(I18nProperties.getCaption(Captions.rows) + numberOfRows);
		countRowItems.setId("rowCount");
		rightFloat.add(countRowItems);


		// Configure Comboboxes Value Change Listeners
		campaignYear.addValueChangeListener(e -> {
			campaignz.clear();
			List<CampaignReferenceDto> allCampaigns_ = campaigns.stream()
					.filter(c -> c.getCampaignYear().equals(campaignYear.getValue())).collect(Collectors.toList());
			campaignz.setItems(allCampaigns_);
			campaignz.setValue(allCampaigns_.get(0));
			importanceSwitcher.setReadOnly(false);

		});

		campaignz.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				campaignFormCombo.clear();
				newForm.clear();
				importFormData.clear();
				List<CampaignFormMetaReferenceDto> campaignFormReferences_ = FacadeProvider.getCampaignFormMetaFacade()
						.getAllCampaignFormMetasAsReferencesByRoundandCampaign(
								campaignPhase.getValue().toString().toLowerCase(), e.getValue().getUuid());
				campaignFormCombo.setItems(campaignFormReferences_);
				campaignFormCombo.setValue(campaignFormReferences_.get(0));
				importanceSwitcher.setReadOnly(false);

				newForm.setItems(campaignFormReferences_);
				importFormData.setItems(campaignFormReferences_);
				reload();
				updateRowCount();
			}
		});

		campaignPhase.addValueChangeListener(e -> {
			importanceSwitcher.setReadOnly(false);

			campaignFormCombo.clear();
			newForm.clear();
			importFormData.clear();
			List<CampaignFormMetaReferenceDto> campaignFormReferences_ = FacadeProvider.getCampaignFormMetaFacade()
					.getAllCampaignFormMetasAsReferencesByRoundandCampaign(e.getValue().toString().toLowerCase(),
							campaignz.getValue().getUuid());
			campaignFormCombo.setItems(campaignFormReferences_);
			campaignFormCombo.setValue(campaignFormReferences_.get(0));
			newForm.setItems(campaignFormReferences_);
			importFormData.setItems(campaignFormReferences_);
			reload();
			updateRowCount();
		});

		campaignFormCombo.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				formMetaReference = FacadeProvider.getCampaignFormMetaFacade()
						.getCampaignFormMetaByUuid(e.getValue().getUuid());
				importanceSwitcher.setReadOnly(false);
				reload();
				// add method to update the grid after this combo changes
				
			} else {

				importanceSwitcher.clear();
				importanceSwitcher.setReadOnly(true);

			}
			updateRowCount();

		});

		regionCombo.addValueChangeListener(e -> {
			provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
			provinceCombo.setItems(provinces);
			provinceCombo.setEnabled(true);
			reload();
			updateRowCount();
		});

		provinceCombo.addValueChangeListener(e -> {
			districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
			districtCombo.setItems(districts);
			districtCombo.setEnabled(true);
			reload();
			updateRowCount();
		});

		districtCombo.addValueChangeListener(e -> {
			communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());
			clusterCombo.setItems(communities);
			clusterCombo.setEnabled(true);
			reload();
			updateRowCount();
		});

		clusterCombo.addValueChangeListener(e -> {
			reload();
			updateRowCount();
		});

		newForm.addValueChangeListener(e -> {
			if (e.getValue() != null && campaignz != null) {

				CampaignFormDataEditForm cam = new CampaignFormDataEditForm(e.getValue(), campaignz.getValue(), false,
						null, grid);
				// add(cam);

				newForm.setValue(null);
			}
		});

		importFormData.addValueChangeListener(e -> {
			if (importFormData.getValue() != null) {
				CampaignDataImportDialog dialog = new CampaignDataImportDialog();
				dialog.open();
			}
		});
		
		// TODO Importance filter switcher should be visible only on the change of form
				importanceSwitcher.setLabel(I18nProperties.getCaption(Captions.importance));
				importanceSwitcher.getStyle().set("padding-top", "0px !important");
				importanceSwitcher.setClassName("col-sm-6, col-xs-6");

				importanceSwitcher.setPlaceholder("Importance");
				importanceSwitcher.setItems(CampaignFormElementImportance.values());
				importanceSwitcher.setClearButtonVisible(true);
				importanceSwitcher.setTooltipText(I18nProperties.getDescription(Descriptions.campaign_importance));
				importanceSwitcher.addValueChangeListener(e -> {
					formMetaReference = FacadeProvider.getCampaignFormMetaFacade()
							.getCampaignFormMetaByUuid(campaignFormCombo.getValue().getUuid());

					reload();
					if (formMetaReference != null) {
						grid.removeAllColumns();
						configureGrid(criteria);

						final boolean allAndImportantFormElements = e.getValue() == CampaignFormElementImportance.ALL;
						final boolean onlyImportantFormElements = e.getValue() == CampaignFormElementImportance.IMPORTANT;

						final List<CampaignFormElement> campaignFormElements = formMetaReference.getCampaignFormElements();

						for (CampaignFormElement element : campaignFormElements) {

							if (element.isImportant() && onlyImportantFormElements) {
								String caption = null;
								if (caption == null) {
									caption = element.getCaption();
								}

								if (caption != null) {
									addCustomColumn(element.getId(), caption);
								}
							} else if (allAndImportantFormElements) {
								String caption = null;
								if (caption == null) {
									caption = element.getCaption();
								}
								if (caption != null) {
									addCustomColumn(element.getId(), caption);
								}
							}
						}
					}

				});

		if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			enterBulkEdit = new Button("Enter Bulk Edit Mode");
			leaveBulkEdit = new Button("Leave Bulk Edit Mode");
			dropdownBulkOperations = new MenuBar();
			MenuItem bulkActionsItem = dropdownBulkOperations.addItem("Bulk Actions");
			SubMenu subMenu = bulkActionsItem.getSubMenu();
			subMenu.addItem("Delete", e -> handleDeleteAction());

		}
		dropdownBulkOperations.getStyle().set("margin-top", "5px");

		enterBulkEdit.addClassName("bulkActionButton");
		Icon bulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		enterBulkEdit.setIcon(bulkModeButtonnIcon);
		actionButtonlayout.add(enterBulkEdit);

		enterBulkEdit.addClickListener(e -> {
			dropdownBulkOperations.setVisible(true);
			grid.setSelectionMode(Grid.SelectionMode.MULTI);
			
			enterBulkEdit.setVisible(false);
			leaveBulkEdit.setVisible(true);
//			dropdownBulkOperations.setVisible(true);
		});

		leaveBulkEdit.setText("Leave Bulk Edit Mode");
		leaveBulkEdit.addClassName("leaveBulkActionButton");
		leaveBulkEdit.setVisible(false);
		Icon leaveBulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		leaveBulkEdit.setIcon(leaveBulkModeButtonnIcon);
		actionButtonlayout.add(leaveBulkEdit);

		leaveBulkEdit.addClickListener(e -> {
			grid.setSelectionMode(Grid.SelectionMode.SINGLE);
			enterBulkEdit.setVisible(true);
			leaveBulkEdit.setVisible(false);
			dropdownBulkOperations.setVisible(false);
		});
		dropdownBulkOperations.setVisible(false);
		actionButtonlayout.add(dropdownBulkOperations);

		resetHandler.setText(I18nProperties.getCaption(Captions.resetFilters));
		resetHandler.addClickListener(e -> {
			UI.getCurrent().getPage().reload();
			criteria.campaign(lastStarted);
			criteria.setFormType(campaignPhase.getValue().toString());
			updateRowCount();
		});

//		applyHandler.setText("Apply Filters");

		layout.add(displayFilters, actionButtonlayout);

		filterBlock.add(layout, level1Filters);

		add(filterBlock);
	}

	private void handleDeleteAction() {

		deleteAllSelectedItems(grid.getSelectedItems());

	}

	public void deleteAllSelectedItems(Collection<CampaignFormDataIndexDto> selectedRows) {
		confirmationDialog = new ConfirmDialog();

		if (selectedRows.size() == 0) {
			confirmationDialog.setCancelable(false);
			confirmationDialog.setRejectable(false);
			confirmationDialog.addCancelListener(e -> confirmationDialog.close());
			confirmationDialog.setConfirmText("Ok");

			confirmationDialog.setText("You have not selected any data to be deleted. ");
			confirmationDialog.setHeader("Error Deleting Campaign Data");
			confirmationDialog.open();

		} else {
			confirmationDialog.setCancelable(true);
			confirmationDialog.setRejectable(true);
			confirmationDialog.setRejectText("No");
			confirmationDialog.setConfirmText("Yes");
			confirmationDialog.addCancelListener(e -> confirmationDialog.close());
			confirmationDialog.addRejectListener(e -> confirmationDialog.close());
			confirmationDialog.open();
			confirmationDialog.setHeader("Delete Campaign Data");
			confirmationDialog.setText("Are you sure you want to Delete the selected Campaign Data?");
			confirmationDialog.addConfirmListener(e -> {
				List<String> uuids = selectedRows.stream().map(CampaignFormDataIndexDto::getUuid)
						.collect(Collectors.toList());
				FacadeProvider.getCampaignFormDataFacade().deleteCampaignData(uuids);
//				 Notification.show("Camapaign Dayta Deleted ");
				reload();
				if (leaveBulkEdit.isVisible()) {
					leaveBulkEdit.setVisible(false);
					enterBulkEdit.setVisible(true);
					grid.setSelectionMode(Grid.SelectionMode.SINGLE);
					dropdownBulkOperations.setVisible(false);
				}
			});

		}
	}

	public void reload() {
		grid.getDataProvider().refreshAll();
		criteria.campaign(campaignz.getValue());
		criteria.setFormType(campaignPhase.getValue().toString());
		criteria.setCampaignFormMeta(campaignFormCombo.getValue());
		criteria.area(regionCombo.getValue());
		criteria.region(provinceCombo.getValue());
		criteria.district(districtCombo.getValue());
		criteria.community(clusterCombo.getValue());
	}

	@SuppressWarnings("deprecation")
	private void configureGrid(CampaignFormDataCriteria criteria) {
		setMargin(false);

		grid.setSelectionMode(SelectionMode.SINGLE);

		grid.setColumnReorderingAllowed(true);

//		GridColumn<Item> selectAllColumn = grid.addComponentColumn(item -> {
//		    CheckBox checkBox = new CheckBox();
//		    checkBox.addValueChangeListener(event -> {
//		        if (checkBox.getValue()) {
//		            grid.getSelectionModel().select(item);
//		        } else {
//		            grid.getSelectionModel().deselect(item);
//		        }
//		    });
//		    return checkBox;
//		}).setHeader("Select All");
		ComponentRenderer<Checkbox, CampaignFormDataIndexDto> activeRenderer = new ComponentRenderer<>(input -> {
//			boolean value = input.getForm();
			Checkbox checkbox = new Checkbox();

//			if (value == true)
//				checkbox.setValue(true);
			return checkbox;
		});

		grid.addColumn(CampaignFormDataIndexDto.CAMPAIGN).setHeader(I18nProperties.getCaption(Captions.Campaigns))
				.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> "Campaign");// .setFooter(String.format("Row
																												// Count:
																												// %s",
																												// (int)
		// FacadeProvider.getCampaignFormDataFacade().count(criteria)));
		grid.addColumn(CampaignFormDataIndexDto.FORM)
				.setHeader(I18nProperties.getCaption(Captions.campaignCampaignForm)).setSortable(true)
				.setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getForm());
		grid.addColumn(CampaignFormDataIndexDto.AREA).setHeader(I18nProperties.getCaption(Captions.area))
				.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> e.getArea());
		grid.addColumn(CampaignFormDataIndexDto.RCODE).setHeader(I18nProperties.getCaption(Captions.Area_externalId))
				.setSortable(true).setResizable(true).setAutoWidth(true).setTooltipGenerator(e -> "RCode");
		grid.addColumn(CampaignFormDataIndexDto.REGION).setHeader(I18nProperties.getCaption(Captions.region))
				.setSortable(true).setResizable(true).setAutoWidth(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.region));
		grid.addColumn(CampaignFormDataIndexDto.PCODE).setHeader(I18nProperties.getCaption(Captions.Region_externalID))
				.setSortable(true).setResizable(true).setAutoWidth(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Region_externalID));
		grid.addColumn(CampaignFormDataIndexDto.DISTRICT).setHeader(I18nProperties.getCaption(Captions.district))
				.setSortable(true).setResizable(true).setAutoWidth(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.district));
		grid.addColumn(CampaignFormDataIndexDto.DCODE)
				.setHeader(I18nProperties.getCaption(Captions.District_externalID)).setSortable(true).setResizable(true)
				.setAutoWidth(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.District_externalID));
		Column<CampaignFormDataIndexDto> comm = grid.addColumn(CampaignFormDataIndexDto.COMMUNITY)
				.setHeader(I18nProperties.getCaption(Captions.community)).setSortable(true).setResizable(true)
				.setAutoWidth(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.community));
		grid.addColumn(CampaignFormDataIndexDto.COMMUNITYNUMBER)
				.setHeader(I18nProperties.getCaption(Captions.clusterNumber)).setSortable(true).setResizable(true)
				.setAutoWidth(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.clusterNumber));
		grid.addColumn(CampaignFormDataIndexDto.CCODE)
				.setHeader(I18nProperties.getCaption(Captions.Community_externalID)).setSortable(true)
				.setResizable(true).setAutoWidth(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Community_externalID));
		grid.addColumn(CampaignFormDataIndexDto.FORM_DATE)
				.setHeader(I18nProperties.getCaption(Captions.CampaignFormData_formDate)).setSortable(true)
				.setResizable(true).setAutoWidth(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.CampaignFormData_formDate));
		grid.addColumn(CampaignFormDataIndexDto.FORM_TYPE).setHeader(I18nProperties.getCaption(Captions.formPhase))
				.setSortable(true).setResizable(true).setAutoWidth(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.formPhase));

		grid.setVisible(true);
		grid.setWidthFull();
//		grid.setHeightFull();
		grid.setAllRowsVisible(false);

		grid.asSingleSelect().addValueChangeListener(e -> {

			// editCampaignFormData(e.getValue());
			CampaignFormDataDto formData = FacadeProvider.getCampaignFormDataFacade()
					.getCampaignFormDataByUuid(e.getValue().getUuid());

			CampaignFormDataEditForm cam = new CampaignFormDataEditForm(formData.getCampaignFormMeta(),
					campaignz.getValue(), true, formData.getUuid(), grid);

		});

		dataProvider = DataProvider.fromFilteringCallbacks(this::fetchCampaignFormData, this::countCampaignFormData);
		grid.setDataProvider(dataProvider);

		GridExporter<CampaignFormDataIndexDto> exporter = GridExporter.createFor(grid);
//	    exporter.setExportValue(comm, item -> "" + item);
//	    exporter.setColumnPosition(lastNameCol, 1);
		exporter.setAutoAttachExportButtons(false);

		exporter.setTitle(I18nProperties.getCaption(Captions.campaignDataInformation));
		exporter.setFileName("GridExport" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));

		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		anchor.setId("campDatAnchor");

		anchor.getStyle().set("width", "100px");

		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());

		add(grid);

	}

	private void editCampaignFormData(CampaignFormDataIndexDto selected) {
		selected = grid.asSingleSelect().getValue();
		if (selected != null) {
			CampaignFormDataDto formData = FacadeProvider.getCampaignFormDataFacade()
					.getCampaignFormDataByUuid(selected.getUuid());
			openFormLayout(formData);
		}
	}

	private void openFormLayout(CampaignFormDataDto formData) {

		FormLayout formLayout = new FormLayout();

		ComboBox<Object> cbCampaign = new ComboBox<>(CampaignFormDataDto.CAMPAIGN);
		cbCampaign.setItems(formData.getCampaign());
		cbCampaign.setValue(formData.getCampaign());
		cbCampaign.setEnabled(false);

		Date date = new Date();

		// Convert Date to LocalDate
		LocalDate localDate = formData.getFormDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		DatePicker formDate = new DatePicker();
		formDate.setValue(localDate);

		ComboBox<Object> cbArea = new ComboBox<>(CampaignFormDataDto.AREA);
		cbArea.setItems(formData.getArea());
		cbArea.setValue(formData.getArea());
//	        cbArea.setItems(FacadeProvider.getAreaFacade().getAllActiveAndSelectedAsReference(campaignUUID));

		ComboBox<Object> cbRegion = new ComboBox<>(CampaignFormDataDto.REGION);
		cbRegion.setItems(formData.getRegion());
		cbRegion.setValue(formData.getRegion());

		ComboBox<Object> cbDistrict = new ComboBox<>(CampaignFormDataDto.DISTRICT);
		cbDistrict.setItems(formData.getDistrict());
		cbDistrict.setValue(formData.getDistrict());

		ComboBox<Object> cbCommunity = new ComboBox<>(CampaignFormDataDto.COMMUNITY);
		cbCommunity.setItems(formData.getCommunity());
		cbCommunity.setValue(formData.getCommunity());

		formLayout.add(cbCampaign, formDate, cbArea, cbRegion, cbDistrict, cbCommunity);

		Dialog dialog = new Dialog();
		dialog.add(formLayout);
//		dialog.setSizeFull();
		dialog.open();
	}

	private Stream<CampaignFormDataIndexDto> fetchCampaignFormData(
			Query<CampaignFormDataIndexDto, CampaignFormDataCriteria> query) {
		return FacadeProvider.getCampaignFormDataFacade()
				.getIndexList(criteria, query.getOffset(), query.getLimit(), query.getSortOrders().stream()
						.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
								sortOrder.getDirection() == SortDirection.ASCENDING))
						.collect(Collectors.toList()))
				.stream();
	}

	private int countCampaignFormData(Query<CampaignFormDataIndexDto, CampaignFormDataCriteria> query) {
		return (int) FacadeProvider.getCampaignFormDataFacade().count(criteria);
	}

	private void export(Grid<CampaignFormDataIndexDto> grid, TextArea result) {
		// Fetch all data from the grid in the current sorted order
		Stream<CampaignFormDataIndexDto> persons = null;
		Set<CampaignFormDataIndexDto> selection = grid.asMultiSelect().getValue();
		if (selection != null && selection.size() > 0) {
			persons = selection.stream();
		} else {
//			persons = dataView.getItems();
		}

		StringWriter output = new StringWriter();
		StatefulBeanToCsv<CampaignFormDataIndexDto> writer = new StatefulBeanToCsvBuilder<CampaignFormDataIndexDto>(
				output).build();
		try {
			writer.write(persons);
		} catch (Exception e) {
			output.write("An error occured during writing: " + e.getMessage());
		}

		result.setValue(output.toString());
	}

	private void setDataProvider() {
		DataProvider<CampaignFormDataIndexDto, CampaignFormDataCriteria> dataProvider = DataProvider
				.fromFilteringCallbacks(
						query -> FacadeProvider.getCampaignFormDataFacade()
								.getIndexList(criteria, query.getOffset(), query.getLimit(),
										query.getSortOrders().stream()
												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
														sortOrder.getDirection() == SortDirection.ASCENDING))
												.collect(Collectors.toList()))
								.stream(),
						query -> (int) FacadeProvider.getCampaignFormDataFacade().count(criteria));
		grid.setDataProvider(dataProvider);
	}

	public void addCustomColumn(String property, String caption) {
		if (!property.toString().contains("readonly")) {

			grid.addColumn(
					e -> e.getFormValues().stream().filter(v -> v.getId().equals(property)).findFirst().orElse(null))
					.setHeader(caption).setSortable(true).setResizable(true).setTooltipGenerator(e -> caption);

		}

	}

	private void updateRowCount() {
		int numberOfRows = (int) FacadeProvider.getCampaignFormDataFacade().count(criteria);
		String newText = "Rows : " + numberOfRows;

		countRowItems.setText(newText);
		countRowItems.setId("rowCount");
//	        Notification.show("Text updated: " + newText);
	}

	private void closeEditor() {
		campaignFormDataEditForm.setVisible(false);
		grid.setVisible(true);
		removeClassName("editing");
	}

}