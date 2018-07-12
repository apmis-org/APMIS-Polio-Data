package de.symeda.sormas.app.visit.edit;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;

import de.symeda.sormas.api.utils.ValidationException;
import de.symeda.sormas.api.visit.VisitStatus;
import de.symeda.sormas.app.BaseEditActivity;
import de.symeda.sormas.app.BaseEditFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.visit.Visit;
import de.symeda.sormas.app.component.menu.LandingPageMenuItem;
import de.symeda.sormas.app.core.async.AsyncTaskResult;
import de.symeda.sormas.app.core.async.SavingAsyncTask;
import de.symeda.sormas.app.core.async.TaskResultHolder;
import de.symeda.sormas.app.shared.VisitFormNavigationCapsule;
import de.symeda.sormas.app.symptom.Symptom;
import de.symeda.sormas.app.visit.VisitSection;

public class VisitNewActivity extends BaseEditActivity<Visit> {

    public static final String TAG = VisitNewActivity.class.getSimpleName();

    private String contactUuid;

    private AsyncTask saveTask;

    @Override
    protected void onCreateInner(Bundle savedInstanceState) {
        super.onCreateInner(savedInstanceState);
        contactUuid = getContactUuidArg(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveContactUuidState(outState, contactUuid);
    }

    @Override
    protected Visit queryRootEntity(String recordUuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Visit buildRootEntity() {
        Visit visit = DatabaseHelper.getVisitDao().build(contactUuid);
        return visit;
    }

    @Override
    public VisitStatus getPageStatus() {
        return (VisitStatus) super.getPageStatus();
    }

    @Override
    protected BaseEditFragment buildEditFragment(LandingPageMenuItem menuItem, Visit activityRootData) {
        VisitFormNavigationCapsule dataCapsule = new VisitFormNavigationCapsule(
                VisitNewActivity.this, getRootEntityUuid(), getPageStatus());
        return VisitEditFragment.newInstance(dataCapsule, activityRootData);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        getSaveMenu().setTitle(R.string.action_save_followup);
        return result;
    }

    @Override
    public void saveData() {

        final Visit visitToSave = getStoredRootEntity();

        saveTask = new SavingAsyncTask(getRootView(), visitToSave) {

            @Override
            protected void onPreExecute() {
                showPreloader();
            }

            @Override
            public void doInBackground(TaskResultHolder resultHolder) throws Exception {
                validateData(visitToSave);
                DatabaseHelper.getVisitDao().saveAndSnapshot(visitToSave);
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<TaskResultHolder> taskResult) {
                hidePreloader();
                super.onPostExecute(taskResult);
                if (taskResult.getResultStatus().isSuccess()) {
                    if (visitToSave.getVisitStatus() == VisitStatus.COOPERATIVE) {
                        // enter symptoms
                        VisitEditActivity.goToActivity(VisitNewActivity.this,
                                new VisitFormNavigationCapsule(VisitNewActivity.this, visitToSave.getUuid(), visitToSave.getVisitStatus())
                                        .setActiveMenu(VisitSection.SYMPTOMS.ordinal()));
                    } else {
                        // back to contact
                        finish();
                    }
                }
            }
        }.executeOnThreadPool();
    }

    private void validateData(Visit data) throws ValidationException {
        //TODO: Validation
        /*VisitValidator.clearErrorsForVisitData(visitDataBinding);
        SymptomsValidator.clearErrorsForSymptoms(symptomsBinding);

        int validationErrorTab = -1;

        if (!SymptomsValidator.validateVisitSymptoms(visit, symptoms, symptomsBinding)) {
            validationErrorTab = VisitEditTabs.SYMPTOMS.ordinal();
        }
        if (!VisitValidator.validateVisitData(visit, contact, visitDataBinding)) {
            validationErrorTab = VisitEditTabs.VISIT_DATA.ordinal();
        }

        if (validationErrorTab >= 0) {
            pager.setCurrentItem(validationErrorTab);
            return true;
        }*/
    }

    @Override
    protected int getActivityTitle() {
        return R.string.heading_level3_1_contact_visit_info;
    }

    public static void goToActivity(Context fromActivity, String contactUuid) {
        BaseEditActivity.goToActivity(fromActivity, VisitNewActivity.class,
                new VisitFormNavigationCapsule(fromActivity, null, null)
                        .setContactUuid(contactUuid));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (saveTask != null && !saveTask.isCancelled())
            saveTask.cancel(true);
    }
}