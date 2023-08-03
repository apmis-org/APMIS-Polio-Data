package com.cinoteck.application.views.myaccount;

import com.cinoteck.application.LanguageSwitcher;
import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;

import com.vaadin.flow.component.html.H1;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import com.vaadin.flow.component.notification.Notification;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;

import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;

import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
//import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.location.LocationDto;

import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;

import de.symeda.sormas.api.user.UserDto;
//import de.symeda.sormas.ui.utils.InternalPasswordChangeComponent;
//import de.symeda.sormas.ui.utils.VaadinUiUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@PageTitle("APMIS-My Account")
@Route(value = "useraccount", layout = MainLayout.class)

public class MyAccountView extends VerticalLayout implements RouterLayout {
	private Map<Tab, Component> tabComponentMap = new LinkedHashMap<>();

	UserProvider userProvider = new UserProvider();
	
	

	public MyAccountView() {
		
		if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);			
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
		setSpacing(false);
		setPadding(false);
		Binder<UserDto> binder = new BeanValidationBinder<>(UserDto.class);

		List<AreaReferenceDto> regionss;
		List<RegionReferenceDto> provincess;
		List<DistrictReferenceDto> districtss;
		List<CommunityReferenceDto> communitiess;

		UserDto currentUser = FacadeProvider.getUserFacade().getCurrentUser();
		// currentUser.getFirstName();

		Div userentry = new Div();

		H3 infooo = new H3(I18nProperties.getCaption(Captions.User_userName));
		infooo.getStyle().set("color", "green");
		infooo.getStyle().set("font-size", "20px");
		infooo.getStyle().set("font-weight", "600");
		infooo.getStyle().set("margin-left", "20px");
		infooo.getStyle().set("margin-bottom", "15px");

		Paragraph infoood = new Paragraph(currentUser.getUserName());
		// Paragraph infoood = new Paragraph(currentUser.getUserRoles().size()+ "");
		infoood.getStyle().set("margin-left", "20px");
		infoood.getStyle().set("margin-bottom", "0px");

		// Div personalInfoo = new Div();
		H3 infoo = new H3("Personal Information");
		infoo.getStyle().set("color", "green");
		infoo.getStyle().set("font-size", "20px");
		infoo.getStyle().set("font-weight", "600");
		infoo.getStyle().set("margin-left", "20px");
		infoo.getStyle().set("margin-bottom", "0px");

		TextField firstnamee = new TextField("");
		firstnamee.setLabel(I18nProperties.getCaption(Captions.firstName));
		firstnamee.setValue(currentUser.getFirstName());
		// firstnamee.setClassName("fName");
		firstnamee.setId("my-disabled-textfield");
		firstnamee.getStyle().set("-webkit-text-fill-color", "green");
		firstnamee.setReadOnly(true);
		// binder.forField(firstnamee).asRequired("First Name is
		// Required").bind(UserDto::getFirstName, UserDto::setFirstName);

		TextField lastnamee = new TextField("");
		lastnamee.setLabel(I18nProperties.getCaption(Captions.lastName));
		lastnamee.setValue(currentUser.getLastName());
		lastnamee.getStyle().set("-webkit-text-fill-color", "green");
		lastnamee.setReadOnly(true);
		// binder.forField(lastnamee).asRequired("Last Name is
		// Required").bind(UserDto::getLastName, UserDto::setLastName);

		TextField emailAddresss = new TextField("");
		emailAddresss.setLabel(I18nProperties.getCaption(Captions.User_userEmail));
		if (currentUser.getUserEmail() == null) {
			emailAddresss.setPlaceholder(I18nProperties.getCaption(Captions.User_userEmail));
		} else {
			emailAddresss.setValue(currentUser.getUserEmail());
		}
		emailAddresss.setReadOnly(true);
		binder.forField(emailAddresss).asRequired("Email Address is Required").bind(UserDto::getUserEmail,
				UserDto::setUserEmail);

		TextField phoneNumberr = new TextField();
		phoneNumberr.setLabel("Phone number");
		if (currentUser.getPhone() == null) {
			phoneNumberr.setPlaceholder("Phone Number");
		} else {
			phoneNumberr.setValue(currentUser.getPhone());
		}
		phoneNumberr.setReadOnly(true);
//		binder.forField(phoneNumberr).withValidator(e -> e.length() >= 10, "Enter a valid Phone Number")
//				.bind(UserDto::getPhone, UserDto::setPhone);

//		Select<String> positionn = new Select<>();
//		positionn.setLabel("Position");
//		positionn.setItems("");
//		positionn.setValue("");

