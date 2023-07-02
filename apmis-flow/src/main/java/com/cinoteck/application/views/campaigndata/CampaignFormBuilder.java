package com.cinoteck.application.views.campaigndata;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.ConversionException;

import com.cinoteck.application.UserProvider;
import com.google.common.collect.Sets;

//import org.hibernate.internal.build.AllowSysOut;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.GeneratedVaadinTextField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.VaadinService;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.MapperUtil;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.translation.TranslationElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElementStyle;
import de.symeda.sormas.api.campaign.form.CampaignFormElementOptions;
import de.symeda.sormas.api.campaign.form.CampaignFormElementType;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserDto;

public class CampaignFormBuilder extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7153373681106891254L;
	private final List<CampaignFormElement> formElements;
	private final Map<String, Object> formValuesMap;
	//private final FormLayout campaignFormLayout; 
	private final Locale userLocale;
	private Map<String, String> userTranslations = new HashMap<String, String>();
	private Map<String, String> userOptTranslations = new HashMap<String, String>();
	Map<String, Component> fields;
	private Map<String, String> optionsValues = new HashMap<String, String>();
	private List<String> constraints;
	private List<CampaignFormTranslations> translationsOpt;
	private CampaignReferenceDto campaignReferenceDto;
	
	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;
	Binder<CampaignFormDataDto> binder = new BeanValidationBinder<>(CampaignFormDataDto.class);
	UserProvider currentUser = new UserProvider();

	public CampaignFormBuilder(List<CampaignFormElement> formElements, List<CampaignFormDataEntry> formValues, CampaignReferenceDto campaignReferenceDto,
			 List<CampaignFormTranslations> translations, String formName) {
		this.formElements = formElements;
		if (formValues != null) {
			this.formValuesMap = new HashMap<>();
			formValues.forEach(formValue -> formValuesMap.put(formValue.getId(), formValue.getValue()));
		} else {
			this.formValuesMap = new HashMap<>();
		}
	//	this.campaignFormLayout = new FormLayout();
		this.fields = new HashMap<>();
		this.translationsOpt = translations;

		this.userLocale = I18nProperties.getUserLanguage().getLocale();
		if (userLocale != null) {
			translations.stream().filter(t -> t.getLanguageCode().equals(userLocale.toString())).findFirst().ifPresent(
					filteredTranslations -> userTranslations = filteredTranslations.getTranslations().stream().collect(
							Collectors.toMap(TranslationElement::getElementId, TranslationElement::getCaption)));
		}
		
		
		FormLayout vertical_ = new FormLayout();
		
		Label formNam = new Label();
		formNam.getElement().setProperty("innerHTML", "<h3>"+formName+"</h3>");
		vertical_.setColspan(formNam, 3);
		vertical_.add(formNam);
		
		
		ComboBox<Object> cbCampaign = new ComboBox<>(CampaignFormDataDto.CAMPAIGN);
		
		cbCampaign.setItems(FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference());
		cbCampaign.setValue(campaignReferenceDto);
		cbCampaign.setEnabled(false);
		
		
		Date date = new Date();

		DatePicker formDate = new DatePicker();
		formDate.setLabel(CampaignFormDataDto.FORM_DATE);
//		formDate.setValue(localDate);
		
		//
		ComboBox<AreaReferenceDto> cbArea = new ComboBox<>(CampaignFormDataDto.AREA);
		ComboBox<RegionReferenceDto> cbRegion = new ComboBox<>(CampaignFormDataDto.REGION);
		ComboBox<DistrictReferenceDto> cbDistrict = new ComboBox<>(CampaignFormDataDto.DISTRICT);
		ComboBox<CommunityReferenceDto> cbCommunity = new ComboBox<>(CampaignFormDataDto.COMMUNITY);
		cbRegion.setEnabled(false);
		cbDistrict.setEnabled(false);
		cbCommunity.setEnabled(false);
		cbArea.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
		
		
		//listeners logic
		cbArea.addValueChangeListener(e -> {
			if(e.getValue() != null) {
				provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
				cbRegion.clear();
				cbRegion.setEnabled(true);
				cbRegion.setItems(provinces);
				cbDistrict.clear();
				cbDistrict.setEnabled(false);
				cbCommunity.clear();
				cbCommunity.setEnabled(false);
			}else {
				cbRegion.clear();
				cbRegion.setEnabled(false);
				cbDistrict.clear();
				cbDistrict.setEnabled(false);
				cbCommunity.clear();
				cbCommunity.setEnabled(false);
			}
			
		});

		cbRegion.addValueChangeListener(e -> {
			if(e.getValue() != null) {
				districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
				cbDistrict.setEnabled(true);
				cbDistrict.setItems(districts);
				cbCommunity.clear();
				cbCommunity.setEnabled(false);
			}else {
				cbDistrict.clear();
				cbDistrict.setEnabled(false);
				cbCommunity.clear();
				cbCommunity.setEnabled(false);
			}
			
		});

		cbDistrict.addValueChangeListener(e -> {
			if(e.getValue() != null) {
				communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());
				cbCommunity.clear();
				cbCommunity.setEnabled(true);
				cbCommunity.setItems(communities);
			} else {
				cbCommunity.clear();
				cbCommunity.setEnabled(false);
			}	
		});

		cbCommunity.addValueChangeListener(e -> {
			//this should open the rest of the form
//			dataView.addFilter(t -> t.getCommunity().toString().equalsIgnoreCase(clusterCombo.getValue().toString()));
		});

	
		vertical_.add(cbCampaign, formDate, cbArea, cbRegion, cbDistrict, cbCommunity);
		
