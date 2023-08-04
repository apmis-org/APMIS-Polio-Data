package com.cinoteck.application.views.configurations;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.HasUuid;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionCriteria;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.SortProperty;

@PageTitle("Districts")
@Route(value = "districts", layout = ConfigurationsView.class)
public class DistrictView extends VerticalLayout {

	private static final long serialVersionUID = 1370022184569877189L;

	DistrictCriteria criteria;
	DistrictIndexDto districtIndexDto;
	DistrictDto dto;
	DistrictDataProvider districtDataProvider = new DistrictDataProvider();

	ConfigurableFilterDataProvider<DistrictIndexDto, Void, DistrictCriteria> filteredDataProvider;

	Grid<DistrictIndexDto> grid = new Grid<>(DistrictIndexDto.class, false);

	ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>("Region");

	ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>("Province");

	TextField searchField = new TextField();

	Button resetFilters = new Button("Reset Filters");
	Anchor anchor = new Anchor("", "Export");
	ComboBox<String> riskFilter = new ComboBox<>("Risk");
	ComboBox<EntityRelevanceStatus> relevanceStatusFilter = new ComboBox<>("Relevance Status");
	Paragraph countRowItems;
	UserProvider currentUser = new UserProvider();
	Button enterBulkEdit = new Button("Enter Bulk Edit Mode");
	Button leaveBulkEdit = new Button("Leave Bulk Edit");
	MenuBar dropdownBulkOperations = new MenuBar();
	SubMenu subMenu;
	ConfirmDialog archiveDearchiveConfirmation;
	String uuidsz = "";
	GridListDataView<DistrictIndexDto> dataView;
	ListDataProvider<DistrictIndexDto> dataProvider;
	int itemCount;

	@SuppressWarnings("deprecation")
	public DistrictView() {

		this.criteria = new DistrictCriteria();
		setSpacing(false);
		setHeightFull();
		setSizeFull();
		addFiltersLayout();
		districtGrid(criteria);

	}

	private void districtGrid(DistrictCriteria criteria) {
		this.criteria = criteria;
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);
		grid.addColumn(DistrictIndexDto::getAreaname).setHeader("Region").setSortable(true).setResizable(true)
				.setTooltipGenerator(e -> "Region");
		grid.addColumn(DistrictIndexDto::getAreaexternalId).setHeader("Rcode").setResizable(true).setSortable(true)
				.setTooltipGenerator(e -> "Rcode");
		grid.addColumn(DistrictIndexDto::getRegion).setHeader("Province").setSortable(true).setResizable(true)
				.setTooltipGenerator(e -> "Province");
		grid.addColumn(DistrictIndexDto::getRegionexternalId).setHeader("PCode").setResizable(true).setSortable(true)
				.setTooltipGenerator(e -> "PCode");
		grid.addColumn(DistrictIndexDto::getName).setHeader("District").setSortable(true).setResizable(true)
				.setTooltipGenerator(e -> "District");
		grid.addColumn(DistrictIndexDto::getExternalId).setHeader("DCode").setResizable(true).setSortable(true)
				.setTooltipGenerator(e -> "DCode");

		grid.setVisible(true);

		if (criteria == null) {
			criteria = new DistrictCriteria();
			criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
		}

		dataProvider = DataProvider
				.fromStream(FacadeProvider.getDistrictFacade().getIndexList(criteria, null, null, null).stream());

