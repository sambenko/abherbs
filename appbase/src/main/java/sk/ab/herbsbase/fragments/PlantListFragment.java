package sk.ab.herbsbase.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import sk.ab.common.Constants;
import sk.ab.common.entity.Plant;
import sk.ab.herbsbase.AndroidConstants;
import sk.ab.herbsbase.BaseApp;
import sk.ab.herbsbase.R;
import sk.ab.herbsbase.activities.ListPlantsBaseActivity;
import sk.ab.herbsbase.commons.PlantViewHolder;
import sk.ab.herbsbase.tools.Utils;

public class PlantListFragment extends Fragment {
    static final String STATE_POSITION = "list_position";

    private int list_position;

    private class PropertyAdapter extends FirebaseIndexRecyclerAdapter<Plant, PlantViewHolder> {

        PropertyAdapter(Class<Plant> modelClass, @LayoutRes int modelLayout, Class<PlantViewHolder> viewHolderClass, Query keyRef, Query dataRef) {
            super(modelClass, modelLayout, viewHolderClass, keyRef, dataRef);
        }

        @Override
        protected void populateViewHolder(final PlantViewHolder holder, final Plant plant, int position) {
            DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
            holder.getPhoto().setImageResource(android.R.color.transparent);
            if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                int size = dm.widthPixels;
                holder.getPhoto().getLayoutParams().width = size;
                holder.getPhoto().getLayoutParams().height = size;
            } else {
                int size = dm.heightPixels - Utils.convertDpToPx(50, dm) - Utils.convertDpToPx(70, dm);
                holder.getPhoto().getLayoutParams().height = size;
                holder.getPhoto().getLayoutParams().width = size;
            }
            ((RelativeLayout.LayoutParams) holder.getFamilyIcon().getLayoutParams())
                    .setMargins(holder.getPhoto().getLayoutParams().width - Utils.convertDpToPx(55, dm),
                            holder.getPhoto().getLayoutParams().height - Utils.convertDpToPx(25, dm), 0, 0);

            if (plant.getPhotoUrls().get(0) != null) {
                Utils.displayImage(getActivity().getApplicationContext().getFilesDir(), AndroidConstants.STORAGE_PHOTOS + plant.getPhotoUrls().get(0),
                        holder.getPhoto(), ((BaseApp) getActivity().getApplication()).getOptions());
            }

            holder.getTitle().setText(getName(plant.getLabel()));
            for(Map.Entry<String, String> entry : plant.getTaxonomy().entrySet()) {
                if (entry.getKey().endsWith(Constants.TAXONOMY_FAMILY)) {
                    String family = entry.getValue();
                    holder.getFamily().setText(family);
                    Utils.displayImage(getActivity().getApplicationContext().getFilesDir(), AndroidConstants.STORAGE_FAMILIES + family
                            + AndroidConstants.DEFAULT_EXTENSION, holder.getFamilyIcon(), ((BaseApp) getActivity().getApplication()).getOptions());
                    break;
                }
            }

            holder.getPhoto().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list_position = holder.getAdapterPosition();
                    ((ListPlantsBaseActivity)getActivity()).selectPlant(plant.getName());
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            list_position = savedInstanceState.getInt(STATE_POSITION);
        }
        return inflater.inflate(R.layout.list, null);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getView() != null) {
            RecyclerView list = (RecyclerView) getView().findViewById(R.id.plant_list);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            } else {
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            }
            layoutManager.scrollToPosition(list_position);
            list.setLayoutManager(layoutManager);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference plantsRef = database.getReference(AndroidConstants.FIREBASE_PLANTS);
            DatabaseReference listRef = database.getReference(AndroidConstants.FIREBASE_LISTS + AndroidConstants.FIREBASE_SEPARATOR
                    + ((ListPlantsBaseActivity)getActivity()).getFilterString());

            PropertyAdapter adapter = new PropertyAdapter(Plant.class, R.layout.plant_row, PlantViewHolder.class, listRef, plantsRef);
            list.setAdapter(adapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (getView() != null) {
            RecyclerView list = (RecyclerView) getView().findViewById(R.id.plant_list);
            int pos = ((LinearLayoutManager) list.getLayoutManager()).findFirstVisibleItemPosition();
            savedInstanceState.putInt(STATE_POSITION, pos);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    private String getName(HashMap<String, String> names) {
        String language = Locale.getDefault().getLanguage();

        String name = names.get(language);
        if (name == null) {
            name = names.get(Constants.LANGUAGE_LA);
        }

        return name;
    }
}