		TextField positionn = new TextField();
		positionn.setLabel(I18nProperties.getCaption(Captions.User_userPosition));
		// positionn.setValue(currentUser.getUserPosition());
		if (currentUser.getPhone() == null) {
			positionn.setPlaceholder(I18nProperties.getCaption(Captions.User_userPosition));
		} else {
			positionn.setValue(currentUser.getUserPosition());
		}
		positionn.setReadOnly(true);
		// binder.forField(positionn).bind(UserDto::getUserPosition,
		// UserDto::setUserPosition);

//TODO:: add a model for address in userDto
		TextField addresss = new TextField();
		addresss.setLabel(I18nProperties.getCaption(Captions.address));
		addresss.setReadOnly(true);
		// binder.forField(addresss).bind(UserDto::getAddress,UserDto::setAddress);

		FormLayout dataVieww = new FormLayout();
		dataVieww.add(firstnamee, lastnamee, emailAddresss, phoneNumberr, positionn, addresss);
		dataVieww.getStyle().set("margin-left", "20px");
		dataVieww.getStyle().set("margin-right", "20px");

		Div fieldInfoo = new Div();
		H3 infodataa = new H3("Field Information");
		infodataa.getStyle().set("color", "green");
		infodataa.getStyle().set("font-size", "20px");
		infodataa.getStyle().set("font-weight", "600");
		infodataa.getStyle().set("margin-left", "20px");
		infodataa.getStyle().set("margin-bottom", "0px");

		// Select<String> regionn = new Select<>();
		ComboBox<AreaReferenceDto> regionn = new ComboBox<>(I18nProperties.getCaption(Captions.area));
		// regionn.setLabel("Region");
		binder.forField(regionn).bind(UserDto::getArea, UserDto::setArea);
		regionss = FacadeProvider.getAreaFacade().getAllActiveAsReference();
		regionn.setItems(regionss);
		regionn.setItemLabelGenerator(AreaReferenceDto::getCaption);
		// TODO: come back to add valuechangelistener

//		regionn.setItems("", "Region", "Region", "Region", "Region", "Region", "Region");
//		regionn.setValue("");

		// Select<String> provincee = new Select<>();
		ComboBox<RegionReferenceDto> provincee = new ComboBox<>(I18nProperties.getCaption(Captions.region));
		// provincee.setLabel("Province");
		binder.forField(provincee).bind(UserDto::getRegion, UserDto::setRegion);
		provincee.setItemLabelGenerator(RegionReferenceDto::getCaption);
		// provincee.setItems("", "Province", "Province", "Province", "Province",
		// "Province");
//		provincee.setValue("");

		// Select<String> districtt = new Select<>();
		ComboBox<DistrictReferenceDto> districtt = new ComboBox<>(I18nProperties.getCaption(Captions.district));
		// districtt.setLabel("District");
		binder.forField(districtt).bind(UserDto::getDistrict, UserDto::setDistrict);
		districtt.setItemLabelGenerator(DistrictReferenceDto::getCaption);

//		districtt.setItems("", "District", "District", "District", "District", "District");
//		districtt.setValue("");

		// Select<String> clusterr = new Select();

		MultiSelectComboBox<CommunityReferenceDto> cluster = new MultiSelectComboBox<>(
				I18nProperties.getCaption(Captions.community));
		cluster.setLabel(I18nProperties.getCaption(Captions.community));
		binder.forField(cluster).bind(UserDto::getCommunity, UserDto::setCommunity);

		// clusterr.setItems("", "Cluster", "Cluster", "Cluster", "Cluster", "Cluster");

		TextField streett = new TextField();
		streett.setLabel(I18nProperties.getCaption(Captions.Location_street));

		TextField houseNumm = new TextField();
		houseNumm.setLabel(I18nProperties.getCaption(Captions.Location_houseNumber));

		TextField addInfoo = new TextField();
		addInfoo.setLabel(I18nProperties.getCaption(Captions.Location_additionalInformation));