		dataView = grid.setItems(dataProvider);
//		grid.setDataProvider(filteredDataProvider);

		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				createOrEditDistrict(event.getValue());
			}
		});
		add(grid);

		GridExporter<DistrictIndexDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle("Users");
		exporter.setFileName(
				"APMIS_Regions" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		anchor.setId("exportArea");
		Icon icon = VaadinIcon.UPLOAD_ALT.create();
		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());
	}

	public Component addFiltersLayout() {
		if (criteria == null) {
			criteria = new DistrictCriteria();
		}
		if (currentUser.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			enterBulkEdit = new Button("Enter Bulk Edit Mode");
			leaveBulkEdit = new Button();
			dropdownBulkOperations = new MenuBar();
			MenuItem bulkActionsItem = dropdownBulkOperations.addItem("Bulk Actions");
			subMenu = bulkActionsItem.getSubMenu();
			subMenu.addItem("Archive", e -> handleArchiveDearchiveAction());

		}

		criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
		dataProvider = DataProvider
				.fromStream(FacadeProvider.getDistrictFacade().getIndexList(criteria, null, null, null).stream());

		itemCount = dataProvider.getItems().size();
		countRowItems = new Paragraph("Rows : " + itemCount);
		countRowItems.setId("rowCount");

		HorizontalLayout layout = new HorizontalLayout();
		layout.setPadding(false);
		layout.setVisible(false);
		layout.setAlignItems(Alignment.END);

		HorizontalLayout relevancelayout = new HorizontalLayout();
		relevancelayout.setPadding(false);
		relevancelayout.setVisible(false);
		relevancelayout.setAlignItems(Alignment.END);
		relevancelayout.setJustifyContentMode(JustifyContentMode.END);
		relevancelayout.setClassName("row");

		HorizontalLayout vlayout = new HorizontalLayout();
		vlayout.setPadding(false);

		vlayout.setAlignItems(Alignment.END);

		Button displayFilters = new Button("Show Filters", new Icon(VaadinIcon.SLIDERS));
		displayFilters.getStyle().set("margin-left", "1em");
		displayFilters.addClickListener(e -> {
			if (layout.isVisible() == false) {
				layout.setVisible(true);
				relevancelayout.setVisible(true);
				displayFilters.setText("Hide Filters");
			} else {
				layout.setVisible(false);
				relevancelayout.setVisible(false);

				displayFilters.setText("Show Filters");
			}
		});

		layout.setPadding(false);

		searchField.addClassName("filterBar");
		searchField.setPlaceholder("Search");
		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.getStyle().set("color", "#0D6938");
		searchField.setPrefixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.setWidth("15%");

		layout.add(searchField);

		regionFilter.setPlaceholder("All Regions");
		regionFilter.setClearButtonVisible(true);
		regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
		if (currentUser.getUser().getArea() != null) {
			regionFilter.setValue(currentUser.getUser().getArea());
			criteria.area(currentUser.getUser().getArea());
			provinceFilter.setItems(
					FacadeProvider.getRegionFacade().getAllActiveByArea(currentUser.getUser().getArea().getUuid()));
			regionFilter.setEnabled(false);
			refreshGridData();
		}

		layout.add(regionFilter);

		provinceFilter.setPlaceholder("All Provinces");
		provinceFilter.setClearButtonVisible(true);

		if (currentUser.getUser().getRegion() != null) {
			provinceFilter.setValue(currentUser.getUser().getRegion());
//			filteredDataProvider.setFilter(criteria.region(currentUser.getUser().getRegion()));
			criteria.region(currentUser.getUser().getRegion());
			provinceFilter.setEnabled(false);
			refreshGridData();
		}

		layout.add(provinceFilter);

		searchField.addValueChangeListener(e -> {
			criteria.nameEpidLike(e.getValue());// nameLike(e.getValue());
			resetFilters.setVisible(true);
			refreshGridData();
		});

		regionFilter.addValueChangeListener(e -> {
			if (regionFilter.getValue() != null) {
				AreaReferenceDto area = e.getValue();
				criteria.area(area);
				provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid()));
				refreshGridData();
			}else {
				criteria.area(null);
				refreshGridData();
			}
			resetFilters.setVisible(true);
		
		});

		provinceFilter.addValueChangeListener(e -> {
			if (regionFilter.getValue() != null) {
			RegionReferenceDto province = e.getValue();
			criteria.region(province);
			refreshGridData();
			}else {
				criteria.region(null);
				refreshGridData();
			}

		
		});

		riskFilter.setClearButtonVisible(true);
		riskFilter.setItems("Low Risk (LR)", "Medium Risk (MR)", "High Risk (HR)");
		riskFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				criteria.risk(e.getValue().toString());
