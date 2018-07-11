package de.symeda.sormas.app.event.edit;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.person.CauseOfDeath;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.component.Item;
import de.symeda.sormas.app.component.controls.ControlSpinnerField;
import de.symeda.sormas.app.core.OnSetBindingVariableListener;

public class CauseOfDeathLayoutProcessor {

    private List<Item> diseaseList;
    private List<Item> deathPlaceTypeList;
    private Context context;
    private ViewDataBinding contentBinding;
    private LinearLayout rootChildLayout;
    private OnSetBindingVariableListener mOnSetBindingVariableListener;
    private ViewDataBinding binding;

    private Person record;
    private CauseOfDeath initialDeathCause;
    private Disease initialCauseOfDeathDisease;
    private String initialCauseOfDeathDetails;

    public CauseOfDeathLayoutProcessor(Context context, ViewDataBinding contentBinding, Person record, List<Item> deathPlaceTypeList, List<Item> diseaseList) {
        this.context = context;
        this.contentBinding = contentBinding;
        this.record = record;

        this.deathPlaceTypeList = deathPlaceTypeList;
        this.diseaseList = diseaseList;

        this.initialDeathCause = record.getCauseOfDeath();
        this.initialCauseOfDeathDisease = record.getCauseOfDeathDisease();
        this.initialCauseOfDeathDetails = record.getCauseOfDeathDetails();

        hideRootChildLayout();
    }

    public boolean processLayout(CauseOfDeath causeOfDeath) {
        int layoutResId = getLayoutResId(causeOfDeath);
        String layoutName = getLayoutName(layoutResId);

        if (layoutResId <= 0) {
            hideRootChildLayout();
            return false;
        }

        //ensureCauseOfDeathDetailIntegrity(causeOfDeath);
        //return inflateChildLayout(layoutResId, causeOfDeath);


        ensureCauseOfDeathDataIntegrity(causeOfDeath);
        binding = inflateChildLayout(layoutResId, causeOfDeath);

        if (binding == null)
            return false;

        performSetBindingVariable(binding, layoutName);

        return initializeChildLayout(binding, causeOfDeath);
    }

    private ViewDataBinding inflateChildLayout(int layoutResId, CauseOfDeath causeOfDeath) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layout = (ViewGroup) inflater.inflate(layoutResId, null);

        ViewDataBinding binding = DataBindingUtil.bind(layout);
        //String layoutName = context.getResources().getResourceEntryName(layoutResId);
        //performSetBindingVariable(binding, layoutName);
        //setRootNotificationBindingVariable(b, layoutName);
        //setLocalBindingVariable(b, layoutName);

        return binding;
    }

    private boolean initializeChildLayout(ViewDataBinding binding, final CauseOfDeath causeOfDeath) {
        View innerRootLayout = binding.getRoot();
        final ControlSpinnerField spnDisease = (ControlSpinnerField) innerRootLayout.findViewById(R.id.caseData_disease);


        binding.addOnRebindCallback(new OnRebindCallback() {
            @Override
            public void onBound(ViewDataBinding binding) {
                super.onBound(binding);

                if (spnDisease == null)
                    return;

                if (causeOfDeath == CauseOfDeath.EPIDEMIC_DISEASE) {
                    spnDisease.initializeSpinner(diseaseList);
                }
            }
        });

        LinearLayout rootLayout = getRootChildLayout();

        if (getRootChildLayout() != null) {
            getRootChildLayout().removeAllViews();
            getRootChildLayout().addView(innerRootLayout);

            getRootChildLayout().setVisibility(View.VISIBLE);
        }

        return true;
    }

    private LinearLayout getRootChildLayout() {
        /*if (PresentConditionLayoutProcessor.this.getRootChildLayout() == null)
            return null;

        return (LinearLayout) PresentConditionLayoutProcessor.this.getRootChildLayout().findViewById(R.id.causeOfDeathInclude);*/

        if (rootChildLayout == null)
            rootChildLayout = (LinearLayout)contentBinding.getRoot().findViewById(R.id.causeOfDeathInclude);

        return rootChildLayout;
    }

    private void hideRootChildLayout() {
        if (getRootChildLayout() == null)
            return;

        getRootChildLayout().setVisibility(View.GONE);
        getRootChildLayout().removeAllViews();
    }

    private int getLayoutResId(CauseOfDeath causeOfDeath) {
        if (causeOfDeath == CauseOfDeath.EPIDEMIC_DISEASE) {
            return R.layout.fragment_event_edit_person_info_death_cause_epidemic_layout;
        } else if (causeOfDeath == CauseOfDeath.OTHER_CAUSE) {
            return R.layout.fragment_event_edit_person_info_death_cause_other_layout;
        }

        return -1;
    }

    private String getLayoutName(int layoutResId) {
        if (layoutResId <= 0)
            return null;

        return context.getResources().getResourceEntryName(layoutResId);
    }

    private void ensureCauseOfDeathDataIntegrity(CauseOfDeath causeOfDeath) {
        if (initialDeathCause == causeOfDeath) {
            record.setCauseOfDeathDisease(initialCauseOfDeathDisease);
            record.setCauseOfDeathDetails(initialCauseOfDeathDetails);
        } else {
            record.setCauseOfDeathDisease(null);
            record.setCauseOfDeathDetails(null);
        }
    }

    private void performSetBindingVariable(ViewDataBinding binding, String layoutName) {
        if (this.mOnSetBindingVariableListener != null) {
            this.mOnSetBindingVariableListener.onSetBindingVariable(binding, layoutName);
        }
    }

    public void setOnSetBindingVariable(OnSetBindingVariableListener listener) {
        this.mOnSetBindingVariableListener = listener;
    }
}