//		CampaignFormBuilder(List<CampaignFormElement> formElements, List<CampaignFormDataEntry> formValues,
//				VerticalLayout campaignFormLayout, List<CampaignFormTranslations> translations)
		vertical_.setResponsiveSteps(
				 new ResponsiveStep("0", 1),
			        new ResponsiveStep("520px", 2),
			        new ResponsiveStep("1000px", 3));
		add(vertical_);
		
		
		
		//check logged in user ristriction level
				if(currentUser.getUser().getArea() != null) {
					cbArea.setValue(currentUser.getUser().getArea());
					cbArea.setEnabled(false);
					
					List<RegionReferenceDto> provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(currentUser.getUser().getArea().getUuid());
					cbRegion.clear();
					cbRegion.setEnabled(true);
					cbRegion.setItems(provinces);
				}
				
				if(currentUser.getUser().getRegion() != null) {
					cbRegion.setValue(currentUser.getUser().getRegion());
					cbRegion.setEnabled(false);
					
					List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(currentUser.getUser().getRegion().getUuid());
					cbDistrict.clear();
					cbDistrict.setEnabled(true);
					cbDistrict.setItems(districts);
				}
				
				if(currentUser.getUser().getDistrict() != null) {
					cbDistrict.setValue(currentUser.getUser().getDistrict());
					cbDistrict.setEnabled(false);
					
					List<CommunityReferenceDto> districts = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(currentUser.getUser().getDistrict().getUuid());
					cbCommunity.clear();
					cbCommunity.setEnabled(true);
					cbCommunity.setItems(districts);
				}
				
				
				
				
				
		buildForm();
	
//		binder.forField(firstName).asRequired("First Name is Required").bind(UserDto::getFirstName,
//				UserDto::setFirstName);