//				filteredDataProvider.setFilter(criteria.risk(e.getValue().toString()));
				refreshGridData();
			} else {
				criteria.risk(null);
				refreshGridData();
			}
			refreshGridData();

		});

		relevanceStatusFilter.setItems(EntityRelevanceStatus.values());
		relevanceStatusFilter.setItemLabelGenerator(status -> {
			if (status == EntityRelevanceStatus.ARCHIVED) {
				return I18nProperties.getCaption(Captions.districtArchivedDistricts);
			} else if (status == EntityRelevanceStatus.ACTIVE) {
				return I18nProperties.getCaption(Captions.districtActiveDistricts);
			} else if (status == EntityRelevanceStatus.ALL) {
				return I18nProperties.getCaption(Captions.districtAllDistricts);

			}
			// Handle other enum values if needed
			return status.toString();
		});
		relevanceStatusFilter.addValueChangeListener(e -> {
			if (relevanceStatusFilter.getValue().equals(EntityRelevanceStatus.ACTIVE)) {
				subMenu.removeAll();
				subMenu.addItem("Archive", event -> handleArchiveDearchiveAction());
			} else if (relevanceStatusFilter.getValue().equals(EntityRelevanceStatus.ARCHIVED)) {
				subMenu.removeAll();
				subMenu.addItem("De-Archive", event -> handleArchiveDearchiveAction());

			} else {
				subMenu.removeAll();
				subMenu.addItem("Select Either Active or Archived Relevance to carry out a bulk action");
			}
			criteria.relevanceStatus(e.getValue());
//			filteredDataProvider.setFilter(criteria.relevanceStatus((EntityRelevanceStatus) e.getValue()));
			refreshGridData();
			if(relevanceStatusFilter.getValue() == null) {
				criteria.relevanceStatus(null);
				refreshGridData();
			}
		});
		layout.add(riskFilter);
		layout.add(relevanceStatusFilter);
		relevancelayout.add(countRowItems);

		resetFilters.addClassName("resetButton");
