package com.cinoteck.application.views.logout;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.cinoteck.application.UserProvider;
import com.cinoteck.application.utils.authentication.AccessControl;
import com.cinoteck.application.utils.authentication.AccessControlFactory;
import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;

@PageTitle("Logout")
@Route(value = "logout", layout = MainLayout.class)

public class LogoutView extends VerticalLayout {
	private Button confirmButton;
	private Button cancelButton;
	UserProvider userProvider = new UserProvider();

	public LogoutView() {
		if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);			
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
		FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());
		Dialog dialog = new Dialog();
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);
		
		VerticalLayout dialogHolderLayout = new VerticalLayout();
		
		   
		Div apmisImageContainer = new Div();
		apmisImageContainer.getStyle().set("width", "100%");
		apmisImageContainer.getStyle().set("display", "flex");
		apmisImageContainer.getStyle().set("justify-content", "center");

		Image img = new Image("images/logout.png", "APMIS-LOGO");
		img.getStyle().set("max-height", "-webkit-fill-available");

		apmisImageContainer.add(img);

		Div aboutText = new Div();
		
//		Paragraph text = new Paragraph("You are attempting to log out of APMIS");
		Paragraph text = new Paragraph(I18nProperties.getString(Strings.areSureYouWantToLogout));
		
		
		text.getStyle().set("color", "black");
		text.getStyle().set("font-size", "24px");
//		confirmationText.getStyle().set("color", "green");
//		confirmationText.getStyle().set("font-size", "18px");
		
		
		aboutText.getStyle().set("display", "flex");
		aboutText.getStyle().set("flex-direction", "column");
		aboutText.getStyle().set("align-items", "center");
		aboutText.add(text);

		Div logoutButtons = new Div();
		logoutButtons.getStyle().set("display", "flex");
		logoutButtons.getStyle().set("justify-content", "space-evenly");
		logoutButtons.getStyle().set("width", "100%");
	   
		final AccessControl accessControl = AccessControlFactory.getInstance()
                .createAccessControl();
		//TODO make this check the sesssion and invalidate it... it terms of Spring.. let use another method
		confirmButton = new Button(I18nProperties.getCaption(Captions.actionConfirm), event -> {
			accessControl.signOut();
			
			//confirmButton.getUI().ifPresent(ui -> ui.navigate(""));
		});
		
		
		confirmButton.getStyle().set("width", "35%");
		cancelButton = new Button(I18nProperties.getCaption(Captions.actionCancel), event -> {
			dialog.close();
			cancelButton.getUI().ifPresent(ui -> ui.navigate("dashboard"));
		});
		cancelButton.getStyle().set("width", "35%");
		cancelButton.getStyle().set("background", "white");
		cancelButton.getStyle().set("color", "green");
		logoutButtons.add(confirmButton, cancelButton);

		dialogHolderLayout.add(apmisImageContainer, aboutText, logoutButtons);
		dialog.add(dialogHolderLayout);
		
		
		dialog.open();

		dialog.open();
		add(dialog);

	}
}
