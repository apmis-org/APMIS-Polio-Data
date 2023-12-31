package de.symeda.sormas.api.campaign.form;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.validation.Valid;

import de.symeda.sormas.api.user.FormAccess;

@Remote
public interface CampaignFormMetaFacade {

	CampaignFormMetaDto saveCampaignFormMeta(@Valid CampaignFormMetaDto campaignFormMetaDto);

	/**
	 * Validates the campaign form by checking whether mandatory elements are included, only supported types are used
	 * and elements used in associations are included in the schema. In addition, cleans any elements that are used
	 * in the UI from any HTML tags but those defined in {@link CampaignFormElement#ALLOWED_HTML_TAGS}.
	 */
	void validateAndClean(CampaignFormMetaDto campaignFormMetaDto);

	void validateAllFormMetas();

	CampaignFormMetaDto buildCampaignFormMetaFromJson(String formId, String languageCode, String schemaDefinitionJson, String translationsJson)
		throws IOException;

	List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferences();
	
	List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRound(String round); 
	
	List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundandCampaign(String round, String campaignUUID);

	List<CampaignFormMetaReferenceDto> getAllCampaignFormMetasAsReferencesByRoundandCampaignandForm(String round, String campaignUUID, Set<FormAccess> userFormAccess);

	CampaignFormMetaDto getCampaignFormMetaByUuid(String campaignFormUuid);
	
	CampaignFormMetaReferenceDto getCampaignFormMetaReferenceByUuid(String campaignFormUuid);

	List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaign(String uuid);
	
	List<CampaignFormMetaReferenceDto> getCampaignFormMetaAsReferencesByCampaignIntraCamapaign(String uuid);

    List<CampaignFormMetaDto> getAllAfter(Date campaignFormMetaChangeDate);

	List<String> getAllUuids();

	List<CampaignFormMetaDto> getByUuids(List<String> uuids);

	List<CampaignFormMetaReferenceDto> getCampaignFormMetaAsReferencesByCampaignPostCamapaign(String uuid);
}