//		resetFilters.setVisible(false);
		resetFilters.addClickListener(e -> {
			if (!searchField.isEmpty()) {
				refreshGridData();
				searchField.clear();
			}
			if (!regionFilter.isEmpty()) {
				refreshGridData();
				regionFilter.clear();
			}
			if (!provinceFilter.isEmpty()) {
				refreshGridData();
				provinceFilter.clear();
			}
			if (!riskFilter.isEmpty()) {
				refreshGridData();
				riskFilter.clear();
			}
			if (!relevanceStatusFilter.isEmpty()) {
				refreshGridData();
				relevanceStatusFilter.clear();
			}
			refreshGridData();
			updateRowCount();

		});
		layout.add(resetFilters);

		Button addNew = new Button("Add New District");
		addNew.getElement().getStyle().set("white-space", "normal");
		addNew.getStyle().set("color", "white");
		addNew.getStyle().set("background", "#0D6938");
		addNew.addClickListener(event -> {
			createOrEditDistrict(districtIndexDto);
		});
		layout.add(addNew, anchor);
		layout.setWidth("88%");
		layout.addClassName("pl-3");
		layout.addClassName("row");
		vlayout.setWidth("99%");
		vlayout.add(displayFilters, layout, relevancelayout);
		add(vlayout);

		dropdownBulkOperations.getStyle().set("margin-top", "5px");

		enterBulkEdit.addClassName("bulkActionButton");
		Icon bulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		enterBulkEdit.setIcon(bulkModeButtonnIcon);
		layout.add(enterBulkEdit);

		enterBulkEdit.addClickListener(e -> {
			dropdownBulkOperations.setVisible(true);
			grid.setSelectionMode(Grid.SelectionMode.MULTI);
			enterBulkEdit.setVisible(false);
			leaveBulkEdit.setVisible(true);

		});

		leaveBulkEdit.setText("Leave Bulk Edit Mode");
		leaveBulkEdit.addClassName("leaveBulkActionButton");
		leaveBulkEdit.setVisible(false);
		Icon leaveBulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		leaveBulkEdit.setIcon(leaveBulkModeButtonnIcon);
		layout.add(leaveBulkEdit);
		layout.addClassName("pl-3");
		layout.addClassName("row");

		leaveBulkEdit.addClickListener(e -> {
			grid.setSelectionMode(Grid.SelectionMode.SINGLE);
			enterBulkEdit.setVisible(true);
			leaveBulkEdit.setVisible(false);
			dropdownBulkOperations.setVisible(false);
		});
		dropdownBulkOperations.setVisible(false);
		layout.add(dropdownBulkOperations);

		return vlayout;
	}

	private void handleArchiveDearchiveAction() {

		archiveDearchiveAllSelectedItems(grid.getSelectedItems());
		Notification.show("Delete action selected!");
	}

	public void archiveDearchiveAllSelectedItems(Collection<DistrictIndexDto> selectedRows) {
		archiveDearchiveConfirmation = new ConfirmDialog();

		if (selectedRows.size() == 0) {

			archiveDearchiveConfirmation.setCancelable(true);
			archiveDearchiveConfirmation.addCancelListener(e -> archiveDearchiveConfirmation.close());
			archiveDearchiveConfirmation.setRejectable(true);
			archiveDearchiveConfirmation.addRejectListener(e -> archiveDearchiveConfirmation.close());
			archiveDearchiveConfirmation.setConfirmText("Ok");

			archiveDearchiveConfirmation.setHeader("Error Archiving");
			archiveDearchiveConfirmation
					.setText("You have not selected any data to be Archived, please make a selection.");

			archiveDearchiveConfirmation.open();
		} else {
			archiveDearchiveConfirmation.setCancelable(true);
			archiveDearchiveConfirmation.setRejectable(true);
			archiveDearchiveConfirmation.setRejectText("No");
			archiveDearchiveConfirmation.setConfirmText("Yes");
			archiveDearchiveConfirmation.addCancelListener(e -> archiveDearchiveConfirmation.close());
			archiveDearchiveConfirmation.addRejectListener(e -> archiveDearchiveConfirmation.close());
			archiveDearchiveConfirmation.open();

			for (DistrictIndexDto selectedRow : (Collection<DistrictIndexDto>) selectedRows) {

				dto = new DistrictDto();
				String regionUUid = selectedRow.getUuid();
				dto = FacadeProvider.getDistrictFacade().getByUuid(regionUUid);
				boolean archive = dto.isArchived();

				System.out.println(archive + " archived or not " + regionUUid + "selected region  uuid");
				if (!archive) {
					archiveDearchiveConfirmation.setHeader("Archive Selected Districts");
					archiveDearchiveConfirmation.setText("Are you sure you want to Archive the selected Districts?");
					archiveDearchiveConfirmation.addConfirmListener(e -> {
						FacadeProvider.getDistrictFacade().archive(selectedRow.getUuid());
//						if (leaveBulkEdit.isVisible()) {
//							leaveBulkEdit.setVisible(false);
//							enterBulkEdit.setVisible(true);
//							grid.setSelectionMode(Grid.SelectionMode.SINGLE);
//							dropdownBulkOperations.setVisible(false);
//						}
						refreshGridData();
					});
//					Notification.show("Archiving Selected Rows ");
				} else {
					archiveDearchiveConfirmation.setHeader("De-Archive Selected Districts");
					archiveDearchiveConfirmation.setText("Are you sure you want to De-Archive the selected Districts?");
					archiveDearchiveConfirmation.addConfirmListener(e -> {
						FacadeProvider.getDistrictFacade().dearchive(selectedRow.getUuid());
//						if (leaveBulkEdit.isVisible()) {
//							leaveBulkEdit.setVisible(false);
//							enterBulkEdit.setVisible(true);
//							grid.setSelectionMode(Grid.SelectionMode.SINGLE);
//							dropdownBulkOperations.setVisible(false);
//						}
						refreshGridData();
					});
//					Notification.show("De- Archiving Selected Rows ");
				}
			}

		}
	}

	public void clearFilters() {

	}

	public boolean createOrEditDistrict(DistrictIndexDto districtIndexDto) {
		Dialog dialog = new Dialog();
		FormLayout fmr = new FormLayout();

		TextField nameField = new TextField("Name");

		TextField dCodeField = new TextField("DCode");
		ComboBox<RegionReferenceDto> provinceOfDistrict = new ComboBox<>("Province");
		provinceOfDistrict.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
		ComboBox<String> risk = new ComboBox<>("Risk");
		risk.setItems("Low Risk (LW)", "Medium Risk (MD)", "High Risk (HR)");

		if (districtIndexDto != null) {
			nameField.setValue(districtIndexDto.getName());
			dCodeField.setValue(districtIndexDto.getExternalId().toString());
			provinceOfDistrict.setItems(districtIndexDto.getRegion());
			provinceOfDistrict.setValue(districtIndexDto.getRegion());
//		provinceOfDistrict.isReadOnly();
			provinceOfDistrict.setEnabled(true);
		}

		// this can generate null

		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);

		Button saveButton = new Button("Save");
		Button discardButton = new Button("Discard", e -> dialog.close());
		saveButton.getStyle().set("margin-right", "10px");
		Button archiveButton = new Button();

		if (districtIndexDto != null) {

			dto = new DistrictDto();
			String regionUUid = districtIndexDto.getUuid();
			dto = FacadeProvider.getDistrictFacade().getByUuid(regionUUid);
			boolean isArchivedx = dto.isArchived();
			archiveButton.setText(isArchivedx ? "De-Archive" : "Archive");

			Collection<?> selectedRows;
			selectedRows = grid.getSelectedItems();
			Set<String> selectedRowsUuids = selectedRows.stream().map(row -> ((HasUuid) row).getUuid())
					.collect(Collectors.toSet());
			System.out.println(selectedRowsUuids + " selected row infracsucfhshfvshfhjvs");
			archiveButton.addClickListener(archiveEvent -> {

				if (districtIndexDto != null) {

					archiveDearchiveConfirmation = new ConfirmDialog();
					archiveDearchiveConfirmation.setCancelable(true);
					archiveDearchiveConfirmation.addCancelListener(e -> dialog.close());
					archiveDearchiveConfirmation.setRejectable(true);
					archiveDearchiveConfirmation.setRejectText("No");
					archiveDearchiveConfirmation.addRejectListener(e -> dialog.close());
					archiveDearchiveConfirmation.setConfirmText("Yes");
					archiveDearchiveConfirmation.open();
					uuidsz = dto.getUuid();

					System.out.println(uuidsz + "areeeeeeeeeeeeeeeeeaaaaaaaaaaaaaaa by uuid");
					boolean isArchived = dto.isArchived();
					if (uuidsz != null) {
						if (isArchived == true) {

							archiveDearchiveConfirmation.setHeader("De-Archive District");
							archiveDearchiveConfirmation.setText("Are you sure you want to De-archive this District? ");

							archiveDearchiveConfirmation.addConfirmListener(e -> {
								FacadeProvider.getDistrictFacade().dearchive(uuidsz);
								dialog.close();
								refreshGridData();
							});
//							Notification.show("Dearchiving Area");

						} else {
							archiveDearchiveConfirmation.setHeader("Archive District");
							archiveDearchiveConfirmation.setText("Are you sure you want to Archive this District? ");

							archiveDearchiveConfirmation.addConfirmListener(e -> {
								FacadeProvider.getDistrictFacade().archive(uuidsz);
								dialog.close();
								refreshGridData();
							});

						}
					}
				}
			});
		}

		saveButton.addClickListener(saveEvent -> {

			String name = nameField.getValue();
			String code = dCodeField.getValue();

			String uuids = "";
			if (districtIndexDto != null) {
				uuids = districtIndexDto.getUuid();
			}
			if (name != null && code != null) {

				DistrictDto dce = FacadeProvider.getDistrictFacade().getDistrictByUuid(uuids);
				if (dce != null) {
					dce.setName(name);
					long rcodeValue = Long.parseLong(code);
					dce.setExternalId(rcodeValue);
					dce.setRegion(provinceOfDistrict.getValue());
					if (dce.getRisk() != null) {
						dce.setRisk(risk.getValue());
					}
					FacadeProvider.getDistrictFacade().save(dce, true);
					Notification.show("Saved: " + name + " " + code);
					dialog.close();
					refreshGridData();
				} else {
					DistrictDto dcex = new DistrictDto();

					dcex.setName(name);
					long rcodeValue = Long.parseLong(code);
					dcex.setExternalId(rcodeValue);
					dcex.setRegion(provinceOfDistrict.getValue());
					dcex.setRisk(risk.getValue());
					FacadeProvider.getDistrictFacade().save(dcex, true);
					Notification.show("Saved: " + name + " " + code);
					dialog.close();
					refreshGridData();
				}
			} else {
				Notification.show("Not Valid Value: " + name + " " + code);
			}

		});

