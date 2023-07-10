package com.cinoteck.application.views.campaign;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.diagram.CampaignDashboardElement;
import com.vaadin.flow.component.textfield.IntegerField;


public class CampaignDashboardGridElementComponent extends VerticalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5040277864446152755L;
	List<CampaignDashboardElement> savedElements;
	List<CampaignDashboardElement> allElements;
	Grid<CampaignDashboardElement> grid = new Grid<>(CampaignDashboardElement.class, false);
	CampaignDto campaignDto;
	private CampaignDashboardElement formBeenEdited;
	private String campaignPhase;

	public CampaignDashboardGridElementComponent(List<CampaignDashboardElement> savedElements,
			List<CampaignDashboardElement> allElements, CampaignDto campaignDto, String campaignPhase) {
		this.savedElements = savedElements;
		this.allElements = allElements;
		this.campaignDto = campaignDto;
		this.campaignPhase = campaignPhase;
		

		grid.addColumn(CampaignDashboardElement::getDiagramId).setHeader("Chart");
		grid.addColumn(CampaignDashboardElement::getTabId).setHeader("Tab ID");
		grid.addColumn(CampaignDashboardElement::getSubTabId).setHeader("SubTab ID");
		grid.addColumn(CampaignDashboardElement::getWidth).setHeader("Width");
		grid.addColumn(CampaignDashboardElement::getHeight).setHeader("Height");
		grid.addColumn(CampaignDashboardElement::getOrder).setHeader("Order");
		grid.setItems(savedElements);
		
		addClassName("list-view");
		setSizeFull();
		add(getContent());
	}

	private Component getContent() {
		VerticalLayout formx = editorForm();
		formx.getStyle().remove("width");
		HorizontalLayout content = new HorizontalLayout(grid, formx);
		content.setFlexGrow(4, grid);
		content.setFlexGrow(1, formx);
		content.addClassNames("content");
		content.setSizeFull();
		return content;
	}

	private VerticalLayout editorForm() {
		FormLayout formx = new FormLayout();
		VerticalLayout vert = new VerticalLayout();
		
		Button plusButton = new Button(new Icon(VaadinIcon.PLUS));
		plusButton.addThemeVariants(ButtonVariant.LUMO_ICON);
		plusButton.setTooltipText("Add new form");
		
		
		 Button deleteButton = new Button(new Icon(VaadinIcon.DEL_A));
		 deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON);
		 deleteButton.getStyle().set("background-color", "red!important");
		 deleteButton.setTooltipText("Remove this form");
	        
	        Button saveButton = new Button("Save",
	                new Icon(VaadinIcon.CHECK));
	        
	        Button cacleButton = new Button("Cancle",
	                new Icon(VaadinIcon.REFRESH));
		
		ComboBox<CampaignDashboardElement> charts = new ComboBox<CampaignDashboardElement>();
		charts.setLabel("Charts");
		charts.setItems(allElements);
		charts.setItemLabelGenerator(item -> getItemCaption(item));
		// if its a clicked action set the value from the item....TODO

		
		List<String> tempListTabId = new ArrayList<String>();
		if(campaignDto != null) {
			for(CampaignDashboardElement elex : campaignDto.getCampaignDashboardElements(campaignPhase))
			tempListTabId.add(elex.getTabId());
		}
		
		List<String> tempListSubTabId = new ArrayList<String>();
		if(campaignDto != null) {
			for(CampaignDashboardElement elex : campaignDto.getCampaignDashboardElements(campaignPhase))
				tempListSubTabId.add(elex.getSubTabId());
		}
		
		
		ComboBox<String> tabID = new ComboBox<String>();
		tabID.setLabel("Tab ID");
		tabID.setItems(tempListTabId);
		tabID.setAllowCustomValue(true);
		tabID.addCustomValueSetListener(e -> {
		    String customValue = e.getDetail();
		    tempListTabId.add(customValue);
		    tabID.setItems(tempListTabId);
		    tabID.setValue(customValue);
		});
		
		
		ComboBox<String> subTabID = new ComboBox<String>();
		subTabID.setLabel("SubTab ID");
		subTabID.setItems(tempListSubTabId);
		subTabID.setAllowCustomValue(true);
		subTabID.addCustomValueSetListener(e -> {
		    String customValue = e.getDetail();
		    tempListSubTabId.add(customValue);
		    subTabID.setItems(tempListSubTabId);
		    subTabID.setValue(customValue);
		});
		
		
		IntegerField tabWidth = new IntegerField();
		tabWidth.setLabel("Width");
		tabWidth.setMin(10);
		tabWidth.setMax(100);
		tabWidth.setStep(5);
		tabWidth.setStepButtonsVisible(true);
		
		
		IntegerField tabHeight = new IntegerField();
		tabHeight.setLabel("Height");
		tabHeight.setMin(10);
		tabHeight.setMax(100);
		tabHeight.setStep(5);
		tabHeight.setStepButtonsVisible(true);
		
		IntegerField tabOrder = new IntegerField();
		tabOrder.setLabel("Order");
		tabOrder.setMin(0);
		tabOrder.setMax(100);
		tabOrder.setStepButtonsVisible(true);
		
		
		 HorizontalLayout buttonLay = new HorizontalLayout(plusButton, deleteButton);
		 
		// buttonLay.setEnabled(false);
		 
		 HorizontalLayout buttonAfterLay = new HorizontalLayout(saveButton, cacleButton);
		 buttonAfterLay.getStyle().set("flex-wrap", "wrap");
		 buttonAfterLay.setJustifyContentMode(JustifyContentMode.END);
		 buttonLay.setSpacing(true);
		
		 grid.addSelectionListener(ee -> {
			
			    int size = ee.getAllSelectedItems().size();
			    if(size > 0) {
			    	CampaignDashboardElement selectedCamp = ee.getFirstSelectedItem().get();
					 formBeenEdited = selectedCamp;
			    boolean isSingleSelection = size == 1;
			    buttonLay.setEnabled(isSingleSelection);
			    buttonAfterLay.setEnabled(isSingleSelection);
			    
			    formx.setVisible(true);
				buttonAfterLay.setVisible(true);
				
				charts.setValue(selectedCamp);
				tabID.setValue(selectedCamp.getTabId());
				subTabID.setValue(selectedCamp.getSubTabId());
				tabWidth.setValue(selectedCamp.getWidth());
				tabHeight.setValue(selectedCamp.getHeight());
				tabOrder.setValue(selectedCamp.getOrder());
				
			    saveButton.setText("Update");
			    } else {
			    	formBeenEdited = new CampaignDashboardElement();
			    }
			});
		 
		 deleteButton.addClickListener(dex->{
			 if(formBeenEdited == null) {
				 Notification.show("Please select a form first");
			 } else {

			 campaignDto.getCampaignDashboardElements().remove(formBeenEdited);
			// FacadeProvider.getCampaignFacade().saveCampaign(capdto); 
			 Notification.show(formBeenEdited+" was removed from the Campaign");
			 grid.setItems(campaignDto.getCampaignDashboardElements(campaignPhase));
			 }
			 grid.setItems(campaignDto.getCampaignDashboardElements(campaignPhase));
		 });
		 
		 plusButton.addClickListener(ce->{
			 CampaignDashboardElement newcampform = new CampaignDashboardElement();
			 
			 formx.setVisible(true);
			 buttonAfterLay.setVisible(true);
			 
			 try {
				 charts.setValue(newcampform);
			 }finally {
				
					tabID.setValue("");
					subTabID.setValue("");
					tabWidth.setValue(0);
					tabHeight.setValue(0);
					tabOrder.setValue(0);
			 }
			 grid.setItems(campaignDto.getCampaignDashboardElements(campaignPhase));
		 });
		 
		 cacleButton.addClickListener(ees -> {
			 CampaignDashboardElement newcampform = new CampaignDashboardElement();
			 
			 formx.setVisible(false);
			 buttonAfterLay.setVisible(false);
			 
			 try {
				 charts.setValue(newcampform);
			 }finally {
				
					tabID.setValue("");
					subTabID.setValue("");
					tabWidth.setValue(0);
					tabHeight.setValue(0);
					tabOrder.setValue(0);
			 }
			 saveButton.setText("Save");
			 
			 grid.setItems(campaignDto.getCampaignDashboardElements(campaignPhase));
			 
		 });
		 
		 
		 saveButton.addClickListener(e->{
			 
			 if(((Button) e.getSource()).getText().equals("Save")) {
				 //TODO we need validator on the items before we accept them to database because we are not using binder...
				 CampaignDashboardElement newCampForm = charts.getValue();
				 newCampForm.setTabId(tabID.getValue());
				 newCampForm.setSubTabId(subTabID.getValue());
				 newCampForm.setWidth(tabWidth.getValue());
				 newCampForm.setHeight(tabHeight.getValue());
				 newCampForm.setOrder(tabOrder.getValue());
				 
				 campaignDto.getCampaignDashboardElements().add(newCampForm);
				 
				// FacadeProvider.getCampaignFacade().saveCampaign(capdto);
				 
				 allElements.removeAll(campaignDto.getCampaignDashboardElements());
				 charts.setItems(allElements);
				 
				 
				 Notification.show("New Dashboard Chart added successfully");
				 grid.setItems(campaignDto.getCampaignDashboardElements(campaignPhase));
			 } else {
				 //formBeenEdited
				 if(formBeenEdited != null) {
					 CampaignDashboardElement newCampForm = charts.getValue();
					 newCampForm.setTabId(tabID.getValue());
					 newCampForm.setSubTabId(subTabID.getValue());
					 newCampForm.setWidth(tabWidth.getValue());
					 newCampForm.setHeight(tabHeight.getValue());
					 newCampForm.setOrder(tabOrder.getValue());
					 
					 
				 campaignDto.getCampaignDashboardElements().remove(formBeenEdited);
				 campaignDto.getCampaignDashboardElements().add(newCampForm);
				 //FacadeProvider.getCampaignFacade().saveCampaign(capdto);
				 grid.setItems(campaignDto.getCampaignDashboardElements(campaignPhase));
				 
				 allElements.removeAll(campaignDto.getCampaignDashboardElements());
				 charts.setItems(allElements);
				 
				 Notification.show("Campaign Updated");
				 } else {
					 Notification.show("Please select a form before you update");
				 }
			 }
		 });
		 
		 
		formx.add(charts, tabID, subTabID, tabWidth, tabHeight, tabOrder);
		formx.setColspan(charts, 3);
		formx.setColspan(tabID, 3);
		formx.setColspan(subTabID, 3);
		formx.setColspan(tabWidth, 1);
		formx.setColspan(tabHeight, 1);
		formx.setColspan(tabOrder, 1);
		
		formx.setVisible(false);
		buttonAfterLay.setVisible(false);
		
		vert.add(buttonLay, formx, buttonAfterLay);
		
		return vert;
	}
	
	private String getItemCaption(CampaignDashboardElement item) {
		return item.getDiagramId();
	}

	public CampaignDto getModifiedDto() {
		
		return campaignDto;
	}
}