		TextField postalCodee = new TextField();
		postalCodee.setLabel(I18nProperties.getCaption(Captions.Location_postalCode));

		TextField cityy = new TextField();
		cityy.setLabel(I18nProperties.getCaption(Captions.city));

		Select<String> areaTypee = new Select<>();
		areaTypee.setLabel(I18nProperties.getCaption(Captions.Location_areaType));
		areaTypee.setItems("", "Urban", "Rural");
		areaTypee.setValue("");

		TextField contacPersonn = new TextField();
		contacPersonn.setLabel(I18nProperties.getCaption(Captions.Location_details));

		FormLayout fielddataVieww = new FormLayout();
		fielddataVieww.setResponsiveSteps(
				// Use one column by default
				new ResponsiveStep("0", 1),
				// Use two columns, if the layout's width exceeds 320px
				new ResponsiveStep("320px", 2),
				// Use three columns, if the layout's width exceeds 500px
				new ResponsiveStep("500px", 3));
		fielddataVieww.add(provincee, regionn, districtt, cluster, streett, houseNumm, addInfoo, postalCodee, cityy,
				areaTypee, contacPersonn);
		fielddataVieww.getStyle().set("margin-left", "20px");
		fielddataVieww.getStyle().set("margin-right", "20px");

		H3 security = new H3("Password & Accesibility");
		security.getStyle().set("color", "green");
		security.getStyle().set("font-size", "20px");
		security.getStyle().set("font-weight", "600");
		security.getStyle().set("margin-left", "20px");
		security.getStyle().set("margin-bottom", "15px");
		security.getStyle().set("margin-top", "16px !important");

		Dialog passwordDialog = new Dialog();
		// change password button. set it to figma design
		// Button openPasswordPopupButton = new Button("Change Password");
//		passwordDialog.getStyle().set("margin-bottom", "0px");
//		passwordDialog.getStyle().set("margin-top", "12px !important");
		// openPasswordPopupButton.addClickListener(event -> passwordDialog.open());

		// Dialog passwordDialog = new Dialog();
		// change password button. set it to figma design
		Button openPasswordPopupButton = new Button("Change Password");
//		passwordDialog.getStyle().set("margin-bottom", "0px");
//		passwordDialog.getStyle().set("margin-top", "12px !important");

		openPasswordPopupButton.addClickListener(event -> {
			CredentialPassWordChanger sev = new CredentialPassWordChanger(currentUser);
		});
		add();

		VerticalLayout pwdSecc = new VerticalLayout();
		pwdSecc.setClassName("superDiv");

		ComboBox<Language> languagee = new ComboBox<>(I18nProperties.getCaption(Captions.language));
		languagee.setItemLabelGenerator(Language::toString);
		languagee.setItems(Language.getAssignableLanguages());
		languagee.getStyle().set("margin-bottom", "0px");
		languagee.getStyle().set("margin-top", "-15px !important");

		binder.forField(languagee).asRequired("Language is Required").bind(UserDto::getLanguage, UserDto::setLanguage);

		languagee.setRequired(true);
		// binder.forField(languagee).asRequired("Language is
		// Required").bind(UserDto::getLanguage, UserDto::setLanguage);
		languagee.setValue(currentUser.getLanguage());

		languagee.getStyle().set("width", "400px");

		languagee.getStyle().set("width", "400px");

		Div anch = new Div();
		anch.setClassName("anchDiv");
		pwdSecc.getStyle().set("margin-left", "20px");

		pwdSecc.add(openPasswordPopupButton, languagee, anch);

		Div actionss = new Div();

		Icon vadIc = new Icon(VaadinIcon.CLOSE_CIRCLE_O);
		vadIc.setId("fghf");
		vadIc.getStyle().set("color", "green !important");

		Icon vadIcc = new Icon(VaadinIcon.CHECK_CIRCLE_O);
		vadIc.getStyle().set("color", "white");

		Button discard = new Button(I18nProperties.getCaption(Captions.actionDiscard), vadIc);
		discard.getStyle().set("margin-right", "20px");
		discard.getStyle().set("color", "green");
		discard.getStyle().set("background", "white");
		discard.getStyle().set("border", "1px solid green");