//		dialog.setHeaderTitle("Edit " + districtIndexDto.getName());
		if (districtIndexDto == null) {
			dialog.setHeaderTitle("Add New District");
			dialog.getFooter().add(discardButton, saveButton);
		} else {
			dialog.setHeaderTitle("Edit " + districtIndexDto.getName());
			dialog.getFooter().add(archiveButton, discardButton, saveButton);

		}
		fmr.add(nameField, dCodeField, provinceOfDistrict, risk);
		dialog.add(fmr);

//       getStyle().set("position", "fixed").set("top", "0").set("right", "0").set("bottom", "0").set("left", "0")
//		.set("display", "flex").set("align-items", "center").set("justify-content", "center");

		dialog.open();

		return true;
	}

	private void updateRowCount() {
		int numberOfRows = filteredDataProvider.size(new Query<>());
		String newText = "Rows : " + numberOfRows;

		countRowItems.setText(newText);
		countRowItems.setId("rowCount");
	}

	private void refreshGridData() {
		ListDataProvider<DistrictIndexDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getDistrictFacade().getIndexList(criteria, null, null, null).stream());

		dataView = grid.setItems(dataProvider);
		itemCount = dataProvider.getItems().size();
		String newText = "Rows : " + itemCount;
		countRowItems.setText(newText);
		countRowItems.setId("rowCount");

//		dataView = grid.setItems(dataProvider);
	}

}