//		binder.forField(cbArea).asRequired().bind(CampaignFormDataDto::getArea, CampaignFormDataDto::setArea);
//		binder.forField(cbCampaign).asRequired().bind(CampaignFormDataDto::getCampaign, CampaignFormDataDto::setCampaign);
//		binder.forField(cbRegion).asRequired().bind(CampaignFormDataDto::getArea, CampaignFormDataDto::setArea);
//		binder.forField(cbDistrict).asRequired().bind(CampaignFormDataDto::getArea, CampaignFormDataDto::setArea);
//		binder.forField(cbCommunity).asRequired().bind(CampaignFormDataDto::getArea, CampaignFormDataDto::setArea);
		
	}

	public void buildForm() {
		int currentCol = -1;
		// GridLayout currentLayout = campaignFormLayout;
		int sectionCount = 0;

		int ii = 0;
		// System.out.println("Got one____");
		FormLayout vertical = new FormLayout();

		TabSheet accrd = new TabSheet();
		accrd.setHeight(750, Unit.PIXELS);

		int accrd_count = 0;
		
		for (CampaignFormElement formElement : formElements) {
			CampaignFormElementOptions campaignFormElementOptions = new CampaignFormElementOptions();
			CampaignFormElementType type = CampaignFormElementType.fromString(formElement.getType());
			String fieldId = formElement.getId();
			List<CampaignFormElementStyle> styles;
			if (formElement.getStyles() != null) {
				styles = Arrays.stream(formElement.getStyles()).map(CampaignFormElementStyle::fromString)
						.collect(Collectors.toList());
			} else {
				styles = new ArrayList<>();
			}

			if (formElement.getOptions() != null) {
				// userOptTranslations = null;
				campaignFormElementOptions = new CampaignFormElementOptions();
				
				optionsValues = formElement.getOptions().stream()
						.collect(Collectors.toMap(MapperUtil::getKey, MapperUtil::getCaption)); // .collect(Collectors.toList());

				if (userLocale != null) {
					
					translationsOpt.stream().filter(t -> t.getLanguageCode().equals(userLocale.toString())).findFirst()
							.ifPresent(filteredTranslations -> filteredTranslations.getTranslations().stream()
									.filter(cd -> cd.getElementId().equals(formElement.getId())).findFirst()
									.ifPresent(optionsList -> userOptTranslations = optionsList.getOptions().stream()
											.filter(c -> c.getCaption() != null)
											.collect(Collectors.toMap(MapperUtil::getKey, MapperUtil::getCaption))));

				}

			

				if (userOptTranslations.size() == 0) {
					campaignFormElementOptions.setOptionsListValues(optionsValues);
					// get18nOptCaption(formElement.getId(), optionsValues));
				} else {
					campaignFormElementOptions.setOptionsListValues(userOptTranslations);

				}

			} else {
				optionsValues = new HashMap<String, String>();
			}

			if (formElement.getConstraints() != null) {
				campaignFormElementOptions = new CampaignFormElementOptions();
				constraints = (List<String>) Arrays.stream(formElement.getConstraints()).collect(Collectors.toList());
				ListIterator<String> lstItemsx = constraints.listIterator();
				int i = 1;
				while (lstItemsx.hasNext()) {
					String lss = lstItemsx.next().toString();
					if (lss.toLowerCase().contains("max")) {
						campaignFormElementOptions.setMax(Integer.parseInt(lss.substring(lss.lastIndexOf("=") + 1)));
					} else if (lss.toLowerCase().contains("min")) {
						campaignFormElementOptions.setMin(Integer.parseInt(lss.substring(lss.lastIndexOf("=") + 1)));
					} else if (lss.toLowerCase().contains("expression")) {
						campaignFormElementOptions.setExpression(true);
					}
				}

			}
			// input:checked

			String dependingOnId = formElement.getDependingOn();
			Object[] dependingOnValues = formElement.getDependingOnValues();

			Object value = formValuesMap.get(formElement.getId());

			int occupiedColumns = getOccupiedColumns(type, styles);

			if (type == CampaignFormElementType.DAYWISE) {
				accrd_count++;
				if (accrd_count > 1) {

					final FormLayout layout = new FormLayout(vertical);
					// layout.addComponent(label);
					int temp = accrd_count;
					temp = temp - 1;
					layout.setClassName("daywise_background_" + temp); // .addStyleName(dependingOnId); sormas background: green
					accrd.add(formElement.getCaption(), layout);

					vertical = new FormLayout();
					vertical.setSizeFull();
					vertical.setWidthFull();
					vertical.setHeightFull();

				}
			} else if (type == CampaignFormElementType.SECTION) {
				sectionCount++;
			
				vertical.setId("formSectionId-" + sectionCount);
				
				

			} else if (type == CampaignFormElementType.LABEL) {
		
				Label labx = new Label();
				labx.getElement().setProperty("innerHTML", formElement.getCaption());
				labx.setId(formElement.getId());
				vertical.setColspan(labx, 3);
				vertical.add(labx);
				//needed
//				if (dependingOnId != null && dependingOnValues != null) {
//					//needed
//					setVisibilityDependency(labx, dependingOnId, dependingOnValues, CampaignFormElementType.LABEL);
//				}
			} else {
				CampaignFormElementOptions constrainsVal = new CampaignFormElementOptions();
				boolean fieldIsRequired = formElement.isImportant();

				if (type == CampaignFormElementType.YES_NO) {
					ToggleButton toggle = new ToggleButton();
					toggle.setId(formElement.getId());
					toggle.setSizeFull();
					toggle.setRequiredIndicatorVisible(formElement.isImportant());
					toggle.setLabelAsHtml(formElement.getCaption() +" : No ");

					toggle.addValueChangeListener(evt -> toggle.setLabel(formElement.getCaption()+" : "+ (evt.getValue() == true ? "YES " : "NO ")));
					setFieldValue(toggle, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
							
					vertical.add(toggle);
					fields.put(formElement.getId(), toggle);
					
				} else if (type == CampaignFormElementType.TEXT) {
					TextField textField = new TextField();
					textField.setLabel(formElement.getCaption());
					//textField.setValue("Ruukinkatu 2");
					textField.setClearButtonVisible(true);
					textField.setPrefixComponent(VaadinIcon.PENCIL.create());
					textField.setId(formElement.getId());
					textField.setSizeFull();
					textField.setRequiredIndicatorVisible(formElement.isImportant());
					setFieldValue(textField, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(textField);
					fields.put(formElement.getId(), textField);
					
				}else if(type == CampaignFormElementType.NUMBER){
					NumberField numberField = new NumberField();
					numberField.setLabel(formElement.getCaption());
					numberField.setId(formElement.getId());
					numberField.setSizeFull();
					numberField.setRequiredIndicatorVisible(formElement.isImportant());
					setFieldValue(numberField, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(numberField);
					fields.put(formElement.getId(), numberField);
					
					//Binder<String> binder = new Binder<>(String.class);

					
					if(fieldId.equalsIgnoreCase("Villagecode")) {
						numberField.setAllowedCharPattern("(?!.*000$).*");
//								 new RegexpValidator("(?!.*000$).*", I18nProperties.getValidationError(
//											errormsg == null ? caption + ": " + Validations.onlyDecimalNumbersAllowed : errormsg, caption) ));
						 
						numberField.addValueChangeListener(e -> {
							if (e.getValue() != null && e.getValue().toString().length() > 2 && e.getValue().toString().length() < 8) {
								if (VaadinService.getCurrentRequest().getWrappedSession()
										.getAttribute("Clusternumber") != null) {
									
									final String des = VaadinService.getCurrentRequest().getWrappedSession()
											.getAttribute("Clusternumber") + e.getValue().toString().substring(0, 3);
									
									numberField.setValue(Double.parseDouble(des));

								}
							}
						});
					
					}
					
					
					
					if(fieldId.equalsIgnoreCase("PopulationGroup_0_4")) {
						
						 
						numberField.addValueChangeListener(e -> {
								if (VaadinService.getCurrentRequest().getWrappedSession()
										.getAttribute("populationdata") != null) {
									
									final String des = VaadinService.getCurrentRequest().getWrappedSession().getAttribute("populationdata").toString();
									numberField.setValue(Double.parseDouble(des));
									numberField.setReadOnly(true);
								}
								
						});
					
					}
					
					
				}else if(type == CampaignFormElementType.RANGE){
					IntegerField integerField = new IntegerField();
					integerField.setLabel(formElement.getCaption());
//					integerField.setHelperText("Max 10 items");
					integerField.setId(formElement.getId());
					integerField.setValue(2);
					integerField.setStepButtonsVisible(true);
					integerField.setSizeFull();
					integerField.setRequiredIndicatorVisible(formElement.isImportant());
					setFieldValue(integerField, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					
					vertical.add(integerField);
					fields.put(formElement.getId(), integerField);
					
					String validationMessageTag = "";
					Map<String, Object> validationMessageArgs = new HashMap<>();

					if (constrainsVal.isExpression()) {
						
						if (!fieldIsRequired) {
						//	ApmisNotification notification = new ApmisNotification("Application submitted!");
						}
						
						constrainsVal.setExpression(false);

					} else {

						if (constrainsVal.getMin() != null || constrainsVal.getMax() != null) {
							
							integerField.setMin(constrainsVal.getMin());
							integerField.setMax(constrainsVal.getMax());
							
							if (constrainsVal.getMin() == null) {
								validationMessageTag = Validations.numberTooBig;
								validationMessageArgs.put("value", constrainsVal.getMax());
							} else if (constrainsVal.getMax() == null) {
								validationMessageTag = Validations.numberTooSmall;
								validationMessageArgs.put("value", constrainsVal.getMin());
							} else {
								validationMessageTag = Validations.numberNotInRange;
								validationMessageArgs.put("min", constrainsVal.getMin());
								validationMessageArgs.put("max", constrainsVal.getMax());
							}
							
							//needed
							// field.addValidator(
							// new NumberValidator(I18nProperties.getValidationError(validationMessageTag,
							// validationMessageArgs), minValue, maxValue));
							

//							((TextField) field).addValidator(new NumberNumericValueValidator(
//									caption.toUpperCase() + ": "
//											+ 
//											
//											I18nProperties.getValidationError(validationMessageTag,
//													validationMessageArgs),
//									constrainsVal.getMin(), constrainsVal.getMax(), true, isOnError));

						}else {
							
							//needed
							//This should throw error as range suppose to have min and max if not taken care by expression
						}
					}
				
					
					
				}else if( type == CampaignFormElementType.DECIMAL) {
					BigDecimalField bigDecimalField = new BigDecimalField();
					bigDecimalField.setLabel(formElement.getCaption());
					bigDecimalField.setWidth("240px");
					bigDecimalField.setValue(new BigDecimal("948205817.472950487"));
					bigDecimalField.setId(formElement.getId());
					bigDecimalField.setSizeFull();
					bigDecimalField.setRequiredIndicatorVisible(formElement.isImportant());
					setFieldValue(bigDecimalField, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(bigDecimalField);
					fields.put(formElement.getId(), bigDecimalField);

				
				} else if (type == CampaignFormElementType.TEXTBOX) {
					TextArea textArea = new TextArea();
					textArea.setWidthFull();
					textArea.setLabel(formElement.getCaption());
					textArea.setId(formElement.getId());
					textArea.setSizeFull();
					textArea.setRequiredIndicatorVisible(formElement.isImportant());
					setFieldValue(textArea, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(textArea);
					fields.put(formElement.getId(), textArea);


				} else if (type == CampaignFormElementType.RADIO) {
					RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
					radioGroup.setLabel(formElement.getCaption());
					HashMap<String, String> data = (HashMap<String, String>) campaignFormElementOptions.getOptionsListValues();
					radioGroup.setItems(data.keySet().stream().collect(Collectors.toList()));
					radioGroup.setItemLabelGenerator(itm -> data.get(itm.toString().trim()));
					radioGroup.setId(formElement.getId());
					radioGroup.setSizeFull();
					radioGroup.setRequiredIndicatorVisible(formElement.isImportant());
					setFieldValue(radioGroup, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(radioGroup);
					fields.put(formElement.getId(), radioGroup);

					

				} else if (type == CampaignFormElementType.RADIOBASIC) {
					RadioButtonGroup<String> radioGroupVert = new RadioButtonGroup<>();
					radioGroupVert.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
					radioGroupVert.setLabel(formElement.getCaption());
					
					HashMap<String, String> data = (HashMap<String, String>) campaignFormElementOptions.getOptionsListValues();
					radioGroupVert.setItems(data.keySet().stream().collect(Collectors.toList()));
					radioGroupVert.setItemLabelGenerator(itm -> data.get(itm.toString().trim()));
					
					radioGroupVert.setId(formElement.getId());
					radioGroupVert.setSizeFull();
					radioGroupVert.setRequiredIndicatorVisible(formElement.isImportant());
					setFieldValue(radioGroupVert, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(radioGroupVert);
					
				} else if (type == CampaignFormElementType.DROPDOWN) {
					
					ComboBox<String> select = new ComboBox<>(formElement.getCaption());
					
					//campaignFormElementOptions.getOptionsListValues();
					
					HashMap<String, String> data = (HashMap<String, String>) campaignFormElementOptions.getOptionsListValues();
					
					select.setItems(data.keySet().stream().collect(Collectors.toList()));
					
					
					select.setItemLabelGenerator(itm -> data.get(itm.toString().trim()));
					setFieldValue(select, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					 
					vertical.add(select);
					
					
					
				} else if (type == CampaignFormElementType.CHECKBOX) {
					CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
					checkboxGroup.setLabel(formElement.getCaption());
					
					HashMap<String, String> data = (HashMap<String, String>) campaignFormElementOptions.getOptionsListValues();
					checkboxGroup.setItems(data.keySet().stream().collect(Collectors.toList()));
					checkboxGroup.setItemLabelGenerator(itm -> data.get(itm.toString().trim()));
					
					checkboxGroup.setId(formElement.getId());
					checkboxGroup.setSizeFull();
					checkboxGroup.setRequiredIndicatorVisible(formElement.isImportant());
					setFieldValue(checkboxGroup, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(checkboxGroup);
					fields.put(formElement.getId(), checkboxGroup);


				} else if (type == CampaignFormElementType.CHECKBOXBASIC) {
					CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
					checkboxGroup.setLabel(formElement.getCaption());

					HashMap<String, String> data = (HashMap<String, String>) campaignFormElementOptions.getOptionsListValues();
					checkboxGroup.setItems(data.keySet().stream().collect(Collectors.toList()));
					checkboxGroup.setItemLabelGenerator(itm -> data.get(itm.toString().trim()));
					
					checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
					checkboxGroup.setId(formElement.getId());
					checkboxGroup.setSizeFull();
					checkboxGroup.setRequiredIndicatorVisible(formElement.isImportant());
					setFieldValue(checkboxGroup, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(checkboxGroup);
					fields.put(formElement.getId(), checkboxGroup);



				} else if (type == CampaignFormElementType.DATE) {
					DatePicker.DatePickerI18n singleFormatI18n = new DatePicker.DatePickerI18n();
					singleFormatI18n.setDateFormat("dd-MM-yyyy");

					DatePicker datePicker = new DatePicker(formElement.getCaption());
					datePicker.setI18n(singleFormatI18n);
					datePicker.setSizeFull();
					datePicker.setPlaceholder("DD-MM-YYYY");
					datePicker.setId(formElement.getId());
					datePicker.setRequiredIndicatorVisible(formElement.isImportant());
					setFieldValue(datePicker, type, value, optionsValues, formElement.getDefaultvalue(), false, null);
					vertical.add(datePicker);
					fields.put(formElement.getId(), datePicker);
				}
				
				
				
				
				
				
//needed
//				if (dependingOnId != null && dependingOnValues != null) {
//					//needed
//					setVisibilityDependency((Component) field, dependingOnId, dependingOnValues, type);
//				}
				
				
				
			}
			
			if (accrd_count == 0) {

				add(vertical);
			} else {

				add(accrd);
			}

			userOptTranslations = new HashMap<String, String>(); 
		}
		
		vertical.setSizeFull();
		vertical.setId("vertical_nn");
		vertical.setResponsiveSteps(
		        // Use one column by default
		        new ResponsiveStep("0", 1),
		        new ResponsiveStep("520px", 2),
		        new ResponsiveStep("1000px", 3));
		
//		infrastructureFormLayout.setId("infrastructureFormLayout");
//		infrastructureFormLayout.setWidthFull();
//		infrastructureFormLayout.setResponsiveSteps(
//		        // Use one column by default
//		        new ResponsiveStep("0", 1),
//		        new ResponsiveStep("320px", 2),
//		        new ResponsiveStep("500px", 3));
		
		setId("campaignFormLayout");
		setSizeFull();
	}


	public <T extends Component> void setFieldValue(T field, CampaignFormElementType type, Object value,
			Map<String, String> options, String defaultvalue, Boolean isErrored, Object defaultErrorMsgr) {
		Boolean isExpressionValue = false;
		switch (type) {
		case YES_NO:
			System.out.println(field.getId() +"@@@@@@@   YES_NO  @@@@@@@@@@@2 "+value);
			if (value != null) {
//				value = value.toString().equalsIgnoreCase("YES") ? true
//						: value.toString().equalsIgnoreCase("NO") ? false : value;

				boolean booleanValue = Boolean.parseBoolean((String) value);
				
				((ToggleButton) field).setValue(booleanValue);//Sets.newHashSet(value));
			}
		//	System.out.println(Sets.newHashSet(value)+"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2 "+value);
			
			break;
		case RANGE:
			if (defaultErrorMsgr != null) {
				if (defaultErrorMsgr.toString().endsWith("..")) {
					isExpressionValue = true;
					defaultErrorMsgr = defaultErrorMsgr.toString().equals("..") ? null
							: defaultErrorMsgr.toString().replace("..", "");
				}
			}

			if (isExpressionValue && !isErrored && value == null) {
				
				Object tempz = defaultErrorMsgr != null ? defaultErrorMsgr : "Data entered not in range or calculated rangexxx!";
				String lb = field.getElement().getProperty("label");
				
				field.getElement().setProperty("label", lb == null ? "" : lb);
				field.getElement().setProperty("errorMessage", defaultErrorMsgr != null ? defaultErrorMsgr.toString() : "Data entered not in range or calculated range!");
				
			//	Notification.show("Error found", tempz.toString(), Notification.TYPE_TRAY_NOTIFICATION);
			}
			
			if(value != null) {
				((IntegerField) field).setValue(Integer.parseInt(value.toString()));
				
			}else if(defaultvalue != null) {
				((IntegerField) field).setValue(Integer.parseInt(defaultvalue));
			}

			break;
		case TEXT:
			if (defaultErrorMsgr != null) {
				if (defaultErrorMsgr.toString().endsWith("..")) {
					isExpressionValue = true;
					defaultErrorMsgr = defaultErrorMsgr.toString().equals("..") ? null
							: defaultErrorMsgr.toString().replace("..", "");
				}
			}

			if (isExpressionValue && !isErrored && value == null) {
				
				Object tempz = defaultErrorMsgr != null ? defaultErrorMsgr : "Data entered not in range or calculated rangexxx!";
				String lb = field.getElement().getProperty("label");
				
				field.getElement().setProperty("label", lb == null ? "" : lb);
				field.getElement().setProperty("errorMessage", defaultErrorMsgr != null ? defaultErrorMsgr.toString() : "Data entered not in range or calculated range!");
				
			//	Notification.show("Error found", tempz.toString(), Notification.TYPE_TRAY_NOTIFICATION);
			}

			if(value != null) {
				((TextField) field).setValue(value.toString());
				
			}else if(defaultvalue != null) {
				((TextField) field).setValue(defaultvalue);
			}
			break;
		case NUMBER:
			
			if (defaultErrorMsgr != null) {
				if (defaultErrorMsgr.toString().endsWith("..")) {
					isExpressionValue = true;
					defaultErrorMsgr = defaultErrorMsgr.toString().equals("..") ? null
							: defaultErrorMsgr.toString().replace("..", "");
				}
			}

			if (isExpressionValue && !isErrored && value == null) {
				
				Object tempz = defaultErrorMsgr != null ? defaultErrorMsgr : "Data entered not in range or calculated rangexxx!";
				String lb = field.getElement().getProperty("label");
				
				field.getElement().setProperty("label", lb == null ? "" : lb);
				field.getElement().setProperty("errorMessage", defaultErrorMsgr != null ? defaultErrorMsgr.toString() : "Data entered not in range or calculated range!");
				
			//	Notification.show("Error found", tempz.toString(), Notification.TYPE_TRAY_NOTIFICATION);
			}
			
			if(value != null) {
				((NumberField) field).setValue(Double.parseDouble(value.toString()));
				
			}else if(defaultvalue != null) {
				((NumberField) field).setValue(Double.parseDouble(defaultvalue));
			}
			
			
			break;
			
		
		case DECIMAL:
			if (value != null) {

				((TextField) field).setValue(value != null ? value.toString() : null);
			}
			break;
		case TEXTBOX:

			if (value != null) {

				if (value.equals(true)) {
					((TextArea)field).setEnabled(true);
				} else if (value.equals(false)) {
					((TextArea)field).setEnabled(false);
					// Notification.show("Warning:", Title "Expression resulted in wrong value
					// please check your data 1", Notification.TYPE_WARNING_MESSAGE);
				}
			}
			;
			((TextArea) field).setValue(value != null ? value.toString() : null);
			break;
		case DATE:
			if (value != null) {
				
				try {

					String vc = value + "";
					// System.out.println("@@@===@@ date to parse"+value);
					Date dst = vc.contains("00:00:00") ? dateFormatter(value) : dateFormatterLongAndMobile(value);

					 LocalDate value_Date = dst.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					((DatePicker) field).setValue(value_Date);

				} catch (ConversionException e) {
					// TODO Auto-generated catch block
					((DatePicker) field).setValue(null);
					//e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			;
			break;
		case RADIO:
			((RadioButtonGroup) field).setValue(Sets.newHashSet(value).toString().replace("[", "").replace("]", ""));
			break;
		case RADIOBASIC:
			((RadioButtonGroup) field).setValue(Sets.newHashSet(value).toString().replace("[", "").replace("]", ""));
			break;
		case CHECKBOX:
			if (value != null) {
				String dcs = value.toString().replace("[", "").replace("]", "").replaceAll(", ", ",");
				String strArray[] = dcs.split(",");
				for (int i = 0; i < strArray.length; i++) {
					((CheckboxGroup) field).select(strArray[i]);
				}
			}
			;
			break;
		case CHECKBOXBASIC:

			if (value != null) {
				String dcxs = value.toString().replace("[", "").replace("]", "").replaceAll(", ", ",");
				String strArraxy[] = dcxs.split(",");
				for (int i = 0; i < strArraxy.length; i++) {
					((CheckboxGroup) field).select(strArraxy[i]);
				}
			}
			;
			break;
		case DROPDOWN:

			if (value != null) {

				if (value.equals(true)) {
					((ComboBox) field).setEnabled(true);
				} else if (value.equals(false)) {
					((ComboBox) field).setEnabled(false);
				}
			}
			;
			
			if (defaultvalue != null) {
				//String dxz = options.get(defaultvalue);
				((ComboBox) field).setValue(defaultvalue);
			}
			;
			
			
			if (value != null) {
			//	String dxz = options.get(value);
				((ComboBox) field).setValue(value);
			}
			;
			

			break;
		default:
			throw new IllegalArgumentException(type.toString());
		}
	}
	
	
	
	private int getOccupiedColumns(CampaignFormElementType type, List<CampaignFormElementStyle> styles) {
		List<CampaignFormElementStyle> colStyles = styles.stream().filter(s -> s.toString().startsWith("col"))
				.collect(Collectors.toList());

		if (type == CampaignFormElementType.YES_NO && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.RADIO && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.CHECKBOX && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.DROPDOWN && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.CHECKBOXBASIC && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.RADIOBASIC && !styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.TEXTBOX && !styles.contains(CampaignFormElementStyle.INLINE)
				|| (type == CampaignFormElementType.TEXT || type == CampaignFormElementType.DATE
						|| type == CampaignFormElementType.NUMBER || type == CampaignFormElementType.DECIMAL
						|| type == CampaignFormElementType.RANGE)) {// && styles.contains(CampaignFormElementStyle.ROW))
																	// {
			return 12;
		}

		if (colStyles.isEmpty()) {
			switch (type) {
			case LABEL:
			case SECTION:
				return 12;
			default:
				return 4;
			}
		}

		// Multiple col styles are not supported; use the first one
		String colStyle = colStyles.get(0).toString();
		return Integer.parseInt(colStyle.substring(colStyle.indexOf("-") + 1));
	}

	private float calculateComponentWidth(CampaignFormElementType type, List<CampaignFormElementStyle> styles) {
		List<CampaignFormElementStyle> colStyles = styles.stream().filter(s -> s.toString().startsWith("col"))
				.collect(Collectors.toList());

		if (type == CampaignFormElementType.YES_NO && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.RADIO && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.RADIOBASIC && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.CHECKBOX && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.CHECKBOXBASIC && styles.contains(CampaignFormElementStyle.INLINE)
				|| type == CampaignFormElementType.DROPDOWN && styles.contains(CampaignFormElementStyle.INLINE)
				|| (type == CampaignFormElementType.TEXT || type == CampaignFormElementType.NUMBER
						|| type == CampaignFormElementType.DECIMAL || type == CampaignFormElementType.RANGE
						|| type == CampaignFormElementType.DATE || type == CampaignFormElementType.TEXTBOX)
				// && !styles.contains(CampaignFormElementStyle.ROW)
				|| type == CampaignFormElementType.LABEL || type == CampaignFormElementType.SECTION) {
			return 100f;
		}
		if (1 == 1) {
			return 100f;
		}

		if (colStyles.isEmpty()) {
			// return 33.3f;
		}

		// Multiple col styles are not supported; use the first one
		String colStyle = colStyles.get(0).toString();
		return Integer.parseInt(colStyle.substring(colStyle.indexOf("-") + 1)) / 12f * 100;
	}
	
	
	
//


	private Date dateFormatterLongAndMobile(Object value) {

		String dateStr = value + "";
		DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a");
		DateFormat formatter_ = new SimpleDateFormat("MMM d, yyyy HH:mm:ss");
		DateFormat formatter_x = new SimpleDateFormat("MMM d, yyyy HH:mm:ss a");
		DateFormat formattercx = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		DateFormat formatterx = new SimpleDateFormat("dd/MM/yyyy");
		Date date;
		//System.out.println("date in question " + value); delteme meeeeee  +++++++++++++++++++++++===========

		try {
			date = (Date) formatter.parse(dateStr);
		} catch (ParseException e) {
			System.out.println("date wont parse on " + e.getMessage());
			try {
				date = (Date) formatter_.parse(dateStr);
					} catch (ParseException ex) {
						System.out.println("date wont parse on " + ex.getMessage());
						try {
							date = (Date) formatter_x.parse(dateStr);
						} catch (ParseException edz) {
							System.out.println("date wont parse on " + edz.getMessage());
					try {
						date = (Date) formatterx.parse(dateStr);
					} catch (ParseException ed) {
						System.out.println("date wont parse on " + ed.getMessage());
						
						try {
							date = (Date) formattercx.parse(dateStr);
						} catch (ParseException edx) {
							System.out.println("date wont parse on " + edx.getMessage());
							date = new Date((Long) value);
						}
					}
				}
			}
		}

		return date;
	}

	private Date dateFormatter(Object value) throws ParseException {
		// TODO Auto-generated method stub

		String dateStr = value + "";
		DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
		Date date;

		date = (Date) formatter.parse(dateStr);

		//System.out.println(date);

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String formatedDate = cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1) + "/"
				+ cal.get(Calendar.YEAR);
		//System.out.println("formatedDate : " + formatedDate);

		Date res = new Date(formatedDate + "");

		return res;
	}

	private void setVisibilityDependency(Component component, String dependingOnId,
			Object[] dependingOnValues, CampaignFormElementType typex) {
		Component dependingOnField = fields.get(dependingOnId);
		List<Object> dependingOnValuesList = Arrays.asList(dependingOnValues);

		if (dependingOnField == null) {
			return;
		}
		
		//fieldValueMatchesDependingOnValuesNOTValuer
		if (dependingOnValuesList.stream().anyMatch(v -> v.toString().contains("!"))) {

			// hide on default
			component.setVisible(dependingOnValuesList.stream().anyMatch(
					v -> fieldValueMatchesDependingOnValuesNOTValuer(dependingOnField, dependingOnValuesList)));

			// check value and determine if to hide or show
			((AbstractField) dependingOnField).addValueChangeListener(e -> {
				boolean visible = fieldValueMatchesDependingOnValuesNOTValuer(dependingOnField, dependingOnValuesList);

				component.setVisible(visible);
				if (typex != CampaignFormElementType.LABEL) {
					if (!visible) {
						((AbstractField) component).setValue(null);
					}
				}
			});
		} else {

			// hide on default
			component.setVisible(dependingOnValuesList.stream()
					.anyMatch(v -> fieldValueMatchesDependingOnValues(dependingOnField, dependingOnValuesList)));

			// check value and determine if to hide or show
			((AbstractField) dependingOnField).addValueChangeListener(e -> {
				boolean visible = fieldValueMatchesDependingOnValues(dependingOnField, dependingOnValuesList);

				component.setVisible(visible);
				if (typex != CampaignFormElementType.LABEL) {
					if (!visible) {
						((AbstractField) component).setValue(null);
					}
				}
			});
		}
	}

	private boolean fieldValueMatchesDependingOnValues(Component dependingOnField, List<Object> dependingOnValuesList) {
		if (((AbstractField) dependingOnField).getValue() == null) {
			return false;
		}

//		if (dependingOnField instanceof NullableOptionGroup) {
////			String booleanValue = Boolean.TRUE.equals(((NullableOptionGroup) dependingOnField).getNullableValue())
////					? "true"
////					: "false";
//			String stringValue = Boolean.TRUE.equals(((NullableOptionGroup) dependingOnField).getNullableValue())
//					? "Yes"
//					: "No";
//
//			return dependingOnValuesList.stream().anyMatch(
//					v -> 
//					//v.toString().equalsIgnoreCase(booleanValue) || 
//					v.toString().equalsIgnoreCase(stringValue));
//		} else {

			return dependingOnValuesList.stream()
					.anyMatch(v -> v.toString().equalsIgnoreCase(((AbstractField) dependingOnField).getValue().toString()));
	//	}
	}

	private boolean fieldValueMatchesDependingOnValuesNOTValuer(Component dependingOnField,
			List<Object> dependingOnValuesList) {
		if (((AbstractField) dependingOnField).getValue() == null) {
			return false;
		}

		if (dependingOnField instanceof ToggleButton) {
			String booleanValue = Boolean.TRUE.equals(((ToggleButton) dependingOnField).getValue())
					? "false"
					: "true";
			String stringValue = Boolean.TRUE.equals(((ToggleButton) dependingOnField).getValue()) ? "no"
					: "yes";

			return dependingOnValuesList.stream()
					.anyMatch(v -> v.toString().replaceAll("!", "").equalsIgnoreCase(booleanValue)
							|| v.toString().replaceAll("!", "").equalsIgnoreCase(stringValue));
		} else {

			return dependingOnValuesList.stream().anyMatch(
					v -> !v.toString().replaceAll("!", "").equalsIgnoreCase(((AbstractField) dependingOnField).getValue().toString()));
		}
	}

	public String get18nCaption(String elementId, String defaultCaption) {
		if (userTranslations != null && userTranslations.containsKey(elementId)) {
			return userTranslations.get(elementId);
		}

		return defaultCaption;
	}

	public List<CampaignFormDataEntry> getFormValues() {
		return fields.keySet().stream().map(id -> {
			Component field = fields.get(id);
			if (field instanceof ToggleButton) {

				  String valc = ((ToggleButton) field).getValue() != null ? ((ToggleButton) field).getValue().toString().equalsIgnoreCase("true") ||((ToggleButton) field).getValue().toString().equalsIgnoreCase("YES") || ((ToggleButton) field).getValue().toString().equalsIgnoreCase("[YES]") || ((ToggleButton) field).getValue().toString().equalsIgnoreCase("[true]") ? "Yes" :
					  ((ToggleButton) field).getValue().toString().equalsIgnoreCase("[NO]") || ((ToggleButton) field).getValue().toString().equalsIgnoreCase("NO") || ((ToggleButton) field).getValue().toString().equalsIgnoreCase("false") || ((ToggleButton) field).getValue().toString().equalsIgnoreCase("[false]") ? "No" : null : null;
				 
				return new CampaignFormDataEntry(id, valc);
						
			} else {
				return new CampaignFormDataEntry(id, ((AbstractField) field).getValue());
			}
		}).collect(Collectors.toList());
	}
//
////let change this method to litrate through all the field, check the validity, and return ;ist of those that are not valid in a catch block
//	public void validateFields() {
		
		
		
		
//		try {
//			fields.forEach((key, value) -> {
//
//				AbstractField formField = fields.get(key);
//				formField
//				if (!fields.get(key).val .isValid()) {
//					fields.get(key).setRequiredError("Error found");
//				}
//			});
//		} finally {
//
//			fields.forEach((key, value) -> {
//
//				AbstractField formField = fields.get(key);
//				try {
//
//					formField.validate();
//
//				} catch (Validator.InvalidValueException e) {
//
//					throw (InvalidValueException) e;
//				}
//			});
//		}

//	}
//
	public void resetFormValues() {

		fields.keySet().forEach(key -> {
			Component field = fields.get(key);
			((AbstractField) field).setValue(formValuesMap.get(key));
		});
	}

	public List<CampaignFormElement> getFormElements() {
		return formElements;
	}

	public Map<String, Component> getFields() {
		return fields;
	}

}