		Button savee = new Button(I18nProperties.getCaption(Captions.actionSave), vadIcc);
		savee.addClickListener(e -> {
			UserDto currentUserToSave = FacadeProvider.getUserFacade().getCurrentUser();
			if (languagee.getValue() != null) {

				LanguageSwitcher languageSwitcher = new LanguageSwitcher();
				currentUserToSave.setLanguage(languagee.getValue());
				FacadeProvider.getUserFacade().saveUser(currentUserToSave);
				I18nProperties.setUserLanguage(languagee.getValue());
				I18nProperties.getUserLanguage();
				Notification.show("Language setting saved successfully " + languagee.getValue());

				System.out.println(userProvider.getUser().getLanguage().toString());
				String userLanguage = userProvider.getUser().getLanguage().toString();

				if (userLanguage.equals("Pashto")) {

					languageSwitcher.switchLanguage(new Locale("ps"));
				} else if (userLanguage.equals("Dari")) {

					languageSwitcher.switchLanguage(new Locale("fa"));
				} else {
					
					languageSwitcher.switchLanguage(Locale.ENGLISH);
				}

			} else {

				Notification.show("Choose prefered language: " + languagee.isInvalid());
			}

		});
		actionss.getStyle().set("margin", "20px");
		actionss.add(discard, savee);
		userentry.add(infooo, infoood, infoo, dataVieww, infodataa, fieldInfoo, fielddataVieww, security, pwdSecc,
				actionss);

		add(userentry);

		// initial idea for a change of password
//		Dialog dialog = new Dialog();
//		dialog.setCloseOnEsc(false);
//		dialog.setCloseOnOutsideClick(false);
//
//		Label messageLabel = new Label("Update Password");
//		
//		TextField newPasswordField = new TextField("New Password");
//		TextField confirmNewPasswordField = new TextField("Confirm New Password");
//		
//		Label instructionLabel = new Label("Choose a new password for your account\r\n <br>"
//				+ "*Must be at least 8 characters\r\n <br>"
//				+ "*Must contain 1 Uppercase and 1 special character\r\n"
//				+ "");
//		instructionLabel.getElement().setProperty("innerHTML", instructionLabel.getText());
//		
//		Button closeButton = new Button("Close");
//		closeButton.addClickListener(event -> dialog.close());
//		
//		VerticalLayout layout = new VerticalLayout();
//		layout.add(messageLabel, newPasswordField);
//		layout.add(confirmNewPasswordField);
//		
//		dialog.add(layout, instructionLabel, closeButton);
//
//	//show popup	
//		Button openDialogButton = new Button("Change Password");
//		openDialogButton.addClickListener(event -> dialog.open());
//
//		add(openDialogButton);

		// trying out a new change password field

		passwordDialog.setCloseOnEsc(false);
		passwordDialog.setCloseOnOutsideClick(false);

		FormLayout formLayout = new FormLayout();

		PasswordField newPasswordField = new PasswordField(I18nProperties.getString(Strings.headingNewPassword));
		newPasswordField.setRevealButtonVisible(true);
		PasswordField confirmPasswordField = new PasswordField("Confirm Password");
		confirmPasswordField.setRevealButtonVisible(true);

		Label instructionLabel = new Label(
				"Choose a new password for your account\r\n <br>" + "*Must be at least 8 characters\r\n <br>"
						+ "*Must contain 1 Uppercase and 1 special character\r\n" + "");
		instructionLabel.getElement().setProperty("innerHTML", instructionLabel.getText());

		// setting action buttons for password change
		Button cancelButton = new Button(I18nProperties.getCaption(Captions.actionCancel));
		cancelButton.addClickListener(event -> passwordDialog.close());

		Button saveButton = new Button(I18nProperties.getCaption(Captions.actionSave));
		saveButton.addClickListener(event -> {
			// Perform password validation and saving logic here
			passwordDialog.close();
		});

		// setting css for the actions buttons
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.add(cancelButton, new Div(), saveButton); // Add an empty Div for spacing

		cancelButton.addClickListener(event -> passwordDialog.close());

		buttonLayout.getStyle().set("margin-top", "1em");

		passwordDialog.add(formLayout, buttonLayout);

		// setting layout for textfields
		VerticalLayout layout = new VerticalLayout();
		layout.add(newPasswordField);
		layout.add(confirmPasswordField);
		formLayout.add(newPasswordField, confirmPasswordField, instructionLabel);

	}